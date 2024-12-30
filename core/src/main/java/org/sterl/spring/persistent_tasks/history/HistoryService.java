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
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryDetailEntity;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryLastStateEntity;
import org.sterl.spring.persistent_tasks.history.repository.TriggerHistoryDetailRepository;
import org.sterl.spring.persistent_tasks.history.repository.TriggerHistoryLastStateRepository;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;
import org.sterl.spring.persistent_tasks.shared.stereotype.TransactionalService;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import lombok.RequiredArgsConstructor;

@TransactionalService
@RequiredArgsConstructor
public class HistoryService {
    private final TriggerHistoryDetailRepository triggerHistoryDetailRepository;
    private final TriggerHistoryLastStateRepository triggerHistoryLastStateRepository;
    private final TriggerService triggerService;
    
    public Optional<TriggerHistoryLastStateEntity> findStatus(long triggerId) {
        return triggerHistoryDetailRepository.findById(triggerId);
    }
    
    public Optional<TriggerHistoryLastStateEntity> findLastKnownStatus(TriggerKey triggerKey) {
        PageRequest page = PageRequest.of(0, 1).withSort(Direction.DESC, "e.data.createdTime", "id");
        var result = triggerHistoryDetailRepository.listKnownStatusFor(triggerKey, page);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public void deleteAll() {
        triggerHistoryLastStateRepository.deleteAllInBatch();
        triggerHistoryDetailRepository.deleteAllInBatch();
    }
    
    public void deleteAllOlderThan(OffsetDateTime age) {
        triggerHistoryLastStateRepository.deleteOlderThan(age);
        triggerHistoryDetailRepository.deleteOlderThan(age);
    }

    /**
     * Counts the <b>unique</b> triggers in the history.
     */
    public long countTriggers(TriggerStatus status) {
        return triggerHistoryDetailRepository.countByStatus(status);
    }

    public List<TriggerHistoryDetailEntity> findAllForInstance(long instanceId) {
        return triggerHistoryLastStateRepository.findAllByInstanceId(instanceId);
    }

    public Optional<TriggerEntity> reQueue(Long id, OffsetDateTime runAt) {
        final var lastState = triggerHistoryDetailRepository.findById(id);
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
        return triggerHistoryDetailRepository.countByKey(key);
    }

    public Page<TriggerHistoryLastStateEntity> findTriggerState(TaskId<?> taskId, Pageable page) {
        return triggerHistoryDetailRepository.findAll(page);
    }
}
