# Reacting to Trigger Failures

Each trigger goes through a trigger lifecycle with the [corresponding events](./life-cycle-events.md). One of these events, `TriggerFailedEvent`, allows you to hook into a trigger’s error handling.

!!! info

    - Events are fired for all triggers, so a filter is needed to ensure the event corresponds to the current trigger.
    - Events are fired even if a trigger will retry; a check is needed to determine whether the handler should only execute when no retries are planned.

## Hook into a Failed Trigger

The default use case is to get notified if a trigger has failed and will not be retried.

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
void onTriggerFailed(TriggerFailedEvent failed) {
    if (TASK_NAME.equals(failed.key().getTaskName()) && failed.isDone()) {
        // trigger will not retry anymore
    }
}
```

## Join the failed trigger transaction

In this example, the following code joins the trigger/framework transaction.

```java
@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
void onTriggerFailed(TriggerFailedEvent failed) {
    if (TASK_NAME.equals(failed.key().getTaskName()) && failed.isDone()) {
        // trigger will not retry anymore
    }
}
```
