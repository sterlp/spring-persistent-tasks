# AGENTS.md — Ground Rules

How we collaborate. The project map/vision lives in **[CONTEXT.md](CONTEXT.md)**; each feature's
business requirements and BDD asserts live in its own `docs/` page.

<!-- COMMON RULES START -->
## Common Rules

- **Be concise.** Short, direct chat answers — no filler, no restating the known. The docs still
  capture every decision.
- **One question at a time, with a recommended answer.**
- **Clarify → plan → code.** Don't assume — if it isn't in the docs, ask or request an
  example/BDD/use-case. The feature must be clear and fit the existing docs.

### Response style
- No preamble/postamble — skip "I will…", "Here is…", "Based on…", "Done."
- Answer directly, 1–4 lines unless detail is asked for. Don't repeat what was said; cut words that
  add no meaning.
- Can't help? Offer alternatives in 1–2 sentences — don't moralize.
- `->` denotes a dependency.

### Docs-first — the docs are the SOLL/WIE
- **Plan in the docs together; joint planning IS the approval.** While planning decide: new module?
  Check for conflicts with existing rules and fit with current docs.
- **`docs/<feature>.md` (audience: User + AI)** — every feature has a name and its own page, laid out
  top to bottom:
  1. a short **functional description** of the feature,
  2. its **rules**,
  3. under each rule its **BDD examples** (GIVEN/WHEN/THEN) — the concrete cases that pin the rule down,
  4. optional: API / status / flow, then an **implementation-state** table at the end.
- **Minimum:** ≥1 rule per feature, ≥2 BDD examples per rule (positive + negative).
  Story -1:n-> Rules -1:n-> BDD.
- **`adr/` (audience: AI across sessions, and humans)** — the decisions + their *why*, so the reasoning
  survives; registry in `adr/README.md`. `CONTEXT.md` = map/vision + feature registry.
- **At the end, reconcile the docs with what was built — and compress** (only what helps, never
  echo the code).
- **Broad sweep → read every `CONTEXT.md` at once** (root + per-module) before diving into a single doc.

### Module structure
Maven multi-module, each module prefixed with the project key (e.g. `<project>-api`):
- **A large feature = its own module.** In large projects the backend holds only config/wiring;
  every feature is its own module.
- **One feature = one name** across its `docs/` page and the module's package/folder.
- `…-api` — code shared between modules. `…-backend` — config + wiring. `…-shared` —
  dependency-free shared code (a backend-only shared stays backend-specific).
- **A module is doc-self-contained.** Every Maven module owns a skeleton — `CONTEXT.md`, `docs/`,
  `adr/` (+ `adr/README.md`) and a `README.md` — created empty up-front, filled as you touch. Root
  `CONTEXT.md` **links** to each module and the module **links back**; root keeps only **cross-cutting**
  docs + ADRs, module-specific ADRs live in the **module's** `adr/` (no re-summarising the module in
  root → no drift). A feature spanning modules: doc + ADR live with the **owning** module, glue only
  links. Exception: `*-test`/support modules get no skeleton.
<!-- COMMON RULES END -->

<!-- COMMON CODE START -->
## Common Code

### Testing
- **Tests are mandatory** — a rule without a test isn't "done".
- **BDD via JUnit** — Given/When/Then through the service or REST endpoint; single components as
  Mockito unit tests.
- **Every bug gets a regression test, preferably end-to-end/blackbox** via the real entry point
  (e.g. MockMvc): fails without the fix, passes with it.

### Logging
- **Log OR throw, never both** (except facades where the exception leaves the context).
- **Always include context:** the ID/object, the problem, and any known workaround/bugfix.
- **Never test logging** — don't assert on log output or levels.

### Code
- **Lombok** for logging (`@Slf4j`) and constructors (`@RequiredArgsConstructor`).
- **>3 method args -> pass a command object.** A larger command object = plain POJO with Lombok
  `@Builder` (especially when there are default values).
- **Shared code lives in the shared module/package** and stays dependency-free.
- **Dependencies flow one way, never in a circle.**
- Keep units small and well-named; apply Clean Code & SOLID (esp. Open/Closed) where it aids
  maintainability.
