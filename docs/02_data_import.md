# Data Import, Video Suggestions & Tracking

> **Status**: Arkitektur og design besluttet. Backend-skjelett eksisterer men ingenting er implementert ennå (kun `Application.java`). Dette dokumentet er fremgangsplan + beslutningsdokument i ett.

> **Terminologi**: SleepingPill kaller ting «sessions» — vi kaller det **videos** i vår løsning. Interne klasser, tabeller og API-endepunkter bruker «video» konsekvent.

---

## Fremgangsplan

### Fase 1 – Database (Flyway-migrasjoner)

Opprett alle tabeller via Flyway-migrasjoner i riktig rekkefølge (fremmednøkler):

- [ ] `V1__create_conferences.sql` — `conferences(id, slug, name, year)`
- [ ] `V2__create_speakers.sql` — `speakers(id UUID, name, created_at)`, GIN-indeks på `name`
- [ ] `V3__create_videos.sql` — `videos(...)` med `keywords text[]`, `search_vector tsvector`, `priority_score int`, `vimeo_id text`
- [ ] `V4__create_video_speakers.sql` — join-tabell med `bio` og `bio_search_vector`
- [ ] `V5__create_video_events.sql` — `video_events(id, video_id, user_uuid, event_type, playback_position_seconds, created_at)`
- [ ] `V6__create_search_logs.sql` — `search_logs(id, query, results_count, created_at)`
- [ ] `V7__create_video_view_stats_view.sql` — aggregert view for unike seere per video

---

### Fase 2 – Data Import (`VideoImporter`)

Micronaut-service som henter data fra SleepingPill og upserer inn i databasen.

**Viktige design-beslutninger:**
- **Kun oppføringer med video importeres.** Foredrag uten `video`-felt hoppes over.
- **Idempotent**: Trygt å kjøre flere ganger. Bruker UPSERT (`ON CONFLICT DO NOTHING / DO UPDATE`).
- **Kjøring**: Ved oppstart (5s delay) og ukentlig mandag 03:00 (Micronaut `@Scheduled`).
- **Slugs**: Hardkodet liste i `VideoImporter.CONFERENCE_SLUGS`. Legg til nye år her.

**Vimeo ID-normalisering:**
SleepingPill-APIet returnerer av og til kun Vimeo-ID-en (`1115460917`) og av og til full URL (`https://vimeo.com/1115460917` eller `https://player.vimeo.com/video/1115460917`). Importøren må normalisere dette til bare ID-en:
```java
// VimeoUtils.extractId(String raw)
// 1. Regexp: (\d+) fra slutten av stringen
// Eksempler som skal gi "1115460917":
//   "1115460917"                              → "1115460917"
//   "https://vimeo.com/1115460917"            → "1115460917"
//   "https://player.vimeo.com/video/1115460917" → "1115460917"
static String extractId(String raw) {
    return raw.replaceAll(".*/", "").replaceAll("\\D.*", "").trim();
}
```
Vi lagrer kun den rene ID-en i databasen (`vimeo_id`). Frontend bygger embed-URL fra dette: `https://player.vimeo.com/video/{vimeo_id}`.

**Steg-for-steg i `VideoImporter.importAll()`:**

1. For hvert `slug` i `CONFERENCE_SLUGS`:
   - Kall `GET https://sleepingpill.javazone.no/public/allSessions/{slug}`
   - Parse JSON til interne DTO-er (`SleepingPillSession`, `SleepingPillSpeaker`)
2. Filtrer bort oppføringer uten `video`-felt
3. Normaliser `video`-feltet til ren Vimeo-ID via `VimeoUtils.extractId()`
4. Upsert `conferences` (slug → år)
5. For hvert video:
   - Beregn `priority_score` (se §Priority Score)
   - Upsert `videos` — alle felt unntatt `search_vector`
   - For hver speaker: Upsert `speakers` (UUID = `UUID.nameUUIDFromBytes(name.toLowerCase().getBytes())`)
   - Upsert `video_speakers` med bio
6. Oppdater `videos.search_vector` i bulk (etter speakers er på plass):
   ```sql
   UPDATE videos v SET search_vector = (
     setweight(to_tsvector('norwegian', coalesce(v.title,'')), 'A') ||
     setweight(to_tsvector('norwegian', coalesce(array_to_string(v.keywords,' '),'')), 'B') ||
     setweight(to_tsvector('norwegian', coalesce((SELECT string_agg(sp.name,' ') FROM video_speakers vs JOIN speakers sp ON vs.speaker_id = sp.id WHERE vs.video_id = v.id),'')), 'B') ||
     setweight(to_tsvector('norwegian', coalesce(v.abstract,'')), 'C')
   ) WHERE ...
   ```

