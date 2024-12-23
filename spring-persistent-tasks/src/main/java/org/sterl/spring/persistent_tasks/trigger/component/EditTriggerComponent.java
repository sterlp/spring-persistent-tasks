package org.sterl.spring.persistent_tasks.trigger.component;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.api.AddTriggerRequest;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.shared.model.TriggerData;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerCanceledEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerCompleteEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerFailedEvent;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;
import org.sterl.spring.persistent_tasks.trigger.repository.TriggerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Transactional(timeout = 30)
@RequiredArgsConstructor
public class EditTriggerComponent {
    private final ApplicationEventPublisher publisher;

    private final StateSerializer stateSerializer = new StateSerializer();
    private final TriggerRepository triggerRepository;

    public Page<TriggerEntity> listTriggers(TaskId<?> task, Pageable page) {
        if (task == null) return triggerRepository.findAll(page);
        return triggerRepository.findAll(task.name(), page);
    }

    public Optional<TriggerEntity> completeTaskWithSuccess(TriggerKey key) {
        return this.completeTaskWithStatus(key, null);
    }

    /**
     * Sets success or error based on the fact if an exception is given or not.
     */
    public Optional<TriggerEntity> completeTaskWithStatus(TriggerKey key, Exception e) {
        final Optional<TriggerEntity> result = triggerRepository.findByKey(key);

        result.ifPresent(t -> {
            t.complete(e);

            if (t.getData().getStatus() != TriggerStatus.FAILED) {
                publisher.publishEvent(new TriggerCompleteEvent(t));
                log.debug("Setting {} to status={} {}", key, t.getData().getStatus(),
                        e == null ? "" : "error=" + e.getClass().getSimpleName());
            } else {
                publisher.publishEvent(new TriggerFailedEvent(t));
                log.info("Setting {} to status={} {}", key, t.getData().getStatus(),
                        e == null ? "" : "error=" + e.getClass().getSimpleName());
            }

        });

        return result;
    }

    public Optional<TriggerEntity> retryTrigger(TriggerKey id, OffsetDateTime retryAt) {
        return triggerRepository //
                .findByKey(id) //
                .map(t -> t.runAt(retryAt));
    }

    public Optional<TriggerEntity> cancelTask(TriggerKey id) {
        return triggerRepository //
                .findByKey(id) //
                .map(t -> {
                    t.cancel();
                    publisher.publishEvent(new TriggerCanceledEvent(t));
                    return t;
                });
    }

    public <T extends Serializable> TriggerEntity addTrigger(AddTriggerRequest<T> tigger) {
        var result = toTriggerEntity(tigger);
        final Optional<TriggerEntity> existing = triggerRepository.findByKey(result.getKey());
        if (existing.isPresent()) {
            if (existing.get().isRunning()) 
                throw new IllegalStateException("Cannot update running trigger " + result.getKey());
            
            existing.get().setData(result.getData());
            result = existing.get();
            log.debug("Updated trigger={}", result);
        } else {
            result = triggerRepository.save(result);
            log.debug("Added trigger={}", result);
        }
        return result;
    }

    @NonNull
    public <T extends Serializable> List<TriggerEntity> addTriggers(Collection<AddTriggerRequest<T>> newTriggers) {
        return newTriggers.stream()
                .map(this::addTrigger)
                .toList();
    }

    private <T extends Serializable> TriggerEntity toTriggerEntity(AddTriggerRequest<T> trigger) {
        byte[] state = stateSerializer.serialize(trigger.state());
        final var t = TriggerEntity.builder()
            .data(TriggerData.builder()
                    .key(trigger.key())
                    .runAt(trigger.runtAt())
                    .priority(trigger.priority())
                    .state(state)
                    .build())
            .build();
        return t;
    }

    public void deleteAll() {
        log.info("All triggers are removed!");
        this.triggerRepository.deleteAllInBatch();
    }

    public void deleteTrigger(TriggerEntity trigger) {
        this.triggerRepository.delete(trigger);
    }

    public int markTriggersAsRunning(Collection<TriggerKey> keys, String runOn) {
        return triggerRepository.markTriggersAsRunning(keys, runOn, 
                OffsetDateTime.now(), TriggerStatus.RUNNING);
    }
}
