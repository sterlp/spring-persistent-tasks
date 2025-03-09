package org.sterl.spring.persistent_tasks.trigger.component;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.api.task.RunningTriggerContextHolder;
import org.sterl.spring.persistent_tasks.task.TaskService;
import org.sterl.spring.persistent_tasks.task.exception.CancelTaskException;
import org.sterl.spring.persistent_tasks.task.exception.FailTaskNoRetryException;
import org.sterl.spring.persistent_tasks.trigger.model.RunTaskWithStateCommand;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RunTriggerComponent {

    private final TaskService taskService;
    private final EditTriggerComponent editTrigger;
    private final StateSerializer serializer = new StateSerializer();

    /**
     * Will execute the given {@link TriggerEntity} and handle any errors etc.
     */
    @Transactional(propagation = Propagation.NEVER)
    public Optional<TriggerEntity> execute(TriggerEntity trigger) {
        if (trigger == null) {
            return Optional.empty();
        }

        final var runTaskWithState = buildTaskWithStateFor(trigger);
        // something went really wrong this trigger is crap
        if (runTaskWithState == null) return Optional.of(trigger);

        try {
            RunningTriggerContextHolder.setContext(runTaskWithState.runningTrigger());
            return runTaskWithState.execute(editTrigger);
        } catch (Exception e) {
            return failTaskAndState(runTaskWithState, e);
        } finally {
            RunningTriggerContextHolder.clearContext();
        }
    }

    @Nullable
    private RunTaskWithStateCommand buildTaskWithStateFor(TriggerEntity trigger) {
        try {
            final var task = taskService.assertIsKnown(trigger.newTaskId());
            final var trx = taskService.getTransactionTemplate(task);
            final var state = serializer.deserialize(trigger.getData().getState());
            return new RunTaskWithStateCommand(task, trx, state, trigger);
        } catch (Exception e) {
            failTaskAndState(new RunTaskWithStateCommand(null, Optional.empty(), null, trigger), e);
            return null;
        }
    }
    
    private Optional<TriggerEntity> failTaskAndState(RunTaskWithStateCommand runTaskWithStateCommand, Exception e) {

        var trigger = runTaskWithStateCommand.trigger();
        var task = runTaskWithStateCommand.task();
        Optional<TriggerEntity> result;

        if (e instanceof CancelTaskException) {
            log.info("Cancel of a running trigger={} requested", trigger.getKey());
            result = editTrigger.cancelTask(trigger.getKey(), e);
        } else if (e instanceof FailTaskNoRetryException) {
            log.warn("Fail no retry of a running trigger={} requested", trigger.getKey(), e);
            result = editTrigger.failTrigger(trigger.getKey(), runTaskWithStateCommand.state(), e, null);
        } else if (task == null 
                || !task.retryStrategy().shouldRetry(trigger.getData().getExecutionCount(), e)) {

            log.error("Failed trigger={}, no further retries!", trigger.getKey(), e);
            result = editTrigger.failTrigger(trigger.getKey(), runTaskWithStateCommand.state(), e, null);
        } else {
            final OffsetDateTime retryAt = task.retryStrategy().retryAt(trigger.getData().getExecutionCount(), e);
            log.warn("Failed trigger={} with retryAt={}", trigger.getKey(), retryAt, e);
            result = editTrigger.failTrigger(trigger.getKey(), runTaskWithStateCommand.state(), e, retryAt);
        }
        return result;
    }
}
