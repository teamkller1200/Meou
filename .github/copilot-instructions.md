---
description: "Use when working on Meou (Fabric mod). Enforces: single in-progress task; task-list.md as source of truth; clear scope boundaries; verifiable completion criteria; role separation for multi-agent workflows."
---

# Meou Project — Agent Collaboration & Task Management

## Task Management Principles

### Single Source of Truth

- **`task-list.md`** is the canonical task list for this workspace
- Update task status **before** and **after** each work session
- Status values: `not-started` | `in-progress` | `completed`
- Only ONE task may be `in-progress` at any time

### Task Lifecycle

1. **Planning Phase**: Draft tasks in `task-list.md` with clear, actionable titles (3–7 words)
2. **Start**: Mark ONE task as `in-progress` before beginning work
3. **Work**: Complete that specific task and document changes
4. **Verify**: Use evidence (see [Evidence-Based Completion](#evidence-based-completion)) to confirm
5. **Close**: Mark task `completed` immediately after verification
6. **Next**: Repeat with the next task

## Work Scope & Boundaries

### Kotlin-first implementation rule

- New code should be written in Kotlin unless there is a required reason to keep Java.
- Existing Java code may remain as-is for compatibility, but newly added logic and features should prefer Kotlin.
- When creating new classes, helpers, data containers, entity logic, or gameplay systems, use Kotlin by default.
- Java should only be used for legacy integration points, generated/required Fabric hooks, or cases where Kotlin would add unnecessary friction.

### Define Each Task

Every task must explicitly state:

- **Purpose**: What problem does this solve?
- **Scope**: Which files/features are affected?
- **Constraints**: What must NOT be changed?
- **Tests**: How will completion be verified?
- **Stop Condition**: What signals the task is done?

### Git and Worktree Operations

- Before modifying, deleting, or renaming a branch, inspect the current branch, worktree, upstream, and PR status.
- Do not create, delete, rename, switch, commit, push, or open/close a PR unless the user explicitly requests that operation.
- For a request that could change Git state, explain the detected constraints and planned operation before executing it.
- If a branch is associated with an existing PR or worktree, do not work around that association silently. Report the constraint and ask for the next action when needed.

### Minecraft API Lookup

- For Minecraft 1.21.1 Mojang-mapped API questions, consult `.vscode/skills/mc-source-lookup/SKILL.md` before relying on memory.
- Use `.vscode/skills/mc-source-lookup/scripts/mc-lookup.py` to verify class names and method signatures when the question concerns API existence, return types, arguments, or client-only classes.
- After API lookup, still verify imports and client/server boundaries in the project source before editing code.

### Example Task Outline

```markdown
## Task: Add item texture display

**Purpose**: Show item textures in inventory screen  
**Scope**: Only `ItemDisplayWidget.java`; no API changes  
**Constraints**: Must not modify server-side code  
**Tests**: Unit test in `ItemDisplayWidgetTest.java`; manual Minecraft test (1.20.1)  
**Stop Condition**: Textures render correctly; all tests pass; PR ready
```

## Multi-Agent Workflows

### Role & Context Boundaries

When using multiple AI agents (e.g., `Explore` subagent for codebase search → main agent for implementation):

1. **Define roles clearly**:
   - `Explore`: Read-only codebase search, pattern identification
   - `Main agent`: Implementation, testing, documentation
2. **Pass context explicitly**:
   - Subagent output → summarized findings only (no raw chat)
   - Main agent task should reference the task in `task-list.md` and findings summary
3. **No redundant searching**: Once Explore returns a result, main agent proceeds with implementation without re-running the same search

4. **Tool restrictions**:
   - Subagent: Search/read tools only
   - Main agent: All tools (edit, test, terminal, commit)

## Evidence-Based Completion

✓ **Accept as complete only when**:

- `git diff` shows precise changes (no unrelated code)
- Test output shows **all tests passing** (or new tests added with expected results)
- **Live verification** in game or dev environment (screenshots, logs, or test output)
- **Type/lint checks pass** (no warnings or errors if previously absent)

✗ **Do NOT accept self-reported completion**:

- "I think it's done" without evidence
- Untested changes
- Incomplete test coverage
- "I assume it works" statements

## Atomic Work Units

### One Task = Full Pipeline

A single task should include:

1. **Code implementation** (changes to Java, mixins, JSON configs)
2. **Testing** (unit tests pass; manual in-game verification if applicable)
3. **Documentation** (update README, comments, or task list if scope changed)
4. **Version control** (commit with clear message)
5. **PR readiness** (Draft PR opened, ready for review or merge)

Do NOT split these across multiple tasks unless explicitly unavoidable (e.g., "awaiting code review").

### Commit Message Format

```
<type>(<scope>): <subject>

<body>
```

Example:

```
feat(inventory): add item texture display

- Render item textures in ItemDisplayWidget
- Add unit tests for texture rendering
- Update .mixins.json for screen event

Closes task-list.md #42
```

## Quality Gates

### Before Marking as `completed`:

- [ ] All changes pushed to a branch or committed
- [ ] Tests run locally: `./gradlew build test` passes
- [ ] Code diff reviewed (no unintended side effects)
- [ ] Task description updated if scope changed
- [ ] Related documentation (README, docs, comments) updated

### If Using PRs:

- [ ] Draft PR created with clear description
- [ ] Linked to task in `task-list.md`
- [ ] Ready for human review or auto-merge

## Handling Ambiguity

If a task becomes unclear or dependencies emerge:

1. **Document** the ambiguity in the task's stop condition
2. **Ask clarifying questions** in the chat (do not guess)
3. **Update `task-list.md`** with findings if scope expands
4. **Return control** to the user to decide next step

## Related Files

- [task-list.md](../task-list.md) — Current task tracking
- [README.md](../README.md) — Project overview
- [TODO.md](../TODO.md) — Backlog for future work
