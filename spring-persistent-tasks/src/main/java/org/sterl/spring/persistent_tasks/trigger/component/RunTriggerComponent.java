package org.sterl.spring.persistent_tasks.trigger.component;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.sterl.spring.persistent_tasks.api.Task;
import org.sterl.spring.persistent_tasks.task.TaskService;
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
    public Optional<TriggerEntity> execute(@Nullable TriggerEntity trigger) {
        if (trigger == null) {
            return Optional.empty();
        }
        Task<Serializable> task = null;
        try {
            task = taskService.assertIsKnown(trigger.newTaskId());
            task.accept(serializer.deserialize(trigger.getData().getState()));
            var result = editTrigger.completeTaskWithSuccess(trigger.getKey());
            editTrigger.deleteTrigger(trigger);
            return result;
        } catch (Exception e) {
            return handleTaskException(trigger, task, e);
        }
    }

    private Optional<TriggerEntity> handleTaskException(TriggerEntity trigger,
            @Nullable Task<Serializable> task,
            @Nullable Exception e) {

        var result = editTrigger.completeTaskWithStatus(trigger.getKey(), e);

        if (task != null &&
                task.retryStrategy().shouldRetry(trigger.getData().getExecutionCount(), e)) {

            final OffsetDateTime retryAt = task.retryStrategy().retryAt(trigger.getData().getExecutionCount(), e);
            log.warn("{} failed, retry will be done at={}!",
                    trigger.getKey(), retryAt, e);

            result = editTrigger.retryTrigger(trigger.getKey(), retryAt);
        } else {
            log.error("{} failed, no more retries! {}", trigger.getKey(), 
                    e == null ? "No exception given." : e.getMessage());
            
            editTrigger.deleteTrigger(trigger);
        }
        return result;
    }

}
