---
title: Trigger Life Cycle Events
description: Learn about trigger life cycle events in Spring Persistent Tasks including TriggerAddedEvent, TriggerRunningEvent, TriggerSuccessEvent, TriggerFailedEvent, and TriggerCanceledEvent.
keywords: trigger events, life cycle events, spring event listener, trigger states, task monitoring, event driven tasks
tags:
  - Life Cycle
  - Events
  - Monitoring
  - Event Listener
---

# Trigger Life Cycle Events

Any trigger follows a particular life cycle having the status:

1. WAITING => TriggerAddedEvent
1. RUNNING => TriggerRunningEvent
1. SUCCESS => TriggerSuccessEvent
1. FAILED => TriggerFailedEvent
1. CANCELED => TriggerCanceledEvent

![TriggerLifeCycleEvent](/assets/trigger-life-cycle-events.png)

# Create a custom life cycle listener

Based on this events custom trigger listeners may be build to react to any kind of state change of a trigger e.g. like the build in `MDC` listener:

```java
@Component
public class MdcTriggerInterceptor {

    public static final String TASK_NAME = "taskName";
    public static final String TASK_ID = "taskId";

    @EventListener
    public void beforeRun(TriggerRunningEvent data) {
        MDC.put(TASK_NAME, data.key().getTaskName());
        MDC.put(TASK_ID, data.key().getId());
    }
    @EventListener
    public void onFailed(TriggerFailedEvent data) {
        MDC.remove(TASK_NAME);
        MDC.remove(TASK_ID);
    }
    @EventListener
    public void onSuccess(TriggerSuccessEvent data) {
        MDC.remove(TASK_NAME);
        MDC.remove(TASK_ID);
    }
}
```