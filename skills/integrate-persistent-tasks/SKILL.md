---
name: integrate-persistent-tasks
description: Integrate spring-persistent-tasks into a Spring Boot 4 app — detect an existing DataSource (else wire H2), add core/db/ui, register a task, queue triggers, read running triggers and history, and embed the /task-ui dashboard. Use when adding durable background jobs/tasks to a Spring Boot app.
---

# Integrate spring-persistent-tasks

A guide for adding [spring-persistent-tasks](https://spring-persistent-task.sterl.org/) — durable,
DB-backed, retryable background tasks — to a **Spring Boot 4 / Java 21** application. The DB is the
queue: triggers survive restarts and are retried on failure, so no in-memory queue or `@Scheduled`
backstop is needed.

## 0. Decide the database (do this first)

Inspect the host app's persistence before adding anything:

1. **Is a `DataSource` already configured?** Look for `spring.datasource.*` in
   `application.yml`/`.properties`, a JPA starter on the classpath, or an existing Liquibase/Flyway
   setup. If yes → **reuse it**. The library just adds its own tables via its Liquibase changelog.
2. **No DataSource?** → wire **H2**. Prefer a **file-backed** H2 (`jdbc:h2:file:./state/spt`) so
   task state survives restarts; use in-memory (`jdbc:h2:mem:…`) only for tests.

Supported DBs: PostgreSQL, MariaDB, MS SQL, H2. **Not** MySQL (no sequences).

## 1. Pick the version (match your Spring Boot major)

The library's **major version tracks Spring Boot's major version** so compatibility is obvious:

| Spring Boot | spring-persistent-tasks | Notes |
|-------------|-------------------------|-------|
| 3.x         | **2.4.x**               | Jackson 2; the original line. |
| 4.x         | **4.x**                 | Jackson 3 + moved auto-config packages (ADR 0001). The 3.x slot is skipped so the major matches Boot 4. |

Latest releases: <https://central.sonatype.com/artifact/org.sterl.spring/spring-persistent-tasks-core/versions>.
Use a property (`${spt.version}`) and set it to the line matching your Boot major.

> **springdoc-openapi users:** the library pulls QueryDSL transitively, which activates springdoc's
> QueryDSL OpenAPI customizer. On Spring Boot 4 use **springdoc 3.x** — springdoc 2.x breaks there.

## 2. Dependencies

```xml
<properties>
  <!-- Boot 3.x → 2.4.x; Boot 4.x → 4.x -->
  <spt.version>4.0.0</spt.version>
</properties>

<!-- the task engine -->
<dependency>
  <groupId>org.sterl.spring</groupId>
  <artifactId>spring-persistent-tasks-core</artifactId>
  <version>${spt.version}</version>
</dependency>
<!-- Liquibase changelog that creates the task tables (also pulls the Boot-4 Liquibase auto-config) -->
<dependency>
  <groupId>org.sterl.spring</groupId>
  <artifactId>spring-persistent-tasks-db</artifactId>
  <version>${spt.version}</version>
</dependency>
<!-- optional: the /task-ui dashboard -->
<dependency>
  <groupId>org.sterl.spring</groupId>
  <artifactId>spring-persistent-tasks-ui</artifactId>
  <version>${spt.version}</version>
</dependency>

<!-- JPA + a driver (only if the host app doesn't already have them) -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
  <scope>runtime</scope>
</dependency>
```

## 3. Enable it

```java
@SpringBootApplication
@EnableSpringPersistentTasks       // scheduler, REST API at /spring-tasks-api, JPA entities/repos
@EnableSpringPersistentTasksUI     // optional: dashboard at /task-ui
public class MyApplication { … }
```

If the host app uses a custom `@EntityScan`/`@EnableJpaRepositories`, also add
`@EnableSpringPersistentTasksEntityScan` and `@EnableSpringPersistentTasksJpaRepositories` so the
library's entities/repositories are still picked up.

## 4. Configuration

```yaml
spring:
  datasource:                     # only if not already present
    url: jdbc:h2:file:./state/spt;AUTO_SERVER=TRUE
    username: sa
    password: ""
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: none              # schema is owned by Liquibase, never Hibernate
  liquibase:
    change-log: classpath:spring-persistent-tasks/db.changelog-master.xml
  persistent-tasks:
    max-threads: 1                # worker threads; keep low to bound parallel work (e.g. LLM calls)
    poll-rate: 30                 # seconds between polls
    history:
      delete-after: PT72H         # how long completed triggers are kept
```

If you already have your own Liquibase master changelog, include the library's instead of pointing
`change-log` at it directly:

```xml
<include file="spring-persistent-tasks/db.changelog-master.xml" />
```

## 5. Register a task

A task is a typed unit of work. The `state` (payload) is serialized into the trigger row.

```java
@Component(MyTask.NAME)
@RequiredArgsConstructor
public class MyTask implements PersistentTask<MyState> {
    public static final String NAME = "myTask";
    public static final TaskId<MyState> ID = new TaskId<>(NAME);

    private final SomeService service;

    @Override
    public void accept(MyState state) {
        service.doWork(state);          // throwing => the framework retries per RetryStrategy
    }

    @Override
    public RetryStrategy retryStrategy() {
        return RetryStrategy.THREE_RETRIES;   // default
    }
}
```

`MyState` must be `Serializable`. Trivial tasks can be a `PersistentTask<String>` bean (a lambda).

## 6. Queue / trigger a task

```java
@RequiredArgsConstructor
class Caller {
    private final PersistentTaskService persistentTaskService;

    void run(MyState state) {
        persistentTaskService.runOrQueue(
            TriggerBuilder.newTrigger(MyTask.NAME)
                .state(state)
                .id("optional-idempotency-key")   // dedupes a logical job
                .correlationId("chain-123")        // group related triggers
                .runAfter(Duration.ofMinutes(5))   // optional delay
                .build());
    }
}
```

`runOrQueue` runs immediately if a worker is free, else persists for the next poll. Use `queue(…)`
to always defer. You can also publish `TriggerTaskCommand.of(trigger)` as a Spring event.

## 7. Read running triggers & history (build agent/ops tools on this)

- `TriggerService` — live/pending triggers:
  - `searchTriggers(TriggerSearch, Pageable)` — filter by task name, status, tag, correlationId.
  - `get(TriggerKey)`, `hasPendingTriggers()`, `countTriggers(...)`.
- `HistoryService` — completed/failed triggers:
  - `searchTriggers(TriggerSearch, Pageable)`, `findLastKnownStatus(TriggerKey)`,
    `findAllDetailsForInstance(instanceId, Pageable)`, `taskStatusHistory()`, `reQueue(id, when)`.
- `TriggerSearch.byCorrelationId(...)`, `byStatus(TriggerStatus...)` build common filters.
- REST equivalents under `/spring-tasks-api/{triggers,history,...}` (also what `/task-ui` consumes).

Expose these as read-only tools (e.g. `task_list_triggers`, `task_history`) so an agent can inspect
what it scheduled.

## 8. Embed the dashboard

With `@EnableSpringPersistentTasksUI`, the React dashboard is served at **`/task-ui`** (same origin,
no frame-busting headers). Embed it in your app via an `<iframe src="/task-ui">`, or just link to it
in a new tab. In a dev setup with a separate frontend, proxy `/task-ui` and `/spring-tasks-api` to
the backend.

> **Gotcha — don't let your own SPA fallback swallow `/task-ui`.** The dashboard is a separate
> React app with its **own** client-side router, so the library forwards `/task-ui` and
> `/task-ui/**` to **its** `/task-ui/index.html` (see `SpringPersistentTasksUIConfig`, which
> registers `addViewControllers` + `addResourceHandlers`). If your host app has a catch-all that
> forwards unknown paths to *your* `index.html` for *your* SPA router, it will **hijack
> `/task-ui`** — because a `@Controller`/`@RequestMapping` handler **outranks** the library's
> `ViewController` registrations. The iframe then loads *your* app instead of the dashboard:
> **doubled header, empty body, no error**. Fix: exclude `/task-ui` (and `/spring-tasks-api`) from
> your fallback so the library serves them. With a regex view controller:
>
> ```java
> // app SPA fallback — note the negative lookahead excluding the library's paths
> @GetMapping({"/", "/{p:(?!task-ui$|spring-tasks-api$)[^\\.]*}",
>              "/{p:^(?!task-ui$|spring-tasks-api$).*}/{s:[^\\.]*}"})
> String forward() { return "forward:/index.html"; }
> ```
>
> Regression-test it with MockMvc: `GET /task-ui` must be `forwardedUrl("/task-ui/index.html")`,
> not your app's `/index.html`. (On Spring Boot 4 build MockMvc from the `WebApplicationContext`
> via `MockMvcBuilders.webAppContextSetup(ctx)` — the `@AutoConfigureMockMvc` slice was split out.)

