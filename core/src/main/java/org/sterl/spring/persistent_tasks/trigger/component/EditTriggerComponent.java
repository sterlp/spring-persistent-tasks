package org.sterl.spring.persistent_tasks.trigger.component;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.api.AddTriggerRequest;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.api.task.RunningTriggerContextHolder;
import org.sterl.spring.persistent_tasks.shared.model.TriggerData;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerAddedEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerCanceledEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerFailedEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerRunningEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerSuccessEvent;
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

    public Optional<TriggerEntity> completeTaskWithSuccess(TriggerKey key, Serializable state) {
        final Optional<TriggerEntity> result = triggerRepository.findByKey(key);

        result.ifPresent(t -> {
            t.complete(null);
            log.debug("{} set to status={}", key, t.getData().getStatus());
            publisher.publishEvent(new TriggerSuccessEvent(
                    t.getId(), t.copyData(), state));
            triggerRepository.delete(t);
        });
        return result;
    }

    /**
     * Sets error based on the fact if an exception is given or not.
     */
    public Optional<TriggerEntity> failTrigger(
            TriggerKey key, 
            Serializable state, 
            Exception e,
            OffsetDateTime retryAt) {
        final Optional<TriggerEntity> result = triggerRepository.findByKey(key);


        result.ifPresent(t -> {
            t.complete(e);
            publisher.publishEvent(new TriggerFailedEvent(t.getId(), t.copyData(), state, e, retryAt));

            if (retryAt == null) {
                triggerRepository.delete(t);
            } else {
                t.runAt(retryAt);
            }
        });

        if (result.isEmpty()) {
            log.error("Trigger with key={} not found and may be at a wrong state!",
                    key, e);
        }

        return result;
    }

    public Optional<TriggerEntity> cancelTask(TriggerKey id, Exception e) {
        return triggerRepository //
                .findByKey(id) //
                .map(t -> cancelTask(t, e));
    }

    private TriggerEntity cancelTask(TriggerEntity t, Exception e) {
        t.cancel(e);
        publisher.publishEvent(new TriggerCanceledEvent(
                t.getId(), t.copyData(),
                stateSerializer.deserializeOrNull(t.getData().getState())));
        triggerRepository.delete(t);
        return t;
    }

    public <T extends Serializable> TriggerEntity addTrigger(AddTriggerRequest<T> tigger) {
        var result = toTriggerEntity(tigger);
        final Optional<TriggerEntity> existing = triggerRepository.findByKey(result.getKey());
        if (existing.isPresent()) {
            if (existing.get().isRunning()) 
                throw new IllegalStateException("Cannot update a running trigger " + result.getKey());
            
            existing.get().setData(result.getData());
            result = existing.get();
            log.debug("Updated trigger={}", result);
        } else {
            result = triggerRepository.save(result);
            log.debug("Added trigger={}", result);
        }
        publisher.publishEvent(new TriggerAddedEvent(
                result.getId(), result.copyData(), tigger.state()));
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

        var correlationId = RunningTriggerContextHolder.buildOrGetCorrelationId(trigger.correlationId());
        final var data = TriggerData.builder()
                .key(trigger.key())
                .runAt(trigger.runtAt())
                .priority(trigger.priority())
                .state(state)
                .correlationId(correlationId)
                .tag(trigger.tag());

        final var t = TriggerEntity.builder()
            .data(data.build())
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

    @Transactional(propagation = Propagation.SUPPORTS)
    public void triggerIsNowRunning(TriggerEntity trigger, Serializable state) {
        if (!trigger.isRunning()) trigger.runOn(trigger.getRunningOn());
        publisher.publishEvent(new TriggerRunningEvent(
                trigger.getId(), trigger.copyData(), state, trigger.getRunningOn()));
    }
}
