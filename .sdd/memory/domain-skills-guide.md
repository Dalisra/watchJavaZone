# Domain Skills Guide — How to Write Skills That Work

**Purpose:** Writing great domain skills. Common mistakes and how to avoid them.
**Use when:** Writing your first skill, reviewing existing skills, teaching skill writing.

---

## What Is a Domain Skill?

A domain skill encodes knowledge that is specific to YOUR project — patterns, conventions, and gotchas that Claude cannot know from training alone.

**Examples of good domain skills:**
- "When adding a REST endpoint in our project, always include our custom ErrorResponse wrapper, register in RouterConfig.java, and add an integration test using our TestContainers setup"
- "Our coordinate system uses nanometers internally, not millimeters. Never use millimeter values in MIF format — always multiply by 1,000,000"
- "The Flyway migration naming convention is V{number}__{description}.sql — the double underscore is mandatory"

**Examples of bad "skills" (too generic):**
- "Write good code following best practices" → this is not a skill, it's noise
- "Always add error handling" → too vague, Claude already knows this
- "Use dependency injection" → generic programming knowledge

**The test:** If it's true for ANY Java project, it's not a domain skill. If it's only true for YOUR project, it is.

---

## Writing a Skill: Ask Claude to Do It

**Don't open the template. Don't write YAML.**

Describe the pattern to Claude in plain language. Claude will write the skill for you.

**The workflow:**

```
1. Start a new Claude session (or continue your current one)
2. Say: "I want to create a skill for [pattern]. Here's what I always
   explain: [describe it in plain language — like you'd explain to a
   new team member]"
3. Claude writes the YAML
4. You read it and correct anything that's wrong
5. Save it to .claude/skills/[skill-name].yaml
6. Acid test (new session, ask Claude to do the thing)
```

**Example:**
> "Create a skill for how we add validators in this project. Validators live in `src/main/java/com/example/validators/`, they extend `AbstractValidator<T>`, every validator needs to implement `validate()` and `getValidatorId()`, and must be registered in `ValidatorRegistry.java` at line 47. The most common mistake is forgetting to register it — Claude always forgets this step."

Claude produces the skill. You review for accuracy. Done in 10 minutes, not 45.

---

## Skill Structure Reference

*(This is what Claude will produce — you don't need to type it yourself.)*

```yaml
---
name: your-skill-name
description: One sentence: what does this help Claude do in your project?
tags: [tag1, tag2]
status: draft
priority: medium
relatedSkills: []
---

# Skill Title

## When to Use This

Use this skill when:
- [Exact trigger phrase — e.g., "user says 'add a validator'"]
- [Situation description — e.g., "working with the validation layer"]

## Quick Start

[What you keep explaining to Claude. Specific:
- Where do files go? (exact path)
- What naming convention? (exact pattern)
- What must every implementation include? (list it)
- What is NEVER allowed? (list the gotchas)]

## Related Skills

- [skill-name] — [why relevant]
```

**Grow it later:** Add Troubleshooting, Examples, Core Concepts as the skill matures and you discover what's missing.

---

## The Five Fingerprints — Finding Your First Skills

Answer these on paper. Each answer is a potential skill.

**1. What do you explain to every new team member about your codebase?**
(Conventions, architecture decisions, "the way we do things here")

**2. What mistake does everyone make at least once?**
(The gotcha that catches everyone — null handling, wrong directory, naming mistake)

**3. What boilerplate do you write repeatedly?**
(The patterns you copy-paste every time you add a new X)

**4. What code review comment do you leave most often?**
(The thing you keep correcting — "always add X", "never do Y")

**5. What does everyone on the team know but isn't documented anywhere?**
(Tribal knowledge — the implicit contracts, the unwritten rules)

### Prioritizing Your First Skill

From your five answers, pick the one with the highest:
- **Frequency** (daily > weekly > monthly)
- **Pain** (wastes an hour > wastes 5 minutes)
- **Specificity** (specific to your project > generic programming)

Write THAT one first.

---

## Common Mistakes

### Too Vague
❌ "Write good validation code following best practices"
✅ "When adding a validator in src/main/java/.../validators/, extend AbstractValidator<T>, implement validate() and getValidatorId(), register in ValidatorRegistry at line 47"

### Too Broad (It's Two Skills)
❌ One skill covering "the entire REST endpoint lifecycle from design to monitoring"
✅ Skill 1: "Add a REST endpoint" | Skill 2: "Add endpoint monitoring"

Rule: if your skill has more than 3 distinct workflows, split it.

### Wrong Location
❌ Project-specific pattern in `~/.claude/skills/common/`
✅ Project-specific patterns in `.claude/skills/` in your project
✅ Language/framework-agnostic patterns in `~/.claude/skills/common/`

### No Trigger Phrases
❌ A skill that works but nobody knows when to invoke it
✅ "When to Use This" section with exact phrases: "when user says 'add validator'", "when working with the validation layer"

### References Non-Existent Files
❌ "See ConfigManager.java for registration" (file was renamed last week)
✅ Verify file paths and line numbers when writing. Add `last_verified` date.

### Status Never Updated
❌ A skill marked `status: draft` after 6 months of use
✅ Promote: draft → beta (when it works) → production-ready (after 2 weeks reliable use)

---

## The Acid Test

After Claude writes the skill and you've saved it:
1. Start a **new Claude session** (fresh context, no history)
2. Ask Claude to do the thing your skill describes, using your natural language
3. Did Claude follow your skill's instructions? If yes: `status: beta`
4. If no: what did it do differently? Tell Claude — ask it to update the skill to fix the gap.

Common acid test failures:
- Skill is too vague → Claude interprets it loosely
- Wrong directory paths → Claude can't find the files
- Missing context → Claude fills in gaps from training data (not your project)

---

## Skill Lifecycle

```
Write (draft)
  → Acid test passes (beta)
    → 2 weeks reliable use (production-ready)
      → Code changes → verify still accurate
        → Still relevant after 3 months? Keep
        → Not relevant? Delete (deletion = health)
```

**Quarterly review (30 minutes):**
For each skill: last used? still accurate? still needed? understood by others?
Delete confidently. A 20-skill accurate library beats a 60-skill stale one.

---

## Skill Writing Speed Curve

| Experience | Time to write first skill | Time to write 10th skill |
|------------|--------------------------|--------------------------|
| First workshop | 30-45 minutes | 10-15 minutes |
| After 1 month | 15-20 minutes | 5-8 minutes |
| After 3 months | 5-10 minutes | 2-3 minutes |

The speed comes from pattern recognition — you start to see "that's a skill" immediately. The first one is always the hardest.

---

## The Promotion Path

When a skill appears in 2+ projects AND is language/framework agnostic:
→ Move to `~/.claude/skills/common/`

Signs a skill is ready to promote:
- You copied it verbatim to a second project
- Another developer said "I need this too"
- It works without any project-specific file paths
- It encodes a pattern, not a convention
