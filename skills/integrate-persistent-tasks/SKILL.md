---
name: integrate-persistent-tasks
description: Integrate org.sterl.spring:spring-persistent-tasks into a Spring Boot app — setup, task design, retry classification, and tests that can actually fail.
---

# Integrate spring-persistent-tasks

> **Fork of the global skill**, extended with field experience from FORgE (Slice 3.1, 2026-08-17),
> MaPa and us4g. Sections marked ** Field note** are the additions — they are the parts that cost
> time when nobody wrote them down.
> Official docs: <https://spring-persistent-task.sterl.org/> · Testing:
> <https://spring-persistent-task.sterl.org/test/junit-test>

** Field note — read this first.** It is a **library**, not a pattern to copy. FORgE planned to
"port the task framework from us4g" and wasted a planning cycle before noticing that us4g simply
declares a dependency. Check `central.sonatype.com` before writing a single class.

---

## 0. Decide the database (do this first)

Inspect the host app's persistence before adding anything:

1. **Is a `DataSource` already configured?** Look for `spring.datasource.*`, a JPA starter, or an
   existing Liquibase/Flyway setup. If yes → **reuse it**. The library only adds its own tables.
2. **No DataSource?** → wire **H2**. Prefer file-backed (`jdbc:h2:file:./state/spt`) so task state
   survives restarts; in-memory only for tests.

Supported DBs: PostgreSQL, MariaDB, MS SQL, H2 — check the current docs, the support matrix moves.

## 1. Pick the version (match your Spring Boot major)

The library's **major version tracks Spring Boot's major** so compatibility is obvious:

| Spring Boot | spring-persistent-tasks | Notes |
|-------------|-------------------------|-------|
| 3.x         | **2.4.x**               | Jackson 2; the original line. |
| 4.x         | **4.x**                 | Jackson 3 + moved auto-config packages. The 3.x slot is skipped so the major matches Boot 4. |

Use a property (`${spt.version}`) and set it to the line matching your Boot major.

> **springdoc-openapi users:** the library pulls QueryDSL transitively, which activates springdoc's
> QueryDSL OpenAPI customizer. On Spring Boot 4 use **springdoc 3.x** — springdoc 2.x breaks there.

** Field note — verify the exact Boot minor, and stop if it fails.** "Boot 4.x ⇒ spt 4.x" is a
rule of thumb, not a guarantee: FORgE runs Boot 4.0.6 while the reference project ran 4.1.0. Make
the first increment *"context starts, existing tests still green"* and treat a failure there as a
**stop-and-report**, never as licence to bump Spring Boot. Upgrading the framework to satisfy a
library is a separate decision with a separate blast radius.

## 2. Dependencies

```xml
<properties>
  <!-- Boot 3.x → 2.4.x; Boot 4.x → 4.x -->
  <spt.version>4.0.1</spt.version>
</properties>

<dependency>
  <groupId>org.sterl.spring</groupId>
  <artifactId>spring-persistent-tasks-core</artifactId>
  <version>${spt.version}</version>
</dependency>
<dependency>
  <groupId>org.sterl.spring</groupId>
  <artifactId>spring-persistent-tasks-db</artifactId>   <!-- Liquibase changelog -->
  <version>${spt.version}</version>
</dependency>
<dependency>
  <groupId>org.sterl.spring</groupId>
  <artifactId>spring-persistent-tasks-ui</artifactId>   <!-- optional dashboard -->
  <version>${spt.version}</version>
</dependency>
<dependency>
  <groupId>org.sterl.spring</groupId>
  <artifactId>spring-persistent-tasks-test</artifactId>
  <version>${spt.version}</version>
  <scope>test</scope>                                   <!-- do not forget the scope -->
</dependency>
```

** Field note — split the artefacts along your module boundaries.** In a multi-module build the
artefacts do **not** all belong in one POM:

| Artefact | Belongs in |
|---|---|
| `-core`, `-test` | the module that owns the tasks (usually `…-backend`) |
| `-db` | the module that owns the schema (`…-database`), next to your other Liquibase parts |
| `-ui` | only where the dashboard is served — and only when you actually ship it |

Putting `-db` in the backend works until someone builds the schema module standalone.

## 3. Enable it

```java
@SpringBootApplication
@EnableSpringPersistentTasks       // scheduler, REST API at /spring-tasks-api, JPA entities/repos
@EnableSpringPersistentTasksUI     // optional: dashboard at /task-ui
public class MyApplication { … }
```

With a custom `@EntityScan`/`@EnableJpaRepositories`, also add
`@EnableSpringPersistentTasksEntityScan` and `@EnableSpringPersistentTasksJpaRepositories`.

## 4. Configuration

```yaml
spring:
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: none              # schema is owned by Liquibase, never Hibernate
  persistent-tasks:
    max-threads: 1                # keep low to bound parallel work
    poll-rate: 30                 # seconds between polls
    history:
      delete-after: PT72H         # how long completed triggers are kept
```

If you already own a Liquibase master changelog, **include** the library's:

