package org.sterl.spring.persistent_tasks.history;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.sterl.spring.persistent_tasks.api.TriggerId;
import org.sterl.spring.persistent_tasks.history.model.LastTriggerStateEntity;
import org.sterl.spring.persistent_tasks.history.model.TriggerStateHistoryEntity;
import org.sterl.spring.persistent_tasks.history.repository.LastTriggerStateRepository;
import org.sterl.spring.persistent_tasks.history.repository.TriggerStateDetailRepository;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;
import org.sterl.spring.persistent_tasks.shared.stereotype.TransactionalService;

import lombok.RequiredArgsConstructor;

@TransactionalService
@RequiredArgsConstructor
public class HistoryService {
    private final LastTriggerStateRepository lastTriggerStateRepository;
    private final TriggerStateDetailRepository triggerStateDetailRepository;
    
    public Optional<LastTriggerStateEntity> findStatus(long triggerId) {
        return lastTriggerStateRepository.findById(triggerId);
    }
    
    public Optional<LastTriggerStateEntity> findLastKnownStatus(TriggerId triggerId) {
        PageRequest page = PageRequest.of(0, 1).withSort(Direction.DESC, "e.data.createdTime", "id");
        var result = lastTriggerStateRepository.listKnownStatusFor(triggerId, page);
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
}
