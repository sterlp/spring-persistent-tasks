---
title: Queue and Execute Tasks
description: Learn how to queue tasks for later execution or run them immediately using TriggerService and PersistentTaskService with Spring application events.
keywords: queue task, trigger task, task execution, spring events, task scheduling, deferred execution, immediate execution
tags:
  - Task Queue
  - Task Execution
  - TriggerService
  - Spring Events
---

# Queue a task execution

![Triggers](/assets/triggers.png)

You can queue a task for later execution or request the executor to run a task directly if a free thread is available on the node.

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
   var trigger = TriggerBuilder
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