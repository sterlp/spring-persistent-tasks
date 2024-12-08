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
            task = taskService.assertIsKnown(trigger.getId().toTaskId());
            task.accept(serializer.deserialize(trigger.getData().getState()));
            return editTrigger.completeTaskWithSuccess(trigger.getId());
        } catch (Exception e) {
            return handleTaskException(trigger, task, e);
        }
    }

    private Optional<TriggerEntity> handleTaskException(TriggerEntity trigger,
            @Nullable Task<Serializable> task,
            @Nullable Exception e) {

        var result = editTrigger.completeTaskWithStatus(trigger.getId(), e);

        if (task != null &&
                task.retryStrategy().shouldRetry(trigger.getData().getExecutionCount(), e)) {

            final OffsetDateTime retryAt = task.retryStrategy().retryAt(trigger.getData().getExecutionCount(), e);
            log.info("Task={} failed, retry will be done at={}!",
                    trigger, retryAt, e);

            result = editTrigger.retryTrigger(trigger.getId(), retryAt);
        }
        return result;
    }

}
