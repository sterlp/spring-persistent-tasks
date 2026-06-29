---
title: JUnit Testing for Tasks and Triggers
description: Comprehensive guide to testing Spring Persistent Tasks with JUnit including disabling scheduler, manual trigger execution, and async testing strategies.
keywords: junit testing, spring task testing, trigger testing, test scheduler, async testing, integration testing
tags:
  - Testing
  - JUnit
  - Integration Tests
---

# JUnit Tests Triggers and Tasks

## Disable SchedulerService

The `SchedulerService` can be disabled for unit testing, which ensures that no trigger will be
executed automatically.

```yaml
spring:
    persistent-tasks:
        scheduler-enabled: false
```

## Test support @since v1.6

With 1.6 where is a `PersistentTaskTestService` and other helper methods like:

-   runNextTrigger
-   scheduleNextTriggersAndWait

```xml
<dependency>
    <groupId>org.sterl.spring</groupId>
    <artifactId>spring-persistent-tasks-test</artifactId>
    <version>x.x.x</version>
</dependency>
```

You may either include it in the component scan or build it manually in your test config:

```java
@TestConfiguration
static class Config {
    @Bean
    PersistentTaskTestService persistentTaskTestService(List<SchedulerService> schedulers, TriggerService triggerService) {
        return new PersistentTaskTestService(schedulers, triggerService);
    }
}
```

This class adds some test methods to wait, run or assert triggers.

### Run & assert with PersistentTaskTestService

For the common synchronous case (scheduler disabled) these helpers are usually all you need — they run
triggers one-by-one (so a trigger that queues another is picked up) and assert the outcome, without
hand-rolling loops over `TriggerService`:

```java
@Autowired
private PersistentTaskTestService taskTest;

@Test
void testTaskRuns() {
    // GIVEN something queued a trigger (e.g. an event listener, a scheduler's planner, ...)
    myService.doSomethingThatQueuesATrigger();

    // run every trigger due up to a point in time (use a future instant to also run staggered/retry ones)
    taskTest.runAllDueTrigger(OffsetDateTime.now());

    // ...or run just the next due trigger and assert its final status (and optionally its key)
    taskTest.assertHasNextTask(TriggerStatus.SUCCESS, TriggerKey.of("my-id", MyTask.ID));
    taskTest.assertNextTaskSuccess(); // shorthand for the next trigger ending SUCCESS
    taskTest.assertNoMoreTriggers();  // nothing left to run
}
```

Available helpers (see `PersistentTaskTestService`):

| Method | Purpose |
|--------|---------|
| `runNextTrigger()` | Run the next due trigger; returns it as `Optional<RunningTriggerEntity>`. |
| `runAllDueTrigger(OffsetDateTime dueUntil)` | Run all triggers due up to `dueUntil`, one by one (new ones are picked up). |
| `assertHasNextTask()` | Run the next trigger and assert one existed. |
| `assertHasNextTask(TriggerStatus status, TriggerKey key)` | Run the next trigger and assert its status (and key, if given). |
| `assertNextTaskSuccess()` | Run the next trigger and assert it ended `SUCCESS`. |
| `assertNoMoreTriggers()` | Assert there is no further trigger to run. |
| `hasRunningTriggers()` / `countRunningTriggers()` | Inspect what the schedulers are currently running (async setups). |

::: tip Single-flight per key is testable without threads
A trigger keyed by a stable business id (`.id("invoices")`) means the engine keeps **at most one
trigger per key**: re-queuing a *waiting* one updates it in place, queuing while it is *running* throws
`IllegalStateException`. So "the same work never runs twice concurrently" is a property you can assert
by triggering the producer twice and checking there is still exactly **one** trigger for that key — no
concurrency in the test. To make a producer idempotent, skip when `tasks.getLastTriggerData(key)` is in
`TriggerStatus.ACTIVE_STATES`, or just queue and catch the `IllegalStateException`.
:::

## Manually run one task

Now you can run any trigger manually using the `TriggerService`

::: tip
Using the `TriggerService` is recommended to see any errors which might arise.
:::

```java
    @Autowired
    private TriggerService triggerService;

    @Test
    void testRunTriggerDirectly() {
        // GIVEN
        // setup your test and create any triggers needed
        var trigger = TriggerBuilder
                .<Vehicle>newTrigger("task2")
                .id("my-id") // will overwrite existing triggers
                .state(new Vehicle("funny"))
                .build();

        // WHEN create and directly run this trigger
        triggerService.run(triggerService.queue(trigger));

        // THEN
        // any asserts you might need
    }

    @Test
    void testRunUnknownTriggersCreated() {
        // GIVEN
        // setup your test call any method which might create triggers

        // WHEN run next pending trigger synchronously
        triggerService.run(triggerService.lockNextTrigger("test"));

        // THEN
        // any asserts you might need
    }
```

