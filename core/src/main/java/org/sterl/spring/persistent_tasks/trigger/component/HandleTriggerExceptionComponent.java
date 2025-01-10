package org.sterl.spring.persistent_tasks.trigger.component;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.trigger.component.RunTriggerComponent.TaskAndState;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Transactional(timeout = 30)
@RequiredArgsConstructor
@Slf4j
public class HandleTriggerExceptionComponent {

    private final EditTriggerComponent editTrigger;

    Optional<TriggerEntity> execute(TaskAndState taskAndState,
            @Nullable Exception e) {

        var trigger = taskAndState.trigger;
        var task = taskAndState.persistentTask;
        var result = editTrigger.completeTaskWithStatus(trigger.getKey(), taskAndState.state, e);

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
