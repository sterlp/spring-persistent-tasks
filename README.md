# Spring Persistent Tasks

A simple task framework which has it's focus being simple and to support [Poor mans Workflow](https://github.com/sterlp/pmw)

# Setup and Run a Task

## Maven

```xml
<dependency>
    <groupId>org.sterl.spring</groupId>
    <artifactId>spring-persistent-tasks-core</artifactId>
    <version>1.x.x</version>
</dependency>
```

## Setup a spring persitent task

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
}
```

## Queue a task execution

```java
    private final TriggerService triggerService;

    public void buildVehicle() {
        // Vehicle has to be Serializable
        final var v = new Vehicle();
        // set any data
        triggerService.queue(BuildVehicleTask.ID.newUniqueTrigger(v));
    }
```

# Setup DB

Liquibase is supported. Either import all or just the required versions:

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

```xml
<dependency>
    <groupId>org.sterl.spring</groupId>
    <artifactId>spring-persistent-tasks-ui</artifactId>
    <version>1.x.x</version>
</dependency>
```

## Open the UI

- http://localhost:8080/task-ui

## Triggers
![Triggers](screenshots/triggers-screen.png)

## History
![History](screenshots/history-screen.png)

# Alternatives

- quartz
- db-scheduler
- jobrunr