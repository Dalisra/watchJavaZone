# Backend Implementation – Watch JavaZone

## Real API Facts (confirmed from live data)

- `GET /public/allSessions` → list of all 21 conferences (2006–2026) in chronological order
- `GET /public/allSessions/{slug}` → sessions for that year
- `video` field = bare Vimeo ID (`"1115460917"`) — handle full URLs too for robustness
- `language` field = `"en"` or `"no"` — already present, no AI detection needed
- Records without `video` (missing or empty string) are **skipped**

---

## Proposed Changes

### Dependencies

#### [MODIFY] [build.gradle](file:///Users/vytautas/Documents/javaBin/watchJavaZone/backend/build.gradle)
- `compileOnly("io.micronaut:micronaut-http-client")` → `implementation(...)`

---

### Flyway Migration

#### [NEW] V1__initial.sql — one file, all DDL in dependency order

```sql
CREATE TABLE conferences (
  id    UUID PRIMARY KEY,    -- SleepingPill conferenceId
  slug  TEXT NOT NULL UNIQUE,
  name  TEXT NOT NULL,
  year  INT  NOT NULL
);

CREATE TABLE speakers (
  id         UUID PRIMARY KEY,  -- UUID.nameUUIDFromBytes(name.toLowerCase())
  name       TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX speakers_name_gin ON speakers USING GIN (to_tsvector('simple', name));

CREATE TABLE videos (
  id                UUID PRIMARY KEY,    -- SleepingPill session id
  conference_id     UUID NOT NULL REFERENCES conferences(id),
  title             TEXT NOT NULL,
  abstract          TEXT,
  intended_audience TEXT,
  vimeo_id          TEXT NOT NULL,
  language          TEXT,                -- "en" or "no"
  format            TEXT,
  length_minutes    INT,
  room              TEXT,
  start_time        TIMESTAMPTZ,
  end_time          TIMESTAMPTZ,
  keywords          TEXT[],              -- from suggestedKeywords CSV
  ai_keywords       TEXT[],             -- AI-generated extra keywords (both languages)
  title_en          TEXT,               -- AI translation if original is "no"
  abstract_en       TEXT,
  title_no          TEXT,               -- AI translation if original is "en"
  abstract_no       TEXT,
  search_vector_en  TSVECTOR,           -- English FTS index
  search_vector_no  TSVECTOR,           -- Norwegian FTS index
  base_score        INT NOT NULL DEFAULT 0,
  view_boost        INT NOT NULL DEFAULT 0,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX videos_score_idx ON videos ((base_score + view_boost) DESC);
CREATE INDEX videos_search_en_idx ON videos USING GIN (search_vector_en);
CREATE INDEX videos_search_no_idx ON videos USING GIN (search_vector_no);

CREATE TABLE video_speakers (
  video_id          UUID NOT NULL REFERENCES videos(id) ON DELETE CASCADE,
  speaker_id        UUID NOT NULL REFERENCES speakers(id),
  bio               TEXT,
  bio_search_vector TSVECTOR GENERATED ALWAYS AS
                    (to_tsvector('simple', coalesce(bio, ''))) STORED,
  PRIMARY KEY (video_id, speaker_id)
);
CREATE INDEX video_speakers_bio_idx ON video_speakers USING GIN (bio_search_vector);

CREATE TYPE video_event_type AS ENUM ('play','pause','progress','ended','seek');
CREATE TABLE video_events (
  id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  video_id               UUID NOT NULL REFERENCES videos(id),
  user_uuid              UUID NOT NULL,
  event_type             video_event_type NOT NULL,
  playback_position_secs INT,
  created_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX video_events_video_id_idx ON video_events(video_id);
CREATE INDEX video_events_created_at_idx ON video_events(created_at);

CREATE TABLE search_logs (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  query         TEXT NOT NULL,
  results_count INT,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- video_view_stats: aggregates video_events data. No extra table needed.
-- unique_viewers = distinct user_uuids who sent a 'play' event (all time)
-- viewers_last_7d = same, but only events from last 7 days
-- Used by: GET /api/videos/trending
CREATE VIEW video_view_stats AS
SELECT
  video_id,
  COUNT(DISTINCT user_uuid) AS unique_viewers,
  COUNT(DISTINCT CASE WHEN created_at >= now() - interval '7 days'
        THEN user_uuid END) AS viewers_last_7d
FROM video_events
WHERE event_type = 'play'
GROUP BY video_id;
```

---

### AI Enrichment

Called once per video during import. Input and output are pure JSON — we don't care what the video's original language is.

**Input sent to AI** (one JSON object per video):
```json
{
  "title": "...",
  "abstract": "...",
  "intendedAudience": "...",
  "suggestedKeywords": "experience report, migration",
  "speakerBios": ["Bio of speaker 1", "Bio of speaker 2"]
}
```

**Expected response** — two objects in one reply:
```json
{
  "english": {
    "title": "...",
    "abstract": "...",
    "intendedAudience": "...",
    "aiKeywords": ["cloud migration", "zero downtime", ...]  // extra suggestions only
  },
  "norwegian": {
    "title": "...",
    "abstract": "...",
    "intendedAudience": "...",
    "aiKeywords": ["skymigrasjon", "nullnedetid", ...]  // extra suggestions only
  }
}
```

> `suggestedKeywords` from SleepingPill are parsed and stored separately in the `keywords[]` column — always preserved. `ai_keywords[]` is a distinct column with purely additive AI suggestions. Both are used when building `search_vector_en` / `search_vector_no`.

Whatever the AI returns is stored as-is. Speaker bios go into `video_speakers` (not translated separately — bio search uses the `bio_search_vector` on that table, which is rebuilt from the English + Norwegian content via `simple` dictionary).

**AI provider**: Google Gemini (`GOOGLE_API_KEY` env var). Called via plain Micronaut HTTP client. Not an annotation processor — a plain service class.

---

### Search — Full-Text Search (FTS)

**FTS** = PostgreSQL's built-in full-text search engine. It works by:
1. **Indexing**: converting text into `TSVECTOR` — a sorted list of normalised word stems ("running" → "run"). Done at import time.
2. **Querying**: converting a search phrase into `TSQUERY` — `to_tsquery('english', 'kotlin')`. Postgres then matches against the index extremely fast.
3. **Ranking**: `ts_rank(search_vector_en, query)` gives each result a relevance score.

No Elasticsearch or separate search service needed. This is native to Postgres and works well at our scale.

**Two vectors, frontend controls language:**
```
GET /api/videos/search?q=kotlin&lang=en  → searches search_vector_en
GET /api/videos/search?q=kotlin&lang=no  → searches search_vector_no
GET /api/videos/search?q=kotlin           → OR across both
```

**search_vector_en built from:**
- `A` (highest weight) = `title` (if EN) or `title_en` (AI translation)
- `B` = `ai_keywords` + speaker names
- `C` = `abstract` (if EN) or `abstract_en` + `intended_audience`

**search_vector_no built from** same pattern with NO variants.

---

### Java Sources

#### [NEW] util/VimeoUtils.java
```java
public static String extractId(String raw) {
    if (raw == null || raw.isBlank()) return null;
    return raw.trim().replaceAll(".*/", "").replaceAll("[^0-9].*", "");
}
```

#### [NEW] importer/dto/SleepingPillSession.java
Fields: `id`, `conferenceId`, `title`, `abstractText`, `intendedAudience`, `suggestedKeywords`, `language`, `format`, `length`, `room`, `video`, `startTimeZulu`, `endTimeZulu`, `speakers`

#### [NEW] importer/SleepingPillClient.java
Declarative `@Client("https://sleepingpill.javazone.no")`:
```
GET /public/allSessions        → List<ConferenceRef>
GET /public/allSessions/{slug} → SleepingPillResponse
```

#### [NEW] ai/AiEnrichmentService.java
Calls OpenAI, returns `EnrichedVideo { aiKeywords, titleEn, abstractEn, titleNo, abstractNo }`.
Skips AI call if both translations already exist in DB (avoid re-paying on re-import of unchanged video).

#### [NEW] importer/VideoImporter.java

**Import flow** (triggered only by `POST /api/import`):
1. Fetch conference list from `/public/allSessions` → sort by year → compute `base_score`
   - `base_score = max(0, 150 - (rank - 1) * 10)` (rank 1 = newest)
2. For each conference slug, fetch sessions
3. Filter out sessions without video
4. Normalise `vimeo_id` via `VimeoUtils.extractId()`
5. UPSERT `conferences`
6. For each video:
   - UPSERT speakers + video_speakers
   - Call AI enrichment (skips if translations already stored)
   - UPSERT `videos` (including `base_score`, `ai_keywords`, translated fields)
7. Rebuild `search_vector_en` and `search_vector_no` per conference (bulk SQL)
8. Update `view_boost` for all videos in one query from `video_view_stats`

**video UPSERT:**
```sql
INSERT INTO videos (..., base_score, ai_keywords, title_en, abstract_en, title_no, abstract_no)
VALUES (...)
ON CONFLICT (id) DO UPDATE SET
  title=EXCLUDED.title, abstract=EXCLUDED.abstract, ...,
  base_score=EXCLUDED.base_score,
  ai_keywords=EXCLUDED.ai_keywords,
  title_en=EXCLUDED.title_en, abstract_en=EXCLUDED.abstract_en,
  title_no=EXCLUDED.title_no, abstract_no=EXCLUDED.abstract_no,
  updated_at=now()
  -- view_boost updated separately in step 8, not here
```

**view_boost update** (step 8 — after import, same request):
```sql
UPDATE videos SET view_boost = (
  SELECT LEAST(coalesce(viewers_last_7d, 0) * 2, 100)
  FROM video_view_stats WHERE video_id = videos.id
)
```

#### [NEW] controller/ImportController.java
```
POST /api/import → triggers VideoImporter.importAll() synchronously, returns summary JSON
```
> No scheduled jobs. Import is expensive (AI calls per video) so it runs only on demand.

#### [NEW] controller/VideoController.java
- `GET /api/videos` — paginated, `ORDER BY (base_score + view_boost) DESC`
- `GET /api/videos/{id}` — detail + related videos (keyword + speaker overlap)
- `GET /api/videos/search?q=&lang=` — FTS via `search_vector_en` or `search_vector_no`
- `GET /api/videos/trending` — from `video_view_stats`, ordered by `viewers_last_7d DESC`

#### [NEW] controller/EventController.java
- `POST /api/events/video` — insert into `video_events`
- `POST /api/events/search` — insert into `search_logs`

---

## Verification Plan

### Unit tests
```bash
cd backend && ./gradlew test
```
`VimeoUtilsTest` — bare ID, full vimeo.com URL, full player URL, null, empty string

### Manual
1. Set env: `DATASOURCES_DEFAULT_URL`, `OPENAI_API_KEY`
2. `./gradlew run` → `Successfully applied 1 migrations`
3. `curl -X POST localhost:8080/api/import` → wait, check logs for import count
4. `curl localhost:8080/api/videos | jq '.[0]'`
5. `curl "localhost:8080/api/videos/search?q=kotlin&lang=en" | jq length`
