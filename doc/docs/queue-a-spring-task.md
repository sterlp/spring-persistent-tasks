# Queue a task execution

You can queue a task or a later execution or request the executer to run a task directly, if a free thread is available on the node.

## Direct usage of the `TriggerService` or `PersistentTaskService`.

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

## Build complex Trigger

```java
private final PersistentTaskService persistentTaskService;

public void buildVehicle() {
   var trigger = TaskTriggerBuilder
            .<Vehicle>newTrigger("task2")
            .id("my-id") // will overwrite existing triggers
            .state(new Vehicle("funny"))
            .runAfter(Duration.ofHours(2))
            .build();

    persistentTaskService.runOrQueue(trigger);
}
```

## Using a Spring Application Event

```java
private final ApplicationEventPublisher eventPublisher;

public void buildVehicle() {
    // Vehicle has to be Serializable
    final var v = new Vehicle();
    // send an event with the trigger inside - same as calling the PersistentTaskService
    eventPublisher.publishEvent(TriggerTaskCommand.of(BuildVehicleTask.ID.newUniqueTrigger(v)));
}
```
