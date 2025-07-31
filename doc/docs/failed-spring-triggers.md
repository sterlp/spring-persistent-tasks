# Reacting to Trigger Failures

## Use callback since v2.2.0

The Trigger interface also gives the ability to listen for `TriggerFailedEvent` and `TriggerExpiredEvent` directly in the trigger, without the need to filter:

```java

@Bean PersistentTask<String> myTask() {
return new PersistentTask<>() {
        @Override
        public void accept(@Nullable String state) {
            // any code
        }

        /**
         * Callback handler which is invoked once <b>after</b>:
         * <ul>
         * <li> if the trigger is finally failed
         * <li> or the trigger is abandoned
         * </ul>
         * <br>
         * This method is not invoked for expired triggers waiting for an signal.
         *
         * @param state the state, could be <code>null</code> if the state could be parsed
         * @param e the exception reason - could also be a {@link FailTaskNoRetryException}
         * @see <a href="https://spring-persistent-task.sterl.org/failed-spring-triggers/">Failed trigger</a>
         */
        public void afterTriggerFailed(String state, Exception e) {
            // custom error handler
        }
    }
}

```

## Using life cycle events

Each trigger goes through a trigger lifecycle with the [corresponding events](./life-cycle-events.md). One of these events, `TriggerFailedEvent`, allows you to hook into a trigger’s error handling.

!!! info

    - Events are fired for all triggers, so a filter is needed to ensure the event corresponds to the current trigger.
    - Events are fired even if a trigger will retry; a check is needed to determine whether the handler should only execute when no retries are planned.

### Events which indicate problems

1. TriggerFailedEvent
1. TriggerExpiredEvent
1. TriggerCanceledEvent

### Hook into a Failed Trigger

The default use case is to get notified if a trigger has failed and will not be retried.

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
void onTriggerFailed(TriggerFailedEvent failed) {
    if (TASK_NAME.equals(failed.key().getTaskName()) && failed.isDone()) {
        // trigger will not retry anymore
    }
}
```

### Join the failed trigger transaction

In this example, the following code joins the trigger/framework transaction.

```java
@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
void onTriggerFailed(TriggerFailedEvent failed) {
    if (TASK_NAME.equals(failed.key().getTaskName()) && failed.isDone()) {
        // trigger will not retry anymore
    }
}
```
