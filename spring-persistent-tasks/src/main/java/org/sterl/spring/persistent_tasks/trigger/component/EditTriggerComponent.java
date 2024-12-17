package org.sterl.spring.persistent_tasks.trigger.component;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.api.AddTriggerRequest;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TriggerId;
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

    public Optional<TriggerEntity> completeTaskWithSuccess(TriggerId id) {
        return this.completeTaskWithStatus(id, null);
    }

    /**
     * Sets success or error based on the fact if an exception is given or not.
     */
    public Optional<TriggerEntity> completeTaskWithStatus(TriggerId id, Exception e) {
        final Optional<TriggerEntity> result = triggerRepository.findById(id);

        result.ifPresent(t -> {
            t.complete(e);

            if (t.getData().getStatus() != TriggerStatus.FAILED) {
                publisher.publishEvent(new TriggerCompleteEvent(t));
                log.debug("Setting task={} to status={} {}", id, t.getData().getStatus(),
                        e == null ? "" : "error=" + e.getClass().getSimpleName());
            } else {
                publisher.publishEvent(new TriggerFailedEvent(t));
                log.info("Setting task={} to status={} {}", id, t.getData().getStatus(),
                        e == null ? "" : "error=" + e.getClass().getSimpleName());
            }

        });

        return result;
    }

    public Optional<TriggerEntity> retryTrigger(TriggerId id, OffsetDateTime retryAt) {
        return triggerRepository //
                .findById(id) //
                .map(t -> t.runAt(retryAt));
    }

    public Optional<TriggerEntity> cancelTask(TriggerId id) {
        return triggerRepository //
                .findById(id) //
                .map(t -> {
                    t.cancel();
                    publisher.publishEvent(new TriggerCanceledEvent(t));
                    return t;
                });
    }

    public <T extends Serializable> TriggerEntity addTrigger(AddTriggerRequest<T> tigger) {
        var result = toTriggerEntity(tigger);
        result = triggerRepository.save(result);
        log.debug("Added trigger={}", result);
        return result;
    }

    @NonNull
    public <T extends Serializable> List<TriggerId> addTriggers(Collection<AddTriggerRequest<T>> newTriggers) {
        var result = triggerRepository
            .saveAll(newTriggers.stream().map(this::toTriggerEntity).toList())
            .stream().map(TriggerEntity::getId)
            .toList();
        log.debug("Added triggers={}", result);
        return result;
    }

    private <T extends Serializable> TriggerEntity toTriggerEntity(AddTriggerRequest<T> trigger) {
        byte[] state = stateSerializer.serialize(trigger.state());
        var t = new TriggerEntity(
            trigger.toTaskTriggerId(),
            TriggerData.builder()
                .runAt(trigger.runtAt())
                .priority(trigger.priority())
                .state(state)
                .build(),
            null
        );
        return t;
    }

    public void deleteAll() {
        log.info("All triggers are removed!");
        this.triggerRepository.deleteAllInBatch();
    }

    public void deleteTrigger(TriggerEntity trigger) {
        this.triggerRepository.delete(trigger);
    }
}
