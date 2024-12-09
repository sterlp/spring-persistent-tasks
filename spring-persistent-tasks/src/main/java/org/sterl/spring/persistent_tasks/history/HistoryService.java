package org.sterl.spring.persistent_tasks.history;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.sterl.spring.persistent_tasks.api.TriggerId;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryEntity;
import org.sterl.spring.persistent_tasks.history.repository.TriggerHistoryRepository;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;
import org.sterl.spring.persistent_tasks.shared.stereotype.TransactionalService;

import lombok.RequiredArgsConstructor;

@TransactionalService
@RequiredArgsConstructor
public class HistoryService {
    private final TriggerHistoryRepository triggerHistoryRepository;
    
    public List<TriggerHistoryEntity> listHistoryForTrigger(TriggerId id, PageRequest page) {
        if (page.getSort() == Sort.unsorted()) {
            page = page.withSort(Sort.by(Direction.DESC, "id"));
        }
        return triggerHistoryRepository.findByTriggerId(id, page);
    }
    
    public Optional<TriggerHistoryEntity> findLastKnownStatus(TriggerId id) {
        final List<TriggerHistoryEntity> result = listHistoryForTrigger(id, PageRequest.of(0, 1));
        if (result.isEmpty()) return Optional.empty();
        return Optional.of(result.getFirst());
    }
    
    public void deleteAll() {
        triggerHistoryRepository.deleteAllInBatch();
    }
    
    public void deleteAllOlderThan(OffsetDateTime age) {
        triggerHistoryRepository.deleteHistoryOlderThan(age);
    }

    /**
     * Counts the <b>unique</b> triggers in the history.
     */
    public long countTriggers(TriggerStatus status) {
        return triggerHistoryRepository.countTriggers(status);
    }
}
