# Watch JavaZone - Implementation Plan

## Goal Description
The objective is to build a YouTube-like platform ("Watch JavaZone") to make JavaZone presentations highly accessible and searchable. The platform will index sessions from the existing sleepingpill API, embed Vimeo videos, track user metrics, and provide recommendations.

## JSON API Analysis (`javazone_2025`)
The JSON response from `https://sleepingpill.javazone.no/public/allSessions/javazone_2025` provides a rich set of metadata under the `sessions` array. Key fields for our application include:
- `title` & `abstract`: The core text for full-text search.
- `suggestedKeywords`: An array (comma-separated string in JSON) of keywords useful for faceted search, tagging, and recommendations.
- `speakers`: Contains `name` and `bio`, crucial for searching by speaker.
- `video`: Contains the Vimeo ID (e.g., `1115460917`) which we'll use to construct embed URLs: `https://player.vimeo.com/video/{video_id}`.
- `language`, `format`, `length`: Useful for filtering (e.g., "Show me 45 min presentations in English").
- `sessionId` & `conferenceId`: Primary keys for deduplication and relations.

## Technology Stack

### Frontend
**React with Vite and TailwindCSS**
The application is scaffolded using `vite` to enable fast development loops.
- **Styling**: Tailwind CSS v4 is configured (`@tailwindcss/vite` plugin).
- **Data Fetching**: `TanStack Query` (`@tanstack/react-query`) is configured in the root to manage caching and fetching API data efficiently.
- **Routing**: `TanStack Router` (`@tanstack/react-router`) is set up using the Vite plugin for file-based routing. The `src/main.tsx` initializes the `RouterProvider` and the root layout (`__root.tsx`).

### Backend
**Micronaut (Java, Gradle)**
An initial baseline project was downloaded from `launch.micronaut.io` with JDK 21 and the following features included:
- `data-jdbc`: For data access.
- `flyway`: For database migrations.
- `postgres` & `jdbc-hikari`: For connection to PostgreSQL.
- `validation`, `yaml`, `reactor`: Standard baseline dependencies.

### Database
**PostgreSQL (via Supabase)**. Relational data is perfect here, and PostgreSQL has powerful features that solve our search problem without needing an external search engine. Local hosting for development.

## Search Implementation (PostgreSQL FTS)
Instead of setting up a complex Elasticsearch cluster, we can fully leverage **PostgreSQL Full-Text Search (FTS)** which is highly capable and native to Supabase.

1.  **Data Ingestion**: A Micronaut cron job or startup hook fetches data from the sleepingpill API and upserts it into our `sessions` table in Postgres.
2.  **Search Vector**: We create a generated `tsvector` column in Postgres that concatenates `title` (weight A), `abstract` (weight B), `speakers.name` (weight B), and `suggestedKeywords` (weight C). 
3.  **Search API**: The Micronaut backend exposes `/api/sessions/search?q={query}`. This translates to a Postgres `@@ to_tsquery()` call, returning results ranked by relevance (`ts_rank`).
4.  **Filters**: We can add standard SQL `WHERE` clauses for `language` or `format`.

## Tracking and Analytics
To achieve requirement #2 (track searches, views, popular topics) and #3 (recommendations):

1.  **Tracking Views**: Create a `video_views` table (`session_id`, `anonymous_user_id`, `timestamp`). We can track when a user clicks play.
2.  **Tracking Searches**: Create a `search_logs` table (`search_term`, `timestamp`, `results_count`) to analyze what people are looking for.
3.  **Recommendations v1 (Content-Based)**: 
    *   Find videos with overlapping `suggestedKeywords`.
    *   Find videos by the same `speakers`.
    *   Using Postgres, this is a simple query querying for tags intersection.
4.  **Recommendations v2 (Behavioral)**:
    *   Find trending videos based on the `video_views` table (e.g., most views in the last 7 days).
