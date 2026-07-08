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

## Priority

Use priority to let important work win the worker. Every trigger has a **priority** (`0-9`, higher is picked earlier — same idea as JMS). It defaults to
`TriggerRequest.DEFAULT_PRIORITY` (**4**). When several triggers are due and a worker thread frees up,
the engine picks the **highest-priority waiting** trigger next.

```java
var trigger = TriggerBuilder
        .<Folder>newTrigger("index-folder")
        .id(folder.id())     // single-flight per folder
        .priority(1)         // below the default 4 → background, yields to other work
        .build();

persistentTaskService.runOrQueue(trigger);
```

This matters most when worker threads are scarce (e.g. `persistent-tasks.max-threads: 1`, a single
in-flight task at a time). Give long-running **background** jobs a **low** priority so latency-sensitive
or short tasks are picked first. For example, a bulk re-indexing job set to `priority(1)` lets a
freshly queued user-triggered task (default priority `4`) jump ahead of any not-yet-started index work.

::: warning Priority orders, it does not preempt
A higher priority only decides which **waiting** trigger is picked **next**; it never interrupts a
trigger that is already running. With a single worker, a long task already in flight still has to
finish before the next one starts — so prioritise by keeping background units small and frequent
rather than expecting them to be paused mid-run.
:::

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