# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./gradlew build

# Run (dev mode, port 8082)
./gradlew run

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "backend.util.VimeoUtilsTest"
```

Tests use Micronaut Test Resources, which auto-starts a PostgreSQL container via Testcontainers.

## Architecture

**JavaZone video discovery platform** — imports conference talks from SleepingPill, enriches them with Google Gemini AI, stores in PostgreSQL, and exposes a REST API for browsing, searching, and analytics.

### Stack
- **Framework:** Micronaut 4.10.9 (Netty, Java 21)
- **Database:** PostgreSQL with Flyway migrations (`src/main/resources/db/migration/`)
- **ORM:** Micronaut Data JDBC (not JPA/Hibernate)
- **Search:** PostgreSQL full-text search using `TSVECTOR` columns (English + Norwegian analyzers)

### Layered structure under `src/main/java/backend/`

| Package | Role |
|---|---|
| `controller/` | REST endpoints (`/api/videos`, `/api/events`, `/api/import`) |
| `service/` | Business logic (`VideoTrackingService`) |
| `repository/` | Micronaut Data JDBC repositories |
| `domain/` | Entity models (plain Java classes with Micronaut annotations) |
| `importer/` | SleepingPill HTTP client + import orchestration |
| `ai/` | Google Gemini integration for translation and keyword extraction |
| `util/` | Small utilities (e.g., Vimeo ID extraction) |

### Key data flows

**Import pipeline** (`GET /api/import` → `ImportController` → `VideoImporter`):
1. Fetch all conferences + sessions from SleepingPill API
2. Enrich each video via Gemini: translate title/abstract/audience to EN/NO, generate keywords
3. Upsert conferences, speakers, videos, video_speakers into PostgreSQL
4. Rebuild `search_vector_en` / `search_vector_no` TSVECTOR columns

**Video search** (`GET /api/videos/search?q=&lang=`):
- Queries PostgreSQL full-text search vectors; language selects the appropriate vector
- Logs search queries to `search_logs` table asynchronously

**Video events** (`POST /api/events/video`):
- Stores play/pause/progress/seek/ended events in `video_events`
- `view_boost` on videos is computed from unique viewers in last 7 days

### Database schema notes
- Schema: `watch` (set via Flyway `default-schema`)
- `videos.search_vector_en/no` — manually rebuilt by importer using PostgreSQL `to_tsvector()`
- `video_view_stats` — a view computing `unique_viewers` and `viewers_last_7d` from `video_events`
- `videos.base_score` — recency score, `view_boost` — popularity from last-7-day views

### Environment variables
| Variable | Default | Purpose |
|---|---|---|
| `JDBC_DATABASE_URL` | `jdbc:postgresql://localhost:5432/postgres?currentSchema=watch` | DB connection |
| `JDBC_DATABASE_USERNAME` | `postgres` | DB user |
| `JDBC_DATABASE_PASSWORD` | `postgres` | DB password |
| `GOOGLE_API_KEY` | _(empty)_ | Gemini AI enrichment |

### External dependencies
- **SleepingPill** (`sleepingpill.javazone.no`) — source of conference and session data
- **Google Gemini** (`gemini-2.0-flash`) — AI enrichment; requires `GOOGLE_API_KEY`
