package org.sterl.spring.persistent_tasks.trigger.component;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional(propagation = Propagation.NEVER)
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
            return failTaskAndState(taskAndState, e);
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
            failTaskAndState(new TaskAndState(null, Optional.empty(), null, trigger), e);
            return null;
        }
    }
    
    private Optional<TriggerEntity> failTaskAndState(TaskAndState taskAndState, Exception e) {

        var trigger = taskAndState.trigger;
        var task = taskAndState.persistentTask;
        Optional<TriggerEntity> result;

        if (task != null 
                && task.retryStrategy().shouldRetry(trigger.getData().getExecutionCount(), e)) {

            final OffsetDateTime retryAt = task.retryStrategy().retryAt(trigger.getData().getExecutionCount(), e);

            result = editTrigger.failTrigger(trigger.getKey(), taskAndState.state, e, retryAt);

        } else {
            log.error("{} failed, no more retries! {}", trigger.getKey(), 
                    e == null ? "No exception given." : e.getMessage(), e);
            
            result = editTrigger.failTrigger(trigger.getKey(), taskAndState.state, e, null);
        }
        return result;
    }
    
    @RequiredArgsConstructor
    class TaskAndState {
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
            if (!trigger.isRunning()) trigger.runOn(trigger.getRunningOn());
            eventPublisher.publishEvent(new TriggerRunningEvent(
                    trigger.getId(), trigger.copyData(), state, trigger.getRunningOn()));

            persistentTask.accept(state);

            var result = editTrigger.completeTaskWithSuccess(trigger.getKey(), state);
            editTrigger.deleteTrigger(trigger);

            return result;
        }
    }
}