## 9. Testing

> Reference: <https://spring-persistent-task.sterl.org/test/junit-test>.

Use in-memory H2 and disable the scheduler so tests stay deterministic; drive tasks explicitly:

```yaml
spring:
  datasource: { url: "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1" }
  liquibase: { change-log: classpath:spring-persistent-tasks/db.changelog-master.xml }
  persistent-tasks:
    scheduler-enabled: false
    timers-enabled: false
```

Add the **test artifact** — it provides `PersistentTaskTestService`, the deterministic driver:

```xml
<dependency>
  <groupId>org.sterl.spring</groupId>
  <artifactId>spring-persistent-tasks-test</artifactId>
  <version>${spt.version}</version>
  <scope>test</scope>
</dependency>
```

Drive and assert queued work with it instead of sleeping or hitting the live scheduler:

```java
@Autowired PersistentTaskService tasks;            // queue / runOrQueue, getLastTriggerData
@Autowired TriggerService triggerService;          // searchTriggers, countTriggers, deleteAll
@Autowired HistoryService historyService;          // finished runs, deleteAll
@Autowired PersistentTaskTestService taskTest;     // the test driver

// queue, then run everything due now — runs one-by-one so freshly queued triggers are picked up
events.publishEvent(new ThreadClosedEvent(1L, "alpha", 123L));
taskTest.runAllDueTrigger(OffsetDateTime.now());

// run the next due trigger AND assert its final status / key in one call
taskTest.assertHasNextTask(TriggerStatus.SUCCESS, TriggerKey.of("alpha", MyTask.ID));
taskTest.assertNextTaskSuccess();                  // run next, assert SUCCESS
taskTest.assertNoMoreTriggers();                   // nothing left to run
```

