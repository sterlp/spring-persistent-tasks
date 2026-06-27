# AGENTS.md — Ground Rules

How we collaborate on **spring-persistent-tasks**. These are our general, project-independent
working rules (ported from our other projects so we don't lose them). The user-facing reference
documentation lives in [`doc/docs/`](doc/docs/) (VitePress, published at
https://spring-persistent-task.sterl.org/); the change log lives in [`CHANGELOG.md`](CHANGELOG.md).

## Ground Rules

- **Be concise to save tokens.** Keep chat answers short, direct, and focused — no filler, no
  restating what's already known. Brevity applies to chat; the docs still capture every decision.
- **Ask one question at a time, and always include a recommendation.** When clarification is
  needed, ask a single focused question and propose a suggested answer (marked as recommended).
- **Clarify before you plan — don't assume.** If a requirement is not written down, ask questions
  or ask for examples instead of guessing.
- **Point out conflicts** with the existing docs/behaviour. A new feature should fit into the
  current design without breaking the public API or existing users (semantic versioning).
- **Be compact in the docs.** Don't echo code or details that are clear from the code itself. Docs
  hold the requirements, the vision, how the system behaves, structure, and the decisions (ADRs).
- **One feature = one name everywhere.** A feature owns its docs page, its Java package, and its UI
  folder under the **same name**.
- **Update the docs first.** When requirements or behaviour change, update the relevant
  [`doc/`](doc/) page (and the ADR registry, see below) **before** the code.
- **Update User docs.** If the framework changes, check the user doc (vitepress)
  `doc/docs` and page registry in `doc/.vitepress/config.ts` - how the framework is used
- **Tests are mandatory.** Business rules are guarded by tests (JUnit + the `test` module, plus
  Vitest for the UI). A rule without a test is not "done".
- **Every fixed bug gets a regression test — preferably end-to-end as a blackbox.** Add a test that
  fails without the fix and passes with it. Prefer driving the real entry point (the REST endpoint,
  the public service API) over testing internals, so the test mirrors what a user hits.
- **Every error or rule we discuss is written into the docs.** Don't leave a decision only in chat:
  capture it in the relevant docs page (and in an ADR when it's a technical decision — see below).
- **Finish planning in the docs, not the chat.** A planning session ends only when the docs (plus
  any ADR and registry updates) are complete enough that compacting the chat would lose **no**
  decision. Capture everything before compacting.
- **Tests never hit external infrastructure.** Run against H2 / in-process fakes, never a real
  database server or remote service in unit/integration tests.
- Apply Uncle Bob clean code and SOLID principles to keep the code maintainable.

## Architecture Decision Records (ADR)

Every technical decision is recorded as a short ADR so we don't re-litigate it later. One decision
per file, written once.

- Location: [`doc/adr/`](doc/docs/adr/) — one `NNNN-short-title.md` per decision.
- Registry: [`doc/adr/README.md`](doc/docs/adr/README.md) — a table of all ADRs with status,
  updated whenever an ADR is added.
- Format per ADR: **Status · Context · Decision · Consequences** (kept brief).
- When a decision replaces an older one, mark the old ADR `Superseded` and link the new one.

## Build & Release

- Java 21, Spring Boot 4.x. Build/install locally with `mvn clean install` (the `example` module is
  not part of the root reactor — build it separately if needed).
- Modules: `core` (task/trigger/scheduler/history), `db` (Liquibase schema), `ui` (React dashboard
  served at `/task-ui`), `test` (shared test helpers).
