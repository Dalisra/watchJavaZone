# SDD Experience — The Proof

**Purpose:** The story behind SDD. Where it came from, what was learned, why it works.
**Use when:** Explaining SDD to skeptics, writing content, grounding methodology claims.

---

## The lib-pcb Story

**January 16–27, 2026. Oslo, Norway.**

The goal: build a professional PCB (Printed Circuit Board) design library from scratch. Parse industry-standard formats (Gerber, Altium, MIF), validate designs against manufacturing rules, auto-fix common errors.

Industry standard for this scope: **10–18 months** with an experienced team.

**What actually happened:**
- 197,831 lines of Java code
- 7,461 tests
- 99.8% test pass rate
- Manufacturing-ready Gerber exports
- **11 days**

That's 25–66x faster than industry standard.

### How It Was Built

Not by typing faster. By building skill infrastructure that compounded daily.

**Day 1–2:** Basic structure. First 5 skills. Claude kept getting MIF format wrong.
→ Wrote `mif-format-basics` skill. Problem disappeared.

**Day 3–5:** Validators. Each new validator type required explaining the same patterns.
→ Wrote 3 validator skills. Each new validator took 20 minutes, not 2 hours.

**Day 6–8:** Gerber import. Complex format, many edge cases.
→ Skills for each Gerber layer type. Round-trip tests caught 12 hallucinations before they reached production.

**Day 9–11:** Auto-fix, integration, manufacturing validation.
→ 85 skills total. Claude was operating with full project context at all times.

**The compounding effect:**
- Day 1: 5 skills, 2 hours to explain context each session
- Day 5: 30 skills, 15 minutes to explain context (skills handle it)
- Day 11: 85 skills, Claude reads context automatically, zero explanation needed

### What Was Learned

1. **Skills compound.** Each skill makes the next feature faster. The 11th day was 4x faster than the 1st.
2. **Round-trip tests are essential.** Format conversion without round-trip testing is gambling.
3. **Fear is productive.** The six fears drove the six systems that prevented six categories of failure.
4. **Deletion is health.** Several skills were deleted when Claude Code improved. Smaller, accurate library > larger, stale one.

---

## The Synthesis Story

**February 14–20, 2026. Oslo, Norway.**

The goal: build local-first knowledge infrastructure from scratch. Index code, docs, PDFs, and videos across a workspace. Make everything sub-second searchable. Track how files move and relate across repositories.

This was a harder problem than lib-pcb in one specific way: the tool needed to work on itself.

**What actually happened:**
- 84,692 lines of code (53,083 production)
- 2,751 tests
- 255 commits — 73 PRs merged — 45 releases
- 65% Claude co-authorship (92 of 141 substantive commits)
- **7 days**

No industry baseline exists for this kind of tool. The comparison that matters: lib-pcb took 11 days with an experienced team and 85 domain skills. Synthesis took 7 days, starting from zero, on an entirely different problem domain.

### How It Was Built

The lib-pcb skills didn't transfer directly — the domain was different. But the *method* transferred completely.

**Day 1–2:** Core indexing pipeline. Skills for the file processing model, the SQLite schema, the CLI conventions. Each session started with context already loaded.

**Day 3–4:** Search and relationship graphs. Cross-repo dependency tracking required encoding the graph traversal patterns once. After that, Claude generated correct graph queries without explanation.

**Day 5–6:** File movement tracking. The hardest feature — hash-based detection, 7-day safety periods, audit trails. Skills encoded the detection model so Claude could reason about edge cases correctly.

**Day 7:** Integration, polish, 2,751 tests passing.

**The process discipline was extreme:** 73 PRs in 7 days = one PR every ~2.3 hours. Every change went through a pull request. The PR-only workflow wasn't bureaucracy — it was the mechanism that let Claude participate fully in each discrete unit of work without context bleed between features.

### The Recursive Validation

The most unusual moment: on Day 6, Synthesis indexed its own development history.

It found its own commit patterns, its own file movement graph, its own dependency structure. The benchmark results — 0.4s search across 8,934 files, 92–95% retrieval improvement — were *validated by Synthesis itself* analyzing its own performance during development.

A tool proving its own value by applying itself to itself. That's a different kind of proof than lib-pcb.

### The Feb 19 Pivot

On Day 6, the project's understanding of itself changed.

The original framing: Synthesis is a *retrieval* tool. Faster search, less time hunting for context.

The new framing: Synthesis is a *knowledge integrity* system. Files don't just move — they evolve. When a file disappears, the knowledge in it doesn't. The audit trail tracks where it went and why. The relationship graph knows what depended on it.

This pivot happened because the tool was being used on itself. The self-referential validation revealed something the original design spec didn't anticipate: retrieval is a symptom. The disease is knowledge that escapes your awareness.

### What Was Learned

1. **The method is domain-agnostic.** lib-pcb → PCB design. Synthesis → knowledge infrastructure. Same six pillars, different skills, same compounding effect.
2. **Process discipline is a force multiplier.** 73 PRs in 7 days wasn't overhead — it was how 65% Claude co-authorship became possible without hallucinations reaching production.
3. **Self-referential validation is the highest proof.** If a knowledge tool can analyze itself and find its own value, the claim is proved in a way no external benchmark can match.
4. **The pivot happens when you use the tool.** The retrieval → knowledge integrity reframe emerged from Synthesis indexing its own development history. You can't design your way to that insight. You have to build it.

### lib-pcb vs Synthesis

| | lib-pcb | Synthesis |
|--|---------|-----------|
| Duration | 11 days | 7 days |
| Production LOC | 197,831 | 53,083 |
| Tests | 7,461 (99.8%) | 2,751 |
| PRs | — | 73 |
| Claude co-authorship | — | 65% |
| Domain | PCB design | Knowledge infrastructure |
| Proof type | Speed | Self-awareness |

**lib-pcb demonstrated that AI-assisted development is fast.**
**Synthesis demonstrated that AI-assisted development can be self-aware.**

Together: SDD works across domains. The skills change. The method doesn't.

---

## Conservative Claims — What the Evidence Actually Supports

**Proven (use freely):**
- lib-pcb: 25–66x faster than industry standard (11 days vs 10–18 months)
- Synthesis: 84,692 LOC in 7 days, self-validated, 65% Claude co-authorship — same method, different domain
- 10–30x productivity improvement is typical for teams applying SDD properly
- Domain transferability: Manufacturing, Renewable Energy, Financial Services, AI Security, Knowledge Infrastructure

**Not proven (don't claim):**
- "320x productivity" — this is a mathematical upper bound, not a typical result
- Specific ROI numbers for any given company without doing the analysis
- "Anyone can achieve 66x" — the upper bound required deep domain skill investment

**The right framing:**
> "We achieved 25–66x on lib-pcb. In practice, teams applying SDD see 10–30x improvement — that's what we've observed across engagements. Individual results depend on skill investment, domain complexity, and team discipline."

---

## Validation Across Sectors

| Sector | Client | Outcome |
|--------|--------|---------|
| Manufacturing | lib-pcb (internal) | 197K LOC, 11 days, 25-66x |
| Knowledge Infrastructure | Synthesis (internal) | 84K LOC, 7 days, self-validated |
| Renewable Energy | Tvimenning AS | 40K NOK contract, 190-230K partnership |
| Financial Services | SpareBank 1 Utvikling | Org-wide Claude Code rollout |
| AI Security | Mynder AS | Strategic retainer discussions |
| Consulting | Item Consulting | 150K NOK corporate workshop |

The methodology transfers across domains because the core principle — encode human expertise as skills — is domain-agnostic.

---

## The Bottleneck Shift

Before SDD: creation was the bottleneck. Writing code was slow.
After SDD: creation is 10x faster. **Absorption became the new bottleneck.**

> "Creating 8,000+ files in 11 days is great. Finding the right file when you need it? Still takes 15 minutes."

lib-pcb generated 691 files per day at peak. Even with 85 skills encoding the patterns, *finding* the right context before asking Claude became work. That problem doesn't have a skill solution — it needs infrastructure.

**SDD solves the creation bottleneck.**
**Synthesis solves the absorption bottleneck.**
Together: sustainable velocity.

---

## Synthesis — When SDD Isn't Enough

**What it is:** Local-first knowledge infrastructure from eXOReaction. Indexes everything — code, docs, PDFs, videos — and makes it sub-second searchable with relationship graphs.

**Where it came from:** Built by eXOReaction directly from the lib-pcb experience. When 8,934 files exist across a workspace, `grep` and manual navigation break down.

**Key metrics (validated Feb 14, 2026):**
- 8,934 files indexed across 3 workspaces
- Sub-second search (0.4s validated)
- 92–95% reduction in retrieval time (5–15 min → 10–30 sec)
- 2.7% storage overhead
- Cross-repo dependency graphs (58 repos, 429 dependencies in <31 seconds)

**The relationship to SDD:**
- SDD: what Claude knows (skills, CLAUDE.md)
- Synthesis: how Claude finds it (index, search, relationships)

SDD without Synthesis works until output volume grows beyond manual navigation — typically 50+ skills across multiple projects, or teams generating hundreds of files per week.

**When you need it:** When you spend more than a few minutes searching for context before you can ask the real question. That's the signal.

**Built by:** eXOReaction AS, Oslo, Norway

---

## Key People

| Person | Role | Connection |
|--------|------|-----------|
| Thor Henning Hetland (Totto) | Founder eXOReaction, created SDD | lib-pcb builder, methodology author |
| Vidar Moe | SpareBank 1 Utvikling | Coined "Skill-Driven Development", Feb 3 2026 |
| Jon Petter Hjulstad | Tvimenning AS | First external client, closed Feb 2026 |
| Ketil Hjerpaasen | Item Consulting | Corporate adoption, 150K NOK |

---

## Why This Matters Now

Adoption curve (estimated):
- **Today (2026):** ~5% of developers using AI-augmented workflows systematically
- **12 months:** ~30% (window closing)
- **24 months:** ~60% (table stakes)
- **36 months:** ~90% (bootcamps teach it)

**The first-mover window is 12–18 months.** Teams that build SDD infrastructure now will have compounding skill libraries while others are still installing Claude Code for the first time.

The competitive moat: a mature skill library (50–100 domain skills) represents months of captured institutional knowledge. It cannot be replicated quickly by a team starting from zero.
