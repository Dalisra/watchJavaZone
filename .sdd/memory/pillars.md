# The 6 Pillars — Deep Reference

**Summary:** See `sdd-context` skill or `MEMORY.md` for quick routing.
**This file:** Full implementation detail for each pillar.

---

## Pillar 1: Intelligent Context

**Core idea:** Teach AI your codebase DNA so it stops guessing.

### What Goes Where
- **CLAUDE.md:** Navigation hub. What is this? Where do I start? Under 200 lines for a library.
- **Skills:** Domain detail. How exactly do I do X in THIS codebase?
- **Common skills:** Universal methodology. Works across all projects.
- **Memory files (this system):** Persistent learning and routing across sessions.

### Creating CLAUDE.md

**Primary path:** Run `claude init` in your project root. Claude reads your codebase and generates an initial CLAUDE.md from what it finds. It catches things you'd forget to mention — build commands, conventions visible in the code, directory structure.

**What `claude init` misses:** Tacit knowledge. Why decisions were made. The gotchas. The "never do X" rules. The things that aren't in the code. Add those yourself.

**Alternative paths:** Ask Claude to draft it ("draft a CLAUDE.md for this project"), or fill in `templates/CLAUDE.md` from the distribution.

### What CLAUDE.md Must Contain
1. One-sentence project description
2. Build + test command (first thing, always)
3. Key conventions (naming, structure, patterns)
4. What NOT to do (the "never" rules)
5. Pointer to `.claude/skills/` for domain skills

### What CLAUDE.md Must NOT Contain
- Full API documentation (put in skills)
- Session learnings / status updates (put in LEARNINGS.md)
- Step-by-step guides (put in skills)
- Anything over 300 lines (split into skills)

### Skill Routing Layers
```
~/.claude/skills/common/   → Active every session, every project (methodology)
~/.claude/skills/          → Company-wide context
.claude/skills/            → This project only (domain knowledge)
```

### Health Check
Run `context-engineering-audit` skill. Target: CLAUDE.md < 200 lines, skills in right layer.

---

## Pillar 2: Strategic Delegation

**Core idea:** Not every task needs the most powerful model. Match cost to complexity.

### Model Selection Guide
| Task Type | Model | Why |
|-----------|-------|-----|
| Boilerplate, repetitive patterns | Haiku | 12x cheaper, fast |
| Unit tests for clear logic | Haiku | Straightforward pattern matching |
| Complex business logic | Sonnet | Needs reasoning |
| Architecture decisions | Sonnet/Opus | High stakes, needs depth |
| Novel problem solving | Opus | Worth the cost |
| Code review of critical paths | Sonnet | Balance speed/quality |

### The 60/70 Rule
Haiku handles 60-70% of typical development tasks adequately.
If Haiku struggles after one retry → escalate to Sonnet.
Never use model selection as an excuse to skip verification.

### Cost Awareness
- Track costs in MAX subscription vs per-token billing
- Expensive model + no verification = worst of both worlds
- Cheap model + thorough verification = often better than the reverse

---

## Pillar 3: Trust But Verify

**Core idea:** AI will hallucinate. Build systems to catch it before it reaches production.

### The Six Fears (from `fear-driven-development` skill)
1. **AI is lying and I can't tell** → Round-trip tests, verify serialization/deserialization
2. **Shipping bugs to production** → Pre-commit checklist, automated test gates
3. **Generating bad code at speed** → Code review ritual, battle testing
4. **Cost spiral** → MAX subscription, model selection discipline
5. **Losing control of the codebase** → PR-only workflow, no direct commits
6. **Silent failures** → Extreme measurement, monitoring, observability

### Verification Hierarchy (from `verification-patterns` skill)
1. Format compliance: Does it parse/compile?
2. Data integrity: Does it produce correct output?
3. Error handling: Does it fail gracefully?
4. Edge cases: Boundary conditions, nulls, empty collections
5. Integration: Does it work with the real system?
6. Acceptance: Does it meet the actual requirement?

### The Round-Trip Test Pattern
```
Write → Serialize → Deserialize → Assert equals original
```
Catches: wrong field types, missing fields, encoding issues, data loss.
When to use: any persistence layer, any format conversion.

### TDD with AI
1. Write the test (you define the contract)
2. Ask Claude to make it pass
3. Verify the implementation makes sense
4. Refactor if needed (tests still pass)
Never accept "tests pass" as the only verification criterion.

---

## Pillar 4: Directed Synthesis

**Core idea:** YOU are the conductor. Claude is the orchestra. Direct it; don't follow it.

### The Conductor Principle
- AI proposes → you evaluate → you decide
- Never let architecture emerge from AI suggestions alone
- Triangulate: get multiple perspectives, you synthesize
- Your judgment is the irreplaceable ingredient

### Multi-Tool Orchestration
```
Claude → generates raw content (code, analysis, text)
NotebookLM → synthesizes across sources, finds patterns
Claude → refines based on synthesis insights
You → decide what's real, what matters, what ships
```

### When to Override AI
- When it suggests an approach that contradicts your domain knowledge
- When it produces something that "works" but feels wrong
- When it's optimizing for what it knows vs what your situation needs
- Always: when it affects architecture or public API

### Directed Synthesis Prompts
- "Generate 3 different approaches to this problem" (not: "solve this")
- "What are the weaknesses of this approach?" (after it proposes something)
- "What would a senior Java architect say about this design?" (perspective shift)
- "What am I not considering?" (gap finding)

---

## Pillar 5: Process Discipline

**Core idea:** Guardrails are what make velocity sustainable. Speed without discipline creates debt.

### Non-Negotiable Rules
1. **No direct commits to main** — ever, even "small fixes"
2. **Tests must pass before any commit** — failing tests = no commit
3. **One logical change per commit** — easier to revert, easier to review
4. **PR for every change** — even solo developers benefit from the review ritual

### Commit Format
```
type: short description (imperative, no period)

Types: feat, fix, docs, refactor, test, chore, style
Examples:
  feat: add email validation to user registration
  fix: resolve null pointer in payment processing
  docs: update API documentation for /orders endpoint
  refactor: extract payment logic into PaymentService
```

### Branch Naming
```
feature/short-description
fix/issue-description
docs/what-you-documented
refactor/what-you-refactored
```

### PR Structure
- Title: What changed (imperative, under 70 chars)
- Body: Why it changed + test plan + notes for reviewer
- Always: link to issue/ticket if one exists

### The Pre-Commit Ritual
1. `git diff` — review every line you're about to commit
2. `[test command]` — tests pass
3. `git status` — no accidental files
4. Commit message follows format
5. Push + create PR

---

## Pillar 6: Continuous Learning

**Core idea:** Every bug fixed updates a skill. The system gets smarter every session.

### The Learning Capture Loop
```
Encounter a problem
  → Solve it
    → Ask: "Why did this happen?"
      → Answer: write in LEARNINGS.md
        → Ask: "Should this be a skill?"
          → Yes: write/update skill
          → No: leave in LEARNINGS.md
```

### When to Update a Skill
- Claude gave wrong guidance → fix the skill immediately
- A pattern appeared that isn't in any skill → write a new one
- A skill's file references are outdated → update them
- A skill is never used → delete it (deletion = health, not failure)

### The Quarterly Audit (30 minutes)
For each skill in `.claude/skills/`:
1. When was it last used? (Never in 3 months → delete)
2. Is the guidance still accurate? (Code changed → update)
3. Is it still needed? (Claude handles it natively now → delete)
4. Does another developer understand it? (Fails the new-person test → rewrite)

### Promotion Path
When a domain skill appears in 2+ projects AND is language-agnostic:
→ Promote to `~/.claude/skills/common/`

### The New Person Test
Best stale-skill detector: a new developer who follows instructions literally.
If a skill leads them astray → the skill is stale. Fix it.
If they can onboard using only CLAUDE.md + skills → your infrastructure is healthy.
