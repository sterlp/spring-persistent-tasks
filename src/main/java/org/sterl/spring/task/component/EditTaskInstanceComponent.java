package org.sterl.spring.task.component;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.task.api.TaskTrigger;
import org.sterl.spring.task.model.TaskStatus;
import org.sterl.spring.task.model.TaskTriggerEntity;
import org.sterl.spring.task.model.TaskTriggerId;
import org.sterl.spring.task.repository.TaskInstanceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Transactional
@Slf4j
@RequiredArgsConstructor
public class EditTaskInstanceComponent {
    private final StateSerializer stateSerializer = new StateSerializer();
    private final TaskInstanceRepository taskInstanceRepository;

    public void completeWithRetry(TaskTriggerId id, Exception e, OffsetDateTime when) {
        taskInstanceRepository.findById(id).ifPresent(t -> {
            t.complete(TaskStatus.NEW, e);
            t.setStart(when);
            log.debug("Retrying task={} error={}", id, e.getClass());
        });
    }
    
    public void completeTaskWithStatus(TaskTriggerId id, TaskStatus newStatus, Exception e) {
        taskInstanceRepository.findById(id).ifPresent(t -> {
            t.complete(newStatus, e);
            log.debug("Setting task={} to status={} {}", id, newStatus, 
                    e == null ? "" : "error=" + e.getClass().getSimpleName());
        });
    }

    public <T extends Serializable> TaskTriggerId addTrigger(TaskTrigger<T> tigger) {
        var t = toTriggerEntity(tigger);
        taskInstanceRepository.save(t);
        return t.newInstanceId();
    }

    public <T extends Serializable> List<TaskTriggerId> addTriggers(Collection<TaskTrigger<T>> newTriggers) {
        return taskInstanceRepository
            .saveAll(newTriggers.stream().map(this::toTriggerEntity).toList())
            .stream().map(TaskTriggerEntity::newInstanceId)
            .toList();
    }
    public void triggerAll(Collection<TaskTrigger<?>> newTriggers) {
        taskInstanceRepository.saveAll(newTriggers.stream().map(this::toTriggerEntity).toList());
    }

    private <T extends Serializable> TaskTriggerEntity toTriggerEntity(TaskTrigger<T> trigger) {
        var id = trigger.taskId();
        byte[] state = stateSerializer.serialize(trigger.state());
        var t = TaskTriggerEntity.builder()
            .id(trigger.id())
            .name(id.name())
            .taskGroup(id.group())
            .start(trigger.when())
            .state(state)
            .priority(trigger.priority())
            .build();
        return t;
    }

    /**
     * Checks if any job is still running or waiting for it's execution.
     */
    public boolean hasTriggers() {
        if (taskInstanceRepository.countByStatus(TaskStatus.NEW) > 0) return true;
        return taskInstanceRepository.countByStatus(TaskStatus.RUNNING) > 0;
    }

    public Optional<TaskTriggerEntity> get(TaskTriggerId id) {
        return taskInstanceRepository.findById(id);
    }
}
