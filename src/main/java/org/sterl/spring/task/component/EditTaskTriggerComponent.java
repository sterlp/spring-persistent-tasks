package org.sterl.spring.task.component;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.task.api.TaskTrigger;
import org.sterl.spring.task.model.TriggerStatus;
import org.sterl.spring.task.model.TriggerEntity;
import org.sterl.spring.task.model.TriggerId;
import org.sterl.spring.task.repository.TriggerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Transactional
@Slf4j
@RequiredArgsConstructor
public class EditTaskTriggerComponent {
    private final StateSerializer stateSerializer = new StateSerializer();
    private final TriggerRepository triggerRepository;

    public void completeWithRetry(TriggerId id, Exception e, OffsetDateTime when) {
        triggerRepository.findById(id).ifPresent(t -> {
            t.complete(TriggerStatus.NEW, e);
            t.setStart(when);
            log.debug("Retrying task={} error={}", id, e.getClass());
        });
    }
    
    public void completeTaskWithStatus(TriggerId id, TriggerStatus newStatus, Exception e) {
        triggerRepository.findById(id).ifPresent(t -> {
            t.complete(newStatus, e);
            log.debug("Setting task={} to status={} {}", id, newStatus, 
                    e == null ? "" : "error=" + e.getClass().getSimpleName());
        });
    }

    public <T extends Serializable> TriggerId addTrigger(TaskTrigger<T> tigger) {
        var t = toTriggerEntity(tigger);
        triggerRepository.save(t);
        return t.getId();
    }

    public <T extends Serializable> List<TriggerId> addTriggers(Collection<TaskTrigger<T>> newTriggers) {
        return triggerRepository
            .saveAll(newTriggers.stream().map(this::toTriggerEntity).toList())
            .stream().map(TriggerEntity::getId)
            .toList();
    }
    public void triggerAll(Collection<TaskTrigger<?>> newTriggers) {
        triggerRepository.saveAll(newTriggers.stream().map(this::toTriggerEntity).toList());
    }

    private <T extends Serializable> TriggerEntity toTriggerEntity(TaskTrigger<T> trigger) {
        byte[] state = stateSerializer.serialize(trigger.state());
        var t = TriggerEntity.builder()
            .id(trigger.toTaskTriggerId())
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
        if (triggerRepository.countByStatus(TriggerStatus.NEW) > 0) return true;
        return triggerRepository.countByStatus(TriggerStatus.RUNNING) > 0;
    }

    public Optional<TriggerEntity> get(TriggerId id) {
        return triggerRepository.findById(id);
    }

    public List<TriggerEntity> findTasksInTimeout(Duration timeout) {
        final var startTime = OffsetDateTime.now().minus(timeout);
        return triggerRepository.findByTimeout(startTime, TriggerStatus.RUNNING);
    }
}
