package org.sterl.spring.persistent_tasks.trigger.component;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.task.TaskService;
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
            return runTaskWithState.execute(editTrigger);
        } catch (Exception e) {
            return failTaskAndState(runTaskWithState, e);
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

        if (task != null 
                && task.retryStrategy().shouldRetry(trigger.getData().getExecutionCount(), e)) {

            final OffsetDateTime retryAt = task.retryStrategy().retryAt(trigger.getData().getExecutionCount(), e);

            result = editTrigger.failTrigger(trigger.getKey(), runTaskWithStateCommand.state(), e, retryAt);

        } else {
            log.error("{} failed, no more retries! {}", trigger.getKey(), 
                    e == null ? "No exception given." : e.getMessage(), e);
            
            result = editTrigger.failTrigger(trigger.getKey(), runTaskWithStateCommand.state(), e, null);
        }
        return result;
    }
}