- [ ] Opprett `VimeoUtils.extractId()` med tester
- [ ] Opprett `SleepingPillClient` (Micronaut HTTP-klient)
- [ ] Opprett `VideoImporter` service
- [ ] Enkel integrasjonstest: importer ett video, verifiser databaseinnhold

---

### Fase 3 – REST API (Controllers)

Alle endepunkter er read-only (GET), unntatt event-tracking (POST).

| Endepunkt | Beskrivelse |
|---|---|
| `GET /api/videos` | Paginert liste, sortert etter `priority_score DESC` |
| `GET /api/videos/{id}` | Video-detalj inkl. `related[]` (se §Video Suggestions) |
| `GET /api/videos/search?q=` | Full-tekst søk med `ts_rank` |
| `GET /api/videos/trending` | Mest unike seere siste 7 dager |
| `POST /api/events/video` | Logg video-event fra frontend |
| `POST /api/events/search` | Logg søk fra frontend |

- [ ] `VideoController` — GET-endepunkter
- [ ] `EventController` — POST-endepunkter
- [ ] Response-DTO-er (ikke eksponér interne entity-er direkte)

---

### Fase 4 – Frontend-integrasjon

Dette henger på at backend-API-et er oppe.

- [ ] `TanStack Query`-hooks for videos, search, trending
- [ ] Videospiller bygger embed-URL fra `vimeo_id`: `https://player.vimeo.com/video/{vimeo_id}`
- [ ] Videospiller sender events til `POST /api/events/video`
- [ ] UUID genereres i frontend og lagres i `localStorage`

---

## Detaljer & Beslutninger

### Priority Score (beregnes ved import)

| Betingelse | Justering |
|---|---|
| Base | +100 |
| Nyeste konferanseår | +50 |
| 2.-nyeste år | +40 |
| 3.–4. nyeste år | +30 |
| Eldre (per ekstra år) | −10 |
| Ingen video | Importeres ikke |

### Video Suggestions (Related Content)

`GET /api/videos/{id}` returnerer en `related`-liste beregnet i Postgres:

- **Keyword-overlapp**: `keywords && ?::text[]` array-operator
- **Samme foredragsholder**: `EXISTS` subquery på `video_speakers JOIN speakers`
- Ingen ekstern ML — ren SQL, ekstremt rask.

### User Tracking (Video Events)

Frontend sender `POST /api/events/video` ved:

| Event | Trigger |
|---|---|
| `play` | Bruker trykker play |
| `pause` | Bruker pauser |
| `progress` | Hvert 30s mens man ser (frontend-timer) |
| `ended` | Video ferdig |
| `seek` | Bruker hopper til posisjon |

Feltet `playback_position_seconds` gir oss: completion rate, gjennomsnittlig seertid, drop-off-punkter.

### Anonym bruker-identitet

Hvert nettleservindu genererer et UUID lagret i `localStorage`. Ingen innlogging nødvendig. Brukes til:
- Deduplisering av views (`unique_viewers`)
- Fremtidig personalisering («fortsett å se»)

### Databaseskjema (nøkkeltabeller)

| Tabell | Formål |
|---|---|
| `conferences` | Én rad per JavaZone-år |
| `videos` | Alle foredrag med `vimeo_id`, FTS `search_vector`, `keywords text[]`, `priority_score` |
| `speakers` | Kanonisk speaker-identitet. PK = UUID fra lowercased navn — stabil på tvers av re-import. GIN-indeks på `name`. |
| `video_speakers` | Join-tabell. Lagrer video-spesifikk bio + `bio_search_vector`. PK = `(video_id, speaker_id)`. |
| `video_events` | Hvert play/pause/ended/seek-event |
| `search_logs` | Hvert søk for analytics |

Viewet `video_view_stats` forhåndsaggregerer view-teller per video.

#### Hvorfor bio ligger på `video_speakers`, ikke `speakers`

En foredragsholder kan presentere på flere JavaZone-konferanser. Bio endres fra år til år. Ved å lagre bio på join-tabellen får vi:
- Én kanonisk `speakers`-rad per person (deduplisert på navn)
- Fersk bio per (video, speaker)-par
- Bio er søkbar uavhengig via `bio_search_vector`

#### Hvordan `search_vector` bygges

`videos.search_vector` er en vanlig kolonne (ikke `GENERATED ALWAYS`) fordi den trenger data fra joined tabeller. Importøren gjenoppbygger den etter innsetting av speakers:
```
A  = title
B  = keywords + speaker-navn
C  = speaker-bio + abstract + intended_audience
```
Søk treffer `videos.search_vector` ELLER `video_speakers.bio_search_vector` (via EXISTS-subquery).