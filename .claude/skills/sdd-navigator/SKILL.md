---
name: sdd-navigator
description: Problem-first routing to the right SDD skill. Start here when you're not sure which skill to use.
tags: [navigation, sdd, methodology]
status: production-ready
priority: critical
relatedSkills: [sdd-context, skill-routing-decisions]
---

# SDD Navigator

**Start here.** Find the right skill for your situation.

---

## "I'm having trouble with..."

### Context & Setup

| Problem | Skills to Use |
|---------|--------------|
| Claude keeps ignoring my project conventions | `claude-md-guidelines` → improve CLAUDE.md |
| Not sure if my CLAUDE.md is well-structured | `context-engineering-audit` |
| Don't know where to put a new skill | `skill-routing-decisions` |
| Have 10+ skills and it's getting hard to navigate | `skill-navigator-pattern` → build your navigator |
| Starting a new project from scratch | `claude-md-guidelines` + `sdd-context` |

### Writing Skills

| Problem | Skills to Use |
|---------|--------------|
| Don't know what my first skill should be | `sdd-domain-skill-extractor` → Five Fingerprints |
| Know what the skill should do, don't know how to write it | [memory/domain-skills-guide.md](../memory/domain-skills-guide.md) |
| Skill I wrote isn't working | [memory/domain-skills-guide.md](../memory/domain-skills-guide.md) → Acid Test section |
| Skills are getting stale / outdated | `sdd-skill-health-check` |
| Not sure if something should be a skill or go in CLAUDE.md | `claude-md-guidelines` → "What Goes Where" |

### Verification & Quality

| Problem | Skills to Use |
|---------|--------------|
| AI generated something and I'm not sure it's correct | `fear-driven-development` + `verification-patterns` |
| Need to write good tests | `tdd-workflow` + `verification-patterns` |
| Worried about regressions | `regression-prevention` |
| Something broke that was working before | `proactive-bug-hunting` |
| Not sure my code handles edge cases | `verification-patterns` → Six Pillars section |

### Development Workflow

| Problem | Skills to Use |
|---------|--------------|
| Starting a new feature | `starter-feature-workflow` |
| Need to refactor safely | `starter-refactor-safely` |
| Need to understand unfamiliar code | `starter-explain-codebase` |
| Doing a code review | `starter-code-review` |
| Making an architectural decision | `starter-write-adr` |
| Debugging a tricky problem | `starter-debug-systematically` |

### AI Orchestration

| Problem | Skills to Use |
|---------|--------------|
| Feeling like AI is driving instead of me | `directed-synthesis` |
| Not sure which model to use | [memory/pillars.md](../memory/pillars.md) → Pillar 2 |
| Want to orchestrate multiple AI tools | `directed-synthesis` |
| AI keeps going in the wrong direction | `directed-synthesis` → Conductor Principle |

### Process

| Problem | Skills to Use |
|---------|--------------|
| Not sure about commit message format | `github-workflow` |
| How to structure a PR | `github-workflow` |
| Want to improve my development workflow | `github-workflow` + `tdd-workflow` |
| Feeling like I'm losing control of the codebase | `fear-driven-development` → Fear #5 |

### Communication & Documentation

| Problem | Skills to Use |
|---------|--------------|
| Writing a technical document | `writing-patterns` + `documentation-style` |
| Need to communicate something to non-technical stakeholders | `writing-patterns` |
| Organizing project documentation | `docs-organization` |
| Tone feels wrong (too hyped, not professional) | `tone-of-voice` |

### Learning & Improvement

| Problem | Skills to Use |
|---------|--------------|
| Want to capture what I learned today | `docs-organization` + [memory/workshop-learnings.md](../memory/workshop-learnings.md) |
| Skills feel outdated but not sure which ones | `sdd-skill-health-check` |
| Want to understand the full SDD methodology | `sdd-context` + [memory/pillars.md](../memory/pillars.md) |
| Want to explain SDD to a colleague | [memory/experience.md](../memory/experience.md) |

---

## "I want to..."

### Common Tasks

| Task | Start Here |
|------|-----------|
| Add a new feature | `starter-feature-workflow` |
| Fix a bug systematically | `starter-debug-systematically` |
| Refactor without breaking things | `starter-refactor-safely` |
| Review code (own or colleague's) | `starter-code-review` |
| Understand a module I didn't write | `starter-explain-codebase` |
| Document an architecture decision | `starter-write-adr` |
| Write my first domain skill | `sdd-domain-skill-extractor` |
| Check the health of my skill infrastructure | `sdd-skill-health-check` + `context-engineering-audit` |

---

## Multi-Step Workflows

### Starting SDD from Zero
1. Install this distribution (`./install.sh`)
2. Read `sdd-context` → understand the methodology
3. Create CLAUDE.md using `claude-md-guidelines` template
4. Run `sdd-domain-skill-extractor` → write first 3 domain skills
5. Start building — capture learnings in `workshop-learnings.md`
6. After 10 skills: read `skill-navigator-pattern` → build your project navigator

### Setting Up a New Project
1. `claude-md-guidelines` → create CLAUDE.md
2. `skill-routing-decisions` → decide where skills go
3. `sdd-domain-skill-extractor` → identify first 5 skills
4. `github-workflow` → configure commit/PR workflow
5. `tdd-workflow` → establish test discipline

### After Something Goes Wrong
1. `proactive-bug-hunting` → systematic diagnosis
2. Fix the issue
3. `tdd-workflow` → write test to prevent recurrence
4. `fear-driven-development` → which fear does this map to? Update that system.
5. Update relevant skill if the skill gave wrong guidance

### Quarterly Skill Maintenance
1. `sdd-skill-health-check` → run audit
2. Delete stale skills (deletion = health)
3. Update outdated file references
4. `context-engineering-audit` → check CLAUDE.md and skill structure
5. Promote any skills that appear in 2+ projects to `common/`

---

## Quick Lookup by Pillar

| Pillar | Go-To Skills |
|--------|-------------|
| 1: Intelligent Context | `claude-md-guidelines`, `skill-routing-decisions`, `skill-navigator-pattern`, `context-engineering-audit` |
| 2: Strategic Delegation | [pillars.md → Pillar 2](../memory/pillars.md) |
| 3: Trust But Verify | `fear-driven-development`, `verification-patterns`, `tdd-workflow`, `regression-prevention` |
| 4: Directed Synthesis | `directed-synthesis` |
| 5: Process Discipline | `github-workflow`, `tdd-workflow` |
| 6: Continuous Learning | `sdd-skill-health-check`, `context-engineering-audit`, `docs-organization` |
