package org.sterl.spring.task.component;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.task.api.Trigger;
import org.sterl.spring.task.api.TriggerId;
import org.sterl.spring.task.api.event.TriggerFailedEvent;
import org.sterl.spring.task.model.BaseTriggerData;
import org.sterl.spring.task.model.TaskSchedulerEntity.TaskSchedulerStatus;
import org.sterl.spring.task.model.TriggerEntity;
import org.sterl.spring.task.model.TriggerStatus;
import org.sterl.spring.task.repository.TriggerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Transactional(timeout = 10)
@Slf4j
@RequiredArgsConstructor
public class EditTaskTriggerComponent {
    private final ApplicationEventPublisher publisher;
    private final TriggerHistoryComponent historyComponent;

    private final StateSerializer stateSerializer = new StateSerializer();
    private final TriggerRepository triggerRepository;
    
    public Page<TriggerEntity> listTriggers(Pageable page) {
        return triggerRepository.findAll(page);
    }

    public void failWithRetry(TriggerId id, Exception e, OffsetDateTime when) {
        triggerRepository.findById(id).ifPresent(t -> {
            t.fail(e);

            historyComponent.write(t);
            publisher.publishEvent(new TriggerFailedEvent(t));

            t.runAt(when);
            log.debug("Retrying task={} error={}", id, e.getClass());
        });
    }
    
    public void completeTaskWithStatus(TriggerId id, TriggerStatus newStatus, Exception e) {
        triggerRepository.findById(id).ifPresent(t -> {
            t.complete(newStatus, e);
            historyComponent.write(t);
            log.debug("Setting task={} to status={} {}", id, newStatus, 
                    e == null ? "" : "error=" + e.getClass().getSimpleName());
        });
    }

    public <T extends Serializable> TriggerId addTrigger(Trigger<T> tigger) {
        var result = toTriggerEntity(tigger);
        triggerRepository.save(result);
        log.debug("Added trigger={}", result);
        return result.getId();
    }

    @NonNull
    public <T extends Serializable> List<TriggerId> addTriggers(Collection<Trigger<T>> newTriggers) {
        var result = triggerRepository
            .saveAll(newTriggers.stream().map(this::toTriggerEntity).toList())
            .stream().map(TriggerEntity::getId)
            .toList();
        log.debug("Added triggers={}", result);
        return result;
    }

    private <T extends Serializable> TriggerEntity toTriggerEntity(Trigger<T> trigger) {
        byte[] state = stateSerializer.serialize(trigger.state());
        var t = new TriggerEntity(
            trigger.toTaskTriggerId(),
            BaseTriggerData.builder()
                .triggerTime(trigger.when())
                .priority(trigger.priority())
                .state(state)
                .build(),
            null
        );
        return t;
    }

    /**
     * Checks if any job is still running or waiting for it's execution.
     */
    public boolean hasTriggers() {
        if (triggerRepository.countByDataStatus(TriggerStatus.NEW) > 0) return true;
        return triggerRepository.countByDataStatus(TriggerStatus.RUNNING) > 0;
    }

    public List<TriggerEntity> findTasksInTimeout(Duration timeout) {
        final var startTime = OffsetDateTime.now().minus(timeout);
        return triggerRepository.findByTimeout(startTime, 
                TriggerStatus.RUNNING, TaskSchedulerStatus.ONLINE);
    }

    public void deleteAll() {
        log.info("All triggers are removed!");
        this.triggerRepository.deleteAllInBatch();
    }
}
