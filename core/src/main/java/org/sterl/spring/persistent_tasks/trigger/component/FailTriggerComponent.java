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
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FailTriggerComponent {

    private final EditTriggerComponent editTrigger;
    
    public Optional<TriggerEntity> execute(RunTaskWithStateCommand runTaskWithStateCommand, Exception e) {

        var trigger = runTaskWithStateCommand.trigger();
        var task = runTaskWithStateCommand.task();
        var state = runTaskWithStateCommand.state();
        return execute(task, trigger, state, e);
    }

    /**
     * Fails the given trigger, no retry will be applied!
     */
    public <T extends Serializable> Optional<TriggerEntity> execute(TriggerEntity trigger, Exception e) {
        return execute(null, trigger, null, e);
    }
    public <T extends Serializable> Optional<TriggerEntity> execute(
            @Nullable PersistentTask<T> task, 
            TriggerEntity trigger, 
            @Nullable T state,
            Exception e) {

        Optional<TriggerEntity> result;

        if (e instanceof CancelTaskException) {
            log.info("Cancel of a running trigger={} requested", trigger.getKey());
            result = editTrigger.cancelTask(trigger.getKey(), e);
        } else if (e instanceof FailTaskNoRetryException) {
            log.warn("Fail no retry of a running trigger={} requested", trigger.getKey(), e);
            result = editTrigger.failTrigger(trigger.getKey(), state, e, null);
        } else if (task == null 
                || !task.retryStrategy().shouldRetry(trigger.getData().getExecutionCount(), e)) {

            log.error("Failed trigger={}, no further retries!", trigger.getKey(), e);
            result = editTrigger.failTrigger(trigger.getKey(), state, e, null);
        } else {
            final OffsetDateTime retryAt = task.retryStrategy().retryAt(trigger.getData().getExecutionCount(), e);
            if (retryAt == null) {
                log.error("Failed trigger={}, no further retries!", trigger.getKey(), e);
            } else {
                log.warn("Failed trigger={} with retryAt={}", trigger.getKey(), retryAt, e);
            }
            result = editTrigger.failTrigger(trigger.getKey(), state, e, retryAt);
        }
        return result;
    }
}
