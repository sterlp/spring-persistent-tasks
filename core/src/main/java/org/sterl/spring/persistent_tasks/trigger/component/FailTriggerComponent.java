package org.sterl.spring.persistent_tasks.trigger.component;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.sterl.spring.persistent_tasks.api.task.PersistentTask;
import org.sterl.spring.persistent_tasks.task.exception.CancelTaskException;
import org.sterl.spring.persistent_tasks.task.exception.FailTaskNoRetryException;
import org.sterl.spring.persistent_tasks.trigger.model.RunTaskWithStateCommand;
import org.sterl.spring.persistent_tasks.trigger.model.RunningTriggerEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FailTriggerComponent {

    private final EditTriggerComponent editTrigger;
    
    public Optional<RunningTriggerEntity> execute(RunTaskWithStateCommand runTaskWithStateCommand, Exception e) {

        var trigger = runTaskWithStateCommand.trigger();
        var task = runTaskWithStateCommand.task();
        var state = runTaskWithStateCommand.state();
        return execute(task, trigger, state, e);
    }

    /**
     * Fails the given trigger, no retry will be applied!
     */
    public <T extends Serializable> Optional<RunningTriggerEntity> execute(RunningTriggerEntity trigger, Exception e) {
        return execute(null, trigger, null, e);
    }

    public <T extends Serializable> Optional<RunningTriggerEntity> execute(
            @Nullable PersistentTask<T> task, 
            RunningTriggerEntity trigger, 
            @Nullable T state,
            Exception e) {

        Optional<RunningTriggerEntity> result;

        if (e instanceof CancelTaskException) {
            log.info("Cancel of a running trigger={} requested", trigger.getKey());
            result = editTrigger.cancelTask(trigger.getKey(), e);
        } else {
            result = triggerFailed(task, trigger, state, e);
        }
        return result;
    }

    private <T extends Serializable> Optional<RunningTriggerEntity> triggerFailed(
            @Nullable PersistentTask<T> task,
            RunningTriggerEntity trigger, 
            @Nullable T state, Exception e) {
        
        final var retryAt = determineWhenToRetry(task, trigger, e);
        var result = editTrigger.failTrigger(trigger.getKey(), state, e, retryAt);

        invokeTriggerErrorCallback(task, retryAt, state, e);

        return result;
    }

    private <T extends Serializable> void invokeTriggerErrorCallback(
            PersistentTask<T> task,
            final OffsetDateTime retryAt, T state, Exception e) {
        if (task != null && retryAt == null) {
            try {
                task.afterTriggerFailed(state, e);
            } catch (Exception ex) {
                log.error("Failed to invoke afterTriggerFailed on {}", task.getClass(), e);
            }
        }
    }

    /**
     * @return <code>null</code> no retry, otherwise the time when to retry
     */
    private <T extends Serializable> OffsetDateTime determineWhenToRetry(
            @Nullable PersistentTask<T> task,
            RunningTriggerEntity trigger, Exception e) {

        final OffsetDateTime retryAt;
        if (task == null) {
            retryAt = null;
            log.warn("No task found for trigger key={}", trigger.key());
        } else if (e instanceof FailTaskNoRetryException) {
            log.info("No retry for trigger={} requested", trigger.getKey(), e);
            retryAt = null;
        } else {
            var failCount = trigger.getData().getExecutionCount();
            var shouldRetry = task.retryStrategy().shouldRetry(failCount, e);
            retryAt = shouldRetry ? task.retryStrategy().retryAt(failCount, e) : null;

            if (retryAt == null) {
                log.error("Failed={} trigger={}, no further retries!", failCount,
                        trigger.getKey(), e);
            } else {
                log.warn("Failed={} trigger={} with retryAt={}", failCount,
                        trigger.getKey(), retryAt, e);
            }
        }
        return retryAt;
    }
}