<!-- COMMON CODE END -->

<!-- COMMON UI START -->
## Common UI (frontend)

- **`…-frontend`** — React/TypeScript UI; one folder per feature, named like the feature.
- **`…-api`** — TS types generated via `typescript-generator-maven-plugin` (`server-api.d.ts`),
  consumed by the frontend.
- **Frontend tests are mandatory (Vitest)** — a rule without a test isn't "done".
<!-- COMMON UI END -->

<!-- COMMON DB START -->
## Common DB

- **`…-database`** — schema & Liquibase migrations; DB scripts always live in their own module,
  never in the backend.
<!-- COMMON DB END -->

<!-- BACKEND (SPRING) START -->
## Backend (Spring) — component architecture

- **Service = the public API.** A feature's components live isolated in their package and are
  reached ONLY through a `*Service`; everything else is private. A component never calls its **own**
  Service (no upward call) — calling **other** features' Services is fine.
- **Flow:** REST `*Resource` -> `*Service` -> `*Component` -> `*Repository`. The Service returns an
  Entity (persistence) or DTO (connectors); the Resource converts to the API model.
- **Extract to a `*Component`** when logic is >10 lines, reused, or needs isolated testing.
- **`@Transactional(timeout = 10_000)`** on the Service only when it does persistence; Components
  participate as `MANDATORY` (no own transaction). Read paths use `@Transactional(readOnly = true)`.
- Full layering, suffixes, converters and test conventions: the **component-architecture ADR**.
<!-- BACKEND (SPRING) END -->

<!-- PROJECT SPECIFIC START -->
## Project: spring-persistent-tasks

A published Spring Boot starter (persistent tasks, triggers, scheduler, history). Users depend on
it as a **library**, so the public API is a contract.

### Docs layout in this repo
- **Agent/Dev docs** — root [`docs/`](docs/): `CONTEXT.md` (map/vision + feature registry) and
  per-feature `docs/<feature>.md`. This is the `docs/` the Common Rules refer to.
- **ADRs** — [`docs/adr/`](docs/adr/), one `NNNN-short-title.md` per decision, registry in
  [`docs/adr/README.md`](docs/adr/README.md). Format: **Status · Context · Decision · Consequences**
  (kept brief). When a decision replaces an older one, mark the old ADR `Superseded` and link the new.
- **User docs = the published homepage** — VitePress in [`homepage/`](homepage/): pages under
  `homepage/src/`, page registry in `homepage/.vitepress/config.ts`, published at
  https://spring-persistent-task.sterl.org/. Change log: [`CHANGELOG.md`](CHANGELOG.md).
- **When user-visible framework behaviour changes, update the homepage too** (`homepage/src/` + the
  `config.ts` registry) — not only the dev docs.
- **Never duplicate user docs into the dev docs — reference them.** User-facing documentation always lives
  in the homepage (`homepage/src/`); a `docs/<feature>.md` links to the relevant homepage page instead of
  restating configuration/usage. The dev doc keeps only the rules, BDD and implementation map.

### Library rules (extend the common rules)
- **Semantic versioning — never break the public API or existing users.** A new feature fits the
  current design; a breaking change needs a major bump and an ADR.
- **Tests never hit external infrastructure** — run against H2 / in-process fakes, never a real
  database server or remote service.

### Modules (these names are authoritative — they map onto the common scheme)
- `core` — task/trigger/scheduler/history: the public API **and** config/wiring (this repo keeps
  them together rather than split into `…-api` / `…-backend`).
- `db` — Liquibase schema (the common `…-database`).
- `ui` — React dashboard served at `/task-ui` (the common `…-frontend`; generated TS types feed it).
- `test` — shared test helpers (support module → no doc skeleton).
- `example` — demo app, **not part of the root reactor**; build it separately if needed.

### Build
- Java 21, Spring Boot 4.x. Build/install locally with `mvn clean install`.
<!-- PROJECT SPECIFIC END -->