```xml
<include file="spring-persistent-tasks/db.changelog-master.xml" />
```

** Field note — include position matters.** Put it **after** your base schema and **before** any
changeset that depends on it. There is no universally correct slot; anchor it relative to your own
files, not at the top or bottom by habit.

** Field note — retention and de-duplication are coupled.** If you guard against reprocessing by
looking into the history (see §7), then `history.delete-after` *is* your de-duplication window.
Set it to 72 h and a message redelivered on day four is processed a second time — silently. Decide
both numbers together and write the reasoning down.


---

## 5. Register a task

```java
@Component(MyTask.NAME)
public class MyTask implements PersistentTask<MyState> {
    public static final String NAME = "MyTask";
    public static final TaskId<MyState> ID = TaskId.of(NAME);

    @Override public void accept(MyState state) { … }

    @Override public RetryStrategy retryStrategy() {
        return new MultiplicativeRetryStrategy(3, ChronoUnit.MINUTES, 1);  // 1, 2, 3 min
    }

    @Override public void afterTriggerFailed(MyState state, Exception e) { … }  // final failure only
}
```

** Field note — the task is a vehicle, not a new home for business logic.** Make it an adapter
that calls the code you already have. If a task grows its own rules, you have two places to change
and one of them will be forgotten.

** Field note — cut the task at the boundary to the foreign system.** That is where the retry
belongs. A task wrapping *the whole* processing retries everything after a remote 500 — including
the parts that already succeeded. One task per outbound dependency keeps the retry proportional.

### Choosing the trigger key

| Field | Use it for |
|---|---|
| `id` | the **technical** identity — the message/event id. This is the de-duplication key. |
| `correlationId` | the **business** identity a human searches by (an order number, a metering point). |
| `tag` | the source or target system, for filtering in the dashboard. |
| `state` | the payload needed to redo the work. |

** Field note — the key must be unique in the same space as the work.** Two traps we hit:

- **Too coarse:** keying a per-customer task on the *event* id breaks as soon as one event carries
  several customers — the second trigger overwrites the first and that work vanishes silently.
- **Not qualified enough:** a business id is often unique only *within its system*. Key on
  `<systemId>-<businessId>`, never the bare id, or two tenants collide.

Keying a write-back task per business object is a feature: at most one pending update per object,
last state wins.

### Field note — the `state` is a published contract, treat it like one

By default the state is serialised with **Java binary serialisation** into a `@Lob byte[]`. That is
**not** a freeze: Java serialisation tolerates *compatible* changes — adding a field, removing one,
widening a type — as long as `serialVersionUID` stays put. Only **incompatible** changes break
stored triggers: renaming or retyping a field, changing the class hierarchy.

Treat the state exactly like any other API you publish — because that is what it is, a contract
with your own database:

- **Give the task its own dedicated state class.** Never hand it a domain entity, a JPA entity or a
  DTO borrowed from another layer. Those change for reasons that have nothing to do with the task,
  and every such change becomes a migration nobody planned.
- **Manage it deliberately:** explicit `serialVersionUID`, a Javadoc note that it is persisted, and
  a conscious call at each change whether it is compatible or not.
- **Keep it small.** Fewer fields, fewer ways to break it. Store what is needed to redo the work —
  for evidence-style payloads, the raw message and nothing else.
- **The escape hatch:** old triggers can simply be **deleted**. An incompatible change is not a
  dead end — decide whether the pending work still matters, drop the stale triggers, move on. Know
  this before you fear a broken deserialisation.
- Expecting frequent evolution? Use `JacksonStateSerializer<T>` from the start; retrofitting it
  does not rescue existing rows.

---

## 6. Queue / trigger a task

```java
persistentTaskService.queue(TriggerBuilder
    .<MyState>newTrigger(MyTask.NAME, state)
    .id(eventId)
    .correlationId(pointOfDelivery)
    .tag(sourceSystem)
    .build());
```

** Field note — persist the trigger in the same transaction as the state that caused it.**
Use `BEFORE_COMMIT`, not `AFTER_COMMIT`: all-or-nothing. With `AFTER_COMMIT` the task can start
before the causing commit is visible and a retry then reads stale data — or finds nothing at all.
The trigger must exist exactly when the business fact exists.

** Field note — classify errors before you retry.** Not every failure deserves three attempts:

| Symptom | Retry? |
|---|---|
| `401`, `403`, `429`, `5xx`, timeout, connection reset | **yes** — transient |
| `404`, validation error, unparsable payload | **no** — `FailTaskNoRetryException` |

Retrying a permanently broken payload burns time and delays the clearing case that a human needs.
Detect the cause **by exception type**, never by string-matching a message.

** Field note — raise the human-visible case only in `afterTriggerFailed`.** While the task is
still retrying there is nothing to clear; a case that resolves itself on the next attempt only
steals attention. If several causes end in the same hook, branch there — and make sure exactly
**one** case per incident is created, never two.

---

## 7. Read running triggers & history

