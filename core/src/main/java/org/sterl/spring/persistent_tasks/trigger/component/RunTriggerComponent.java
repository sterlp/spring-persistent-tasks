package org.sterl.spring.persistent_tasks.trigger.component;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.api.PersistentTask;
import org.sterl.spring.persistent_tasks.task.TaskService;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerRunningEvent;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RunTriggerComponent {

    private final TaskService taskService;
    private final EditTriggerComponent editTrigger;
    private final ApplicationEventPublisher eventPublisher;
    private final StateSerializer serializer = new StateSerializer();

    /**
     * Will execute the given {@link TriggerEntity} and handle any errors etc.
     */
    public Optional<TriggerEntity> execute(TriggerEntity trigger) {
        if (trigger == null) {
            return Optional.empty();
        }
        final var taskAndState = getTastAndState(trigger);
        // something went really wrong this trigger is crap
        if (taskAndState == null) return Optional.of(trigger);

        try {
            return taskAndState.call();
        } catch (Exception e) {
            return handleTaskException(taskAndState, e);
        }
    }

    @Nullable
    private TaskAndState getTastAndState(TriggerEntity trigger) {
        try {
            var task = taskService.assertIsKnown(trigger.newTaskId());
            var trx = taskService.getTransactionTemplate(task);
            var state = serializer.deserialize(trigger.getData().getState());
            return new TaskAndState(task, trx, state, trigger);
        } catch (Exception e) {
            // this trigger is somehow crap, no retry and done.
            handleTaskException(new TaskAndState(null, Optional.empty(), null, trigger), e);
            return null;
        }
    }
    @RequiredArgsConstructor
    private class TaskAndState {
        final PersistentTask<Serializable> persistentTask;
        final Optional<TransactionTemplate> trx;
        final Serializable state;
        final TriggerEntity trigger;

        Optional<TriggerEntity> call() {
            if (trx.isPresent()) {
                return trx.get().execute(t -> runTask());
            } else {
                return runTask();
            }
        }

        private Optional<TriggerEntity> runTask() {
            eventPublisher.publishEvent(new TriggerRunningEvent(trigger));

            persistentTask.accept(state);

            var result = editTrigger.completeTaskWithSuccess(trigger.getKey());
            editTrigger.deleteTrigger(trigger);

            return result;
        }
    }

    private Optional<TriggerEntity> handleTaskException(TaskAndState taskAndState,
            @Nullable Exception e) {

        var trigger = taskAndState.trigger;
        var task = taskAndState.persistentTask;
        var result = editTrigger.completeTaskWithStatus(trigger.getKey(), e);

        if (task != null 
                && task.retryStrategy().shouldRetry(trigger.getData().getExecutionCount(), e)) {

            final OffsetDateTime retryAt = task.retryStrategy().retryAt(trigger.getData().getExecutionCount(), e);

            result = editTrigger.retryTrigger(trigger.getKey(), retryAt);
            if (result.isPresent()) {
                var data = result.get().getData();
                log.warn("{} failed, retry will be done at={} status={}!",
                        trigger.getKey(), 
                        data.getRunAt(),
                        data.getStatus(),
                        e);
            } else {
                log.error("Trigger with key={} not found and may be at a wrong state!",
                        trigger.getKey(), e);
            }
        } else {
            log.error("{} failed, no more retries! {}", trigger.getKey(), 
                    e == null ? "No exception given." : e.getMessage(), e);
            
            editTrigger.deleteTrigger(trigger);
        }
        return result;
    }
}