**Reset state between methods.** The Spring context (and its H2) is cached across test methods, so
triggers/history leak from one test into the next. Clear them in `@BeforeEach`:

```java
@BeforeEach void clean() { triggerService.deleteAll(); historyService.deleteAll(); }
```

**Test the single-flight-per-key guarantee.** Keying a trigger by a stable business id means the engine
keeps **at most one trigger per key** — re-queuing a *waiting* one updates it in place, and queuing
while it is *running* throws `IllegalStateException`. So "the same work never runs twice concurrently"
is a property you can assert without threads: trigger the producer twice and check there is still
exactly one trigger for that key.

```java
scheduler.scan();                                   // queues id="alpha"
scheduler.scan();                                   // sees it already queued → no duplicate
assertThat(triggerService.searchTriggers(taskNamed(MyTask.NAME), PageRequest.of(0, 50))
        .getContent()).extracting(t -> t.getData().getKey().getId())
        .containsExactly("alpha");                  // still one, not two
```

To make a producer idempotent, check before queuing with
`tasks.getLastTriggerData(key).map(t -> TriggerStatus.ACTIVE_STATES.contains(t.status())).orElse(false)`
(waiting/running) and skip if already active — or just queue and catch the `IllegalStateException`.

Never hit a real database server in tests.

## Future / extension ideas

- **Tool-backed tasks:** a task that invokes the app's tool layer (web fetch, document parse, …).
- **Agent-spawns-agent:** a task whose work is "run agent X"; with `correlationId` chaining, an agent
  can launch follow-up agents as durable, retryable tasks and watch them via the history tools above.