## Run all queued tasks

Sometimes it might be useful quickly to execute all running tasks. This sample adds a method e.g. to your base test class which will trigger any task which is now due to be executed.

```java
@Autowired
protected TriggerService triggerService;

/**
 * Run all pending triggers synchronously
 */
protected int waitForDbSchedulerTasks() {
    RunningTriggerEntity t;
    int count = 0;
    while ((t = triggerService.lockNextTrigger("test")) != null) {
        triggerService.run(t);
        ++count;
    }
    return count;
}
```

## Run all queued tasks in the future & retries

A common use case is run tasks which should run in the future or just to wait that all retries are exceeded.

```java
@Autowired
protected TriggerService triggerService;

protected int waitForDbSchedulerTasks(OffsetDateTime thenToRun) {
    List<RunningTriggerEntity> triggers;
    int count = 0;
    while (!(triggers = triggerService.lockNextTrigger("test", 1, thenToRun)).isEmpty()) {
        triggerService.run(triggers.get(0));
        ++count;
    }
    return count;
}
```

## Async execution of Triggers

It is also possible to define a test scheduler and use the async way to execute any triggers (without the spring scheduler which would trigger them automatically).

::: info
Any errors are now in the log and are handled by the framework with retries etc.
:::

```java
@Configuration
public static class TestConfig {
    // @Primary // if more than one
    @Bean(name = "schedulerB", initMethod = "start", destroyMethod = "stop")
    SchedulerService schedulerB(
            MeterRegistry meterRegistry,
            TriggerService triggerService,
            EditSchedulerStatusComponent editSchedulerStatus,
            TransactionTemplate trx) throws UnknownHostException {

        return SchedulerConfig.newSchedulerService("schedulerB",
                meterRegistry,
                triggerService,
                editSchedulerStatus,
                SchedulerThreadFactory.VIRTUAL_THREAD_POOL_FACTORY,
                7,
                Duration.ofSeconds(1),
                trx);
    }
}
```

Now the `PersistentTaskService` has a method to trigger or to trigger and to wait for the result:

```java
@Autowired
private PersistentTaskService persistentTaskService;

@Test
void testFoo() {
    // GIVEN
    // setup your test and create any triggers needed

    // WHEN run any pending triggers asynchronously - but with and wait
    var triggerKeys = persistentTaskService.executeTriggersAndWait();
    // OR queue all triggers asynchronously - and return the futures
    var futureTriggerKeys = persistentTaskService.executeTriggers();

    // THEN
    // any asserts you might need
}
```

## Waiting for all task which might trigger other tasks

In some cases we have longer running triggers which may trigger new jobs. As so we have to wait.

```java
Awaitility.await().atMost(Duration.ofMillis(1500)).until(() -> { // set the max wait time as needed
    waitForDbSchedulerTasks();
    return //* insert here your assert for the last task you wait for */;
});
```

## Clean up and restart as needed

During the setup and cleanup it is possible to cancel any pending triggers, pick what is needed in your case. In general it is usually enough in your tests to wait for any pending triggers and cancel any others.

### Minimal cleanup

```java
    @Autowired
    protected TriggerService triggerService;
    @Autowired
    protected PersistentTaskTestService persistentTaskTestService;

    @BeforeEach
    public void beforeEach() {
        triggerService.deleteAll();
        try {
            persistentTaskTestService.awaitRunningTriggers();
        } catch (Exception e) {
            System.err.println("awaitRunningTriggers has an error, do we care? No! " + e.getMessage());
        }
    }
```

### Full clean up and restart

```java
    @Autowired
    protected SchedulerService schedulerService;
    @Autowired
    protected TriggerService triggerService;
    @Autowired
    protected TaskService taskService;
    @Autowired
    protected HistoryService historyService;
    @Autowired
    private ThreadPoolTaskExecutor triggerHistoryExecutor;

    @BeforeEach
    public void beforeEach() throws Exception {
        triggerService.deleteAll();
        historyService.deleteAll();
        // restore any changes to the thread count
        schedulerService.setMaxThreads(10);
        // optional only needed if you shutdown
        schedulerService.start();
    }

    @AfterEach
    public void afterEach() throws Exception {
        // will cancel any pending tasks, optional
        schedulerService.shutdownNow(); // use .stop() if you want to wait

        // ensure the history is written completely
        awaitHistoryThreads();
    }

    protected void awaitHistoryThreads() {
        Awaitility.await().until(() -> triggerHistoryExecutor.getActiveCount() == 0);
    }
```