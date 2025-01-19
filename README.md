[![Java CI with Maven](https://github.com/sterlp/spring-persistent-tasks/actions/workflows/build.yml/badge.svg)](https://github.com/sterlp/spring-persistent-tasks/actions/workflows/build.yml)

# Spring Persistent Tasks

A simple task management framework designed to queue and execute asynchronous tasks with support for database persistence and a user-friendly interface. It can be used to implement scheduling patterns or outbound patterns.

Focus is the usage with spring boot and JPA.

Secondary goal is to support [Poor mans Workflow](https://github.com/sterlp/pmw)

# Documentation

Use for more advanced doc the [WIKI](https://github.com/sterlp/spring-persistent-tasks/wiki).
The README contains a shorter how to use.

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

# JavaDoc

-   [JavaDoc](https://sterlp.github.io/spring-persistent-tasks/javadoc-core/org/sterl/spring/persistent_tasks/PersistentTaskService.html)

# Maven setup

-   [Maven Central spring-persistent-tasks-core](https://central.sonatype.com/artifact/org.sterl.spring/spring-persistent-tasks-core/versions)

```xml
<dependency>
    <groupId>org.sterl.spring</groupId>
    <artifactId>spring-persistent-tasks-core</artifactId>
    <version>1.x.x</version>
</dependency>
```

# Setup Spring

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
public class BuildVehicleTask implements PersistentTask<Vehicle> {

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

Consider setting a timeout to the `TransactionTemplate`:

```java
@Bean
TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
    TransactionTemplate template = new TransactionTemplate(transactionManager);
    template.setTimeout(10);
    return template;
}
```

### As a closure

Simple task will use defaults:

- Not a transactional task, e.g. HTTP calls
- 4 executions, one regular and 3 retries, linear
- using minutes with an offset of 1 which is added to now

```java
@Bean
PersistentTask<Vehicle> task1(VehicleHttpConnector vehicleHttpConnector) {
    return v -> vehicleHttpConnector.send(v);
}
```

### Task Transaction Management

[Transaction-Management Task](https://github.com/sterlp/spring-persistent-tasks/wiki/Transaction-Management)

## Queue a task execution

### Direct usage of the `TriggerService` or `PersistentTaskService`.

```java
private final TriggerService triggerService;
private final PersistentTaskService persistentTaskService;

public void buildVehicle() {
    // Vehicle has to be Serializable
    final var v = new Vehicle();
    // set any data to v ...

    // EITHER: queue it - will always run later
    triggerService.queue(BuildVehicleTask.ID.newUniqueTrigger(v));

    // OR: will queue it and run it now if possible.
    // if the scheduler service is missing it is same as using the TriggerService
    persistentTaskService.runOrQueue(BuildVehicleTask.ID.newUniqueTrigger(v));
}
```

### JUnit Tests

- [Persistent Task and Testing](https://github.com/sterlp/spring-persistent-tasks/wiki/Triggers-and-Tasks-in-JUnit-Tests)

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

# Alternatives

-   quartz
-   db-scheduler
-   jobrunr
