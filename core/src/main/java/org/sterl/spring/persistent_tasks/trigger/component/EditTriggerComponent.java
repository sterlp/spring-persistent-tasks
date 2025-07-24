package org.sterl.spring.persistent_tasks.trigger.component;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerRequest;
import org.sterl.spring.persistent_tasks.api.TriggerSearch;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerAddedEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerCanceledEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerExpiredEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerFailedEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerResumedEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerRunningEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerSuccessEvent;
import org.sterl.spring.persistent_tasks.trigger.model.RunningTriggerEntity;
import org.sterl.spring.persistent_tasks.trigger.repository.RunningTriggerRepository;

import com.github.f4b6a3.uuid.UuidCreator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Transactional(timeout = 30)
@RequiredArgsConstructor
public class EditTriggerComponent {
    private final ApplicationEventPublisher publisher;

    private final StateSerializer stateSerializer = new StateSerializer();
    private final ToTriggerData toTriggerData = new ToTriggerData(stateSerializer);
    private final ReadTriggerComponent readTrigger;
    private final RunningTriggerRepository triggerRepository;

    public Optional<RunningTriggerEntity> completeTaskWithSuccess(TriggerKey key, Serializable state) {
        final Optional<RunningTriggerEntity> result = readTrigger.get(key);

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
    public Optional<RunningTriggerEntity> failTrigger(
            TriggerKey key, 
            Serializable state, 
            Exception e,
            OffsetDateTime retryAt) {
        
        final var result = readTrigger.get(key);

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

    public Optional<RunningTriggerEntity> cancelTask(TriggerKey id, Exception e) {
        return readTrigger.get(id)
                          .map(t -> cancelTrigger(t, e));
    }

    private RunningTriggerEntity cancelTrigger(RunningTriggerEntity t, Exception e) {
        t.cancel(e);

        publisher.publishEvent(new TriggerCanceledEvent(
                t.getId(), 
                t.copyData(),
                stateSerializer.deserializeOrNull(t.getData().getState())));

        triggerRepository.delete(t);
        return t;
    }

    public <T extends Serializable> RunningTriggerEntity addTrigger(TriggerRequest<T> tigger) {
        var result = toTriggerEntity(tigger);
        final var existing = readTrigger.get(tigger.key());

        if (existing.isPresent()) {
            if (existing.get().isRunning()) 
                throw new IllegalStateException("Cannot update a running trigger " + result.getKey());
            
            existing.get().setData(result.getData());
            result = existing.get();
            log.debug("Updated trigger={}", result);
        } else {
            if (result.getKey().getId() == null) {
                result.getKey().setId(UuidCreator.getTimeOrderedEpochFast().toString());
            }
            result = triggerRepository.save(result);
            log.debug("Added trigger={}", result);
        }

        publisher.publishEvent(new TriggerAddedEvent(
                result.getId(), result.copyData(), tigger.state()));

        return result;
    }
    
    public Page<RunningTriggerEntity> resume(TriggerRequest<?> trigger) {
        var search = TriggerSearch.forTriggerRequest(trigger);
        search.setStatus(TriggerStatus.AWAITING_SIGNAL);
        
        var foundTriggers = readTrigger.searchTriggers(search, Pageable.ofSize(100));
        
        log.debug("Resuming {} triggers for given data {}", foundTriggers.getSize(), trigger);
        foundTriggers.forEach(t -> {
            log.debug("Resuming trigger={} with search={}", t, search);
            var newData = toTriggerEntity(trigger);
            newData.getData().setKey(t.getKey());
            newData.getData().setCorrelationId(t.getData().getCorrelationId());

            t.setData(newData.getData());
            t.runAt(trigger.runtAt());

            publisher.publishEvent(new TriggerResumedEvent(t.getId(), t.copyData(), trigger.state()));
        });
        return foundTriggers;
    }
    
    /**
     * Resumes the first found trigger with the given
     * @param <T> state type
     * @param search search to run
     * @param stateModifier updates the state and should return the run 
     * @return the updated trigger
     */
    public <T extends Serializable> Optional<RunningTriggerEntity> resumeOne(
            TriggerSearch search, Function<T, T> stateModifier) {
        
        search.setStatus(TriggerStatus.AWAITING_SIGNAL);
        var foundTriggers = readTrigger.searchTriggers(search, Pageable.ofSize(1));

        foundTriggers.forEach(t -> {
            log.debug("Resuming trigger={} with search={}", t, search);
            var newStart = stateModifier.apply((T)stateSerializer.deserialize(t.getData().getState()));
            t.getData().setState(stateSerializer.serialize(newStart));
            t.runAt(OffsetDateTime.now());
            publisher.publishEvent(new TriggerResumedEvent(t.getId(), t.copyData(), newStart));
        });

        return foundTriggers.isEmpty() ? Optional.empty() : Optional.of(foundTriggers.getContent().get(0));
    }
    
    public RunningTriggerEntity expireTrigger(RunningTriggerEntity t) {
        t.getData().setStatus(TriggerStatus.EXPIRED_SIGNAL);
        t.getData().setStart(null);
        t.getData().setEnd(null);
        t.getData().updateRunningDuration();
        
        publisher.publishEvent(new TriggerExpiredEvent(
                t.getId(), t.copyData(), 
                stateSerializer.deserializeOrNull(t.getData().getState())));
        return t;
    }

    @NonNull
    public <T extends Serializable> List<RunningTriggerEntity> addTriggers(Collection<TriggerRequest<T>> newTriggers) {
        return newTriggers.stream()
                .map(this::addTrigger)
                .toList();
    }

    private <T extends Serializable> RunningTriggerEntity toTriggerEntity(TriggerRequest<T> trigger) {
        return RunningTriggerEntity.builder()
            .data(toTriggerData.convert(trigger))
            .build();
    }

    public void deleteAll() {
        log.info("All triggers are removed!");
        this.triggerRepository.deleteAllInBatch();
    }

    public void deleteTrigger(RunningTriggerEntity trigger) {
        this.triggerRepository.delete(trigger);
    }

    public int markTriggersAsRunning(Collection<TriggerKey> keys, String runOn) {
        return triggerRepository.markTriggersAsRunning(keys, runOn, 
                OffsetDateTime.now(), TriggerStatus.RUNNING);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void triggerIsNowRunning(RunningTriggerEntity trigger, Serializable state) {
        if (!trigger.isRunning()) trigger.runOn(trigger.getRunningOn());
        publisher.publishEvent(new TriggerRunningEvent(
                trigger.getId(), trigger.copyData(), state, trigger.getRunningOn()));
    }
}
