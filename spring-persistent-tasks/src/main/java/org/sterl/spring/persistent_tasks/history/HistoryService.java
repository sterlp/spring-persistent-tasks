package org.sterl.spring.persistent_tasks.history;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.history.model.LastTriggerStateEntity;
import org.sterl.spring.persistent_tasks.history.model.TriggerStateHistoryEntity;
import org.sterl.spring.persistent_tasks.history.repository.LastTriggerStateRepository;
import org.sterl.spring.persistent_tasks.history.repository.TriggerStateDetailRepository;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;
import org.sterl.spring.persistent_tasks.shared.stereotype.TransactionalService;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import lombok.RequiredArgsConstructor;

@TransactionalService
@RequiredArgsConstructor
public class HistoryService {
    private final LastTriggerStateRepository lastTriggerStateRepository;
    private final TriggerStateDetailRepository triggerStateDetailRepository;
    private final TriggerService triggerService;
    
    public Optional<LastTriggerStateEntity> findStatus(long triggerId) {
        return lastTriggerStateRepository.findById(triggerId);
    }
    
    public Optional<LastTriggerStateEntity> findLastKnownStatus(TriggerKey triggerKey) {
        PageRequest page = PageRequest.of(0, 1).withSort(Direction.DESC, "e.data.createdTime", "id");
        var result = lastTriggerStateRepository.listKnownStatusFor(triggerKey, page);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public void deleteAll() {
        triggerStateDetailRepository.deleteAllInBatch();
        lastTriggerStateRepository.deleteAllInBatch();
    }
    
    public void deleteAllOlderThan(OffsetDateTime age) {
        triggerStateDetailRepository.deleteOlderThan(age);
        lastTriggerStateRepository.deleteOlderThan(age);
    }

    /**
     * Counts the <b>unique</b> triggers in the history.
     */
    public long countTriggers(TriggerStatus status) {
        return lastTriggerStateRepository.countByStatus(status);
    }

    public List<TriggerStateHistoryEntity> findAllForInstance(long instanceId) {
        return triggerStateDetailRepository.findAllByInstanceId(instanceId);
    }

    public Optional<TriggerEntity> reQueue(Long id, OffsetDateTime runAt) {
        final var lastState = lastTriggerStateRepository.findById(id);
        if (lastState.isEmpty()) return Optional.empty();
        
        final var data = lastState.get().getData();
        final var trigger = lastState.get().newTaskId().newTrigger()
            .state(data.getState())
            .runAt(runAt)
            .priority(data.getPriority())
            .id(data.getKey().getId())
            .build();
        
        return Optional.of(triggerService.queue(trigger));
    }

    public long countTriggers(TriggerKey key) {
        return lastTriggerStateRepository.countByKey(key);
    }

    public Page<LastTriggerStateEntity> findTriggerState(TaskId<?> taskId, Pageable page) {
        return lastTriggerStateRepository.findAll(page);
    }
}