```java
persistentTaskService.getLastTriggerData(TriggerKey.of(id, MyTask.ID));  // includes history
triggerService.searchTriggers(…);                                       // active only
historyService…                                                         // completed runs
```

** Field note — a key is unique only among *live* triggers.** Once a trigger completes it moves to
`pt_completed_triggers` and the key is free again; the same key may appear there several times. So:

- Re-queuing a **waiting** trigger overwrites it in place.
- Queuing while the same key is **running** throws `IllegalStateException`.
- After completion, nothing stops a duplicate — the library does not remember "already done".

If you need "this message was already processed", query `getLastTriggerData` (which reads the
history) and guard **at the entry point** only. Do not put that guard inside the task: its own
retries are not new work and would block themselves.

---

## 8. Embed the dashboard

> **Gotcha — don't let your own SPA fallback swallow `/task-ui`.** The dashboard is a separate React
> app with its own client-side router; the library forwards `/task-ui` and `/task-ui/**` to **its**
> `index.html`. A catch-all `@Controller` in the host app **outranks** the library's
> `ViewController` registrations and hijacks the path — the iframe then loads *your* app: doubled
> header, empty body, no error. Exclude `/task-ui` and `/spring-tasks-api` from your fallback:
>
> ```java
> @GetMapping({"/", "/{p:(?!task-ui$|spring-tasks-api$)[^\\.]*}",
>              "/{p:^(?!task-ui$|spring-tasks-api$).*}/{s:[^\\.]*}"})
> String forward() { return "forward:/index.html"; }
> ```
>
> Regression-test with MockMvc: `GET /task-ui` must be `forwardedUrl("/task-ui/index.html")`. On
> Spring Boot 4 build MockMvc via `MockMvcBuilders.webAppContextSetup(ctx)`.

---

## 9. Testing

Disable the scheduler and drive tasks explicitly — deterministic, no sleeping:

```yaml
spring:
  datasource: { url: "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1" }
  persistent-tasks:
    scheduler-enabled: false
    timers-enabled: false
```

```java
@Autowired PersistentTaskService tasks;
@Autowired TriggerService triggerService;
@Autowired PersistentTaskTestService taskTest;

taskTest.runAllDueTrigger(OffsetDateTime.now().plusHours(1));  // also picks up future retries
taskTest.assertHasNextTask(TriggerStatus.SUCCESS, TriggerKey.of("alpha", MyTask.ID));
taskTest.assertNoMoreTriggers();
```

**Reset state between methods** — the context and its H2 are cached across test classes:

```java
@BeforeEach void clean() { triggerService.deleteAll(); historyService.deleteAll(); }
@AfterEach  void tidy()  { triggerService.deleteAll(); }
```

** Field note — clean *after* as well as before.** Cleaning only in `@BeforeEach` leaves the last
test's triggers alive for whatever unrelated class runs next. The failure surfaces far away from
its cause and looks like flakiness.

** Field note — `plusHours(1)` is what makes retry tests instant.** `runAllDueTrigger` takes the
"due until" instant, so a trigger scheduled three minutes out runs immediately. Never
`Thread.sleep` a real backoff.

** Field note — never install a synchronous executor that runs the task inline on enqueue.** It
tests away the very asynchrony that breaks in production. Disabling the scheduler and driving
explicitly is *not* the same thing: the trigger is still really persisted, the caller still returns
before the work happens, transactions still separate.

###  Field note — the tests that lie

Async changes turn passing tests into decoration. Three failure modes we actually hit:

1. **A key that cannot exist.** A test asserted "no trigger with id `abc`" for a broken payload —
   but the id was never parsed from that payload, so a wrongly created trigger would have carried
   `null`. Both assertions were tautologies, green regardless of the code.
   → When asserting absence, **count** (`countTriggers()` before/after) instead of probing a key
   that the failure path cannot produce.
2. **Asserting the wrong rule.** After adding an entry-point guard, "same message three times ⇒ one
   result" silently stopped testing state-based idempotency and started testing the guard. If you
   want the former, give each delivery its **own** message id.
3. **Log-or-throw checked only halfway.** If the rule is "log *and* rethrow", asserting the throw
   leaves the logging untested — someone deletes the log line and everything stays green. Attach a
   logback `ListAppender` and assert the ERROR entry.

**The only proof that a test can fail: break the fix and watch it go red.** Re-introduce the bug,
run the test, capture the real tool output, restore. Store that evidence in a file — a claim in a
chat message is gone after the next compaction.

---

## Migration checklist (existing app, first task)

1. Version + dependencies + `@EnableSpringPersistentTasks`; **existing test suite still green.**
   Nothing else in this step.
2. Task class + state, driven directly by a test — production path untouched.
3. Switch the caller to enqueue; migrate the affected tests to the explicit driver.
4. Entry-point guard (§7), if you need de-duplication.
5. Error split: no-retry vs. retry, and the case in `afterTriggerFailed`.

Each step green on its own. If step 1 turns something red, it is the framework, not your design —
the value of doing it alone is that you know which of the two it was.
