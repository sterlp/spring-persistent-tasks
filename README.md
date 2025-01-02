[![Java CI with Maven](https://github.com/sterlp/spring-persistent-tasks/actions/workflows/build.yml/badge.svg)](https://github.com/sterlp/spring-persistent-tasks/actions/workflows/build.yml)

# Spring Persistent Tasks

A simple task management framework designed to queue and execute asynchronous tasks with support for database persistence and a user-friendly interface. It can be used to implement scheduling patterns or outbound patterns.

Focus is the usage with spring boot and JPA.

Secondary goal is to support [Poor mans Workflow](https://github.com/sterlp/pmw)

# DBs for storage

## Tested in the pipeline

-   H2
-   azure-sql-edge (MSSQL)
-   PostgreSQL
-   MariaDB

![History](screenshots/supported-dbs.png)

## Supported in theory

-   MSSQL, as azure-sql-edge is tested

## Not supported

-   mySQL: sequences are not supported

# Setup and Run a Task

-   [JavaDoc](https://sterlp.github.io/spring-persistent-tasks/javadoc-core/org/sterl/spring/persistent_tasks/PersistentTaskService.html)

## Maven

-   [Maven Central spring-persistent-tasks-core](https://central.sonatype.com/artifact/org.sterl.spring/spring-persistent-tasks-core/versions)

```xml
<dependency>
    <groupId>org.sterl.spring</groupId>
    <artifactId>spring-persistent-tasks-core</artifactId>
    <version>1.x.x</version>
</dependency>
```

## Setup Spring

```java
@SpringBootApplication
@EnableSpringPersistentTasks
public class ExampleApplication {
```

## Setup a spring persistent task

### As a class

```java
@Component(BuildVehicleTask.NAME)
@RequiredArgsConstructor
@Slf4j
public class BuildVehicleTask implements SpringBeanTask<Vehicle> {

    private static final String NAME = "buildVehicleTask";
    public static final TaskId<Vehicle> ID = new TaskId<>(NAME);

    private final VehicleRepository vehicleRepository;

    @Override
    public void accept(Vehicle vehicle) {
        // do stuff
        // save
        vehicleRepository.save(vehicle);
    }
    // OPTIONAL
    @Override
    public RetryStrategy retryStrategy() {
        // run 5 times, multiply the execution count with 4, add the result in HOURS to now.
        return new MultiplicativeRetryStrategy(5, ChronoUnit.HOURS, 4)
    }
    // OPTIONAL
    // if the task in accept requires a DB transaction, join them together with the framework
    // if true the TransactionTemplate is used. Set here any timeouts.
    @Override
    public boolean isTransactional() {
        return true;
    }
}
```

### As a closure

Note: this example has no aspects as above the spring _@Transactional_

```java
@Bean
SpringBeanTask<Vehicle> task1(VehicleRepository vehicleRepository) {
    return v -> vehicleRepository.save(v);
}
```

## Queue a task execution

### Direct usage of the `TriggerService` or `PersistentTaskService`.

```java
    private final TriggerService triggerService;
    private final PersistentTaskService persistentTaskService;

    public void buildVehicle() {
        // Vehicle has to be Serializable
        final var v = new Vehicle();
        // set any data to v ...

        // queue it
        triggerService.queue(BuildVehicleTask.ID.newUniqueTrigger(v));
        // will queue it and run it if possible.
        // if the scheduler service is missing it is same as above
        persistentTaskService.runOrQueue(BuildVehicleTask.ID.newUniqueTrigger(v));
    }
```

### Build Trigger

```java
    private final TriggerService triggerService;

    public void buildVehicle() {
       var trigger = TaskTriggerBuilder
                .<Vehicle>newTrigger("task2")
                .id("my-id") // will overwrite existing triggers
                .state(new Vehicle("funny"))
                .runAfter(Duration.ofHours(2))
                .build()

        triggerService.queue(trigger);
    }
```

### Use a Spring Event

```java
    private final TriggerService triggerService;

    public void buildVehicle() {
        // Vehicle has to be Serializable
        final var v = new Vehicle();
        // set any data
        triggerService.queue(BuildVehicleTask.ID.newUniqueTrigger(v));
    }
```

### Triggers and Tasks in JUnit Tests

The `SchedulerService` can be disabled for unit testing, which ensures that no trigger will be
executed automatically.

```yml
spring:
    persistent-tasks:
        scheduler-enabled: false
```

Now you can run any trigger manually using the `TriggerService`

```java
    @Autowired
    private TriggerService triggerService;

    @Test
    void testRunTriggerDirectly() {
        // GIVEN
        // setup your test and create any triggers needed

        // WHEN run any pending triggers
        triggerService.run(triggerService.queue(trigger));

        // THEN
        // any asserts you might need
    }

    @Test
    void testRunUnknownTriggersCreated() {
        // GIVEN
        // setup your test call any method which might create triggers

        // WHEN run any pending triggers
        triggerService.run(triggerService.lockNextTrigger("test"));

        // THEN
        // any asserts you might need
    }
```

It is also possible to define a test scheduler and use the async way to execute any triggers (without the spring scheduler which would trigger them automatically):

```java
    @Configuration
    public static class TestConfig {

        @Primary
        @SuppressWarnings("resource")
        SchedulerService schedulerService(TriggerService triggerService, EditSchedulerStatusComponent editSchedulerStatus,
                TransactionTemplate trx) throws UnknownHostException {

            final var taskExecutor = new TaskExecutorComponent(triggerService, 10);
            taskExecutor.setMaxShutdownWaitTime(Duration.ofSeconds(0));
            return new SchedulerService("testScheduler", triggerService, taskExecutor, editSchedulerStatus, trx);
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

        // WHEN run any pending triggers
        persistentTaskService.executeTriggersAndWait();

        // THEN
        // any asserts you might need
    }
```

During the setup and cleanup it is possible to cancel any pending triggers:

```java
    @BeforeEach
    public void beforeEach() throws Exception {
        triggerService.deleteAll();
        historyService.deleteAll();
        schedulerService.setMaxThreads(10);
        schedulerService.start();
    }

    @AfterEach
    public void afterEach() throws Exception {
        // will cancel any pending tasks
        schedulerService.shutdownNow(); // use .stop() if you want to wait
    }
```

### Spring configuration options

| Property                                       | Type                 | Description                                                              | Default Value      |
| ---------------------------------------------- | -------------------- | ------------------------------------------------------------------------ | ------------------ |
| `spring.persistent-tasks.poll-rate`            | `java.lang.Integer`  | The interval at which the scheduler checks for new tasks, in seconds.    | `30`               |
| `spring.persistent-tasks.max-threads`          | `java.lang.Integer`  | The number of threads to use; set to 0 to disable task processing.       | `10`               |
| `spring.persistent-tasks.task-timeout`         | `java.time.Duration` | The maximum time allowed for a task and scheduler to complete a task.    | `PT5M` (5 minutes) |
| `spring.persistent-tasks.poll-task-timeout`    | `java.lang.Integer`  | The interval at which the system checks for abandoned tasks, in seconds. | `300` (5 minutes)  |
| `spring.persistent-tasks.scheduler-enabled`    | `java.lang.Boolean`  | Indicates whether this node should handle triggers.                      | `true`             |
| `spring.persistent-tasks.history.delete-after` | `java.time.Duration` | The max age for a trigger in the hstory.                                 | `PT72H` (30 days)  |
| `spring.persistent-tasks.history.delete-rate`  | `java.time.Integer`  | The interval at which old triggers are deleted, in hours.                | `24` (24 hours)    |

# Setup DB with Liquibase

Liquibase is supported. Either import all or just the required versions.

## Maven

-   [Maven Central spring-persistent-tasks-db](https://central.sonatype.com/artifact/org.sterl.spring/spring-persistent-tasks-db/versions)

## Option 1: Just include the master file

```xml
<include file="spring-persistent-tasks/db.changelog-master.xml" />
```

## Option 2: import changesets on by one

```xml
<include file="spring-persistent-tasks/db/pt-changelog-v1.xml" />
```

# Setup UI

## Maven

-   [Maven Central spring-persistent-tasks-ui](https://central.sonatype.com/artifact/org.sterl.spring/spring-persistent-tasks-ui/versions)

```xml
<dependency>
    <groupId>org.sterl.spring</groupId>
    <artifactId>spring-persistent-tasks-ui</artifactId>
    <version>1.x.x</version>
</dependency>
```

## Setup Spring

```java
@SpringBootApplication
@EnableSpringPersistentTasks
@EnableSpringPersistentTasksUI
public class ExampleApplication {
```

## Open the UI

-   http://localhost:8080/task-ui

## Schedulers

![Schedulers](screenshots/schedulers-screen.png)

## Triggers

![Triggers](screenshots/triggers-screen.png)

## History

![History](screenshots/history-screen.png)

## Spring Boot CSRF config for the UI

Axios should work with the following spring config out of the box with csrf:

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .httpBasic(org.springframework.security.config.Customizer.withDefaults())
        .csrf(c ->
            c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
             .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
        );
    return http.build();
}
```

more informations: https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html

# Alternatives

-   quartz
-   db-scheduler
-   jobrunr
