# Watch JavaZone

YouTube-like platform for JavaZone conference videos - searchable, trackable, discoverable.

**Type:** Full-stack web app
**Languages:** Java 21 (Micronaut), TypeScript (React)
**Build:** Backend: `./gradlew test` | Frontend: `npm test`

---

## Quick Start

```bash
# Backend (Micronaut + Postgres)
cd backend
# Requires local Postgres running: jdbc:postgresql://localhost:5432/postgres (schema: watch)
./gradlew run          # http://localhost:8080

# Frontend (React + Vite)
cd frontend
npm install && npm run dev  # http://localhost:5173
```

---

## Architecture

```
backend/               # Micronaut 4.10 (Java 21, Gradle)
  src/main/java/backend/
  src/main/resources/  # application.yml, migrations

frontend/              # React 19 + Vite + TailwindCSS
  src/routes/          # TanStack Router (file-based)
  src/                 # Components, hooks, utils

docs/                  # Implementation plans, ADRs
```

---

## Key Conventions

**Naming:**
- REST endpoints: `/api/resource-name` (kebab-case)
- Java: PascalCase classes, camelCase methods
- TypeScript: PascalCase components, camelCase functions

**Patterns:**
- Dependency Injection: Constructor injection ONLY. Never use `@Inject` on fields.
- Database access: Micronaut Data repositories only
- Database provisioning: Local database (avoid volatile TestResources to save AI API credits via persistence)
- Search: PostgreSQL FTS (no Elasticsearch)
- Video: Vimeo embeds (no custom hosting)
- Config: application.yml (no hardcoded values)
- API client: sleepingpill.javazone.no (read-only)

---

## What NOT To Do

- Never cheat/skip/neuter tests - flag if tests block valid implementation
- Never pollute project root with AI-generated files (use /tmp/, docs/, target/)
- Never use `mv` for git-tracked files (use `git mv` to preserve history)
- Never over-engineer - wait for 3+ use cases before abstracting
- Never exceed 3-4 git operations per task (branch → commit → push → PR)

---

## Context Management Protocol

**At 90% context window:**
1. Persist new knowledge to `.claude/skills/watch-javazone-project.yaml`
2. Verify alignment with high-level project goals
3. Update this CLAUDE.md if conventions change

**Drift Detection:**
- Alert if user request diverges from core mission (see skill for goals)
- New directions are valid, but user must be aware of drift
- Tests must align with goals - flag misalignment

**GitHub Issues for Task Persistence:**
- Create issues for large assignments, direction changes, multi-step work
- Use issues to communicate project evolution to participants/LLMs
- Issues serve as long-term task storage beyond context windows
- Check existing issues before starting work to avoid duplication
- Update issue status to track completion and blockers
- Use labels: `enhancement`, `architecture`, `direction-change`, etc.

---

## Skills

**Domain knowledge:** `.claude/skills/watch-javazone-project.yaml`
Core reference for architecture, goals, implementation state, anti-patterns

**Methodology:** `~/.claude/skills/common/`
SDD workflow, TDD, GitHub patterns, documentation standards

---

## Related

- [PROJECT.md](PROJECT.md) - Original project brief (Norwegian)
- [docs/01_implementation_plan.md](docs/01_implementation_plan.md) - Detailed tech plan
- sleepingpill API: https://github.com/javaBin/moresleep
