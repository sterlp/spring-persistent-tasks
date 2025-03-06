package org.sterl.spring.persistent_tasks.history;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.lang.Nullable;
import org.sterl.spring.persistent_tasks.api.TaskStatusHistoryOverview;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryDetailEntity;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryLastStateEntity;
import org.sterl.spring.persistent_tasks.history.repository.TriggerHistoryDetailRepository;
import org.sterl.spring.persistent_tasks.history.repository.TriggerHistoryLastStateRepository;
import org.sterl.spring.persistent_tasks.shared.StringHelper;
import org.sterl.spring.persistent_tasks.shared.stereotype.TransactionalService;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import lombok.RequiredArgsConstructor;

@TransactionalService
@RequiredArgsConstructor
public class HistoryService {
    private final TriggerHistoryLastStateRepository triggerHistoryLastStateRepository;
    private final TriggerHistoryDetailRepository triggerHistoryDetailRepository;
    private final TriggerService triggerService;
    
    public Optional<TriggerHistoryLastStateEntity> findStatus(long triggerId) {
        return triggerHistoryLastStateRepository.findById(triggerId);
    }
    
    public Optional<TriggerHistoryLastStateEntity> findLastKnownStatus(TriggerKey triggerKey) {
        final var page = PageRequest.of(0, 1).withSort(Direction.DESC, "data.createdTime", "id");
        final var result = triggerHistoryLastStateRepository.listKnownStatusFor(triggerKey, page);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getContent().get(0));
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

    public List<TriggerHistoryDetailEntity> findAllDetailsForInstance(long instanceId) {
        return triggerHistoryDetailRepository.findAllByInstanceId(instanceId);
    }
    
    public Page<TriggerHistoryDetailEntity> findAllDetailsForKey(TriggerKey key) {
        return findAllDetailsForKey(key, PageRequest.of(0, 100));
    }
    public Page<TriggerHistoryDetailEntity> findAllDetailsForKey(TriggerKey key, Pageable page) {
        page = applyDefaultSortIfNeeded(page);
        return triggerHistoryDetailRepository.listKnownStatusFor(key, page);
    }

    public Optional<TriggerEntity> reQueue(Long id, OffsetDateTime runAt) {
        final var lastState = triggerHistoryLastStateRepository.findById(id);
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
        return triggerHistoryLastStateRepository.countByKey(key);
    }

    /**
     * Checks for the last known state in the history.
     * 
     * @param key the {@link TriggerKey}, can be partly <code>null</code>
     * @param page page informations
     * @return the found data, looking only the last states
     */
    public Page<TriggerHistoryLastStateEntity> findTriggerState(
            @Nullable TriggerKey key, @Nullable TriggerStatus status, Pageable page) {

        page = applyDefaultSortIfNeeded(page);
        if (key == null && status == null) {
            return triggerHistoryLastStateRepository.findAll(page);
        }
        final var id = StringHelper.applySearchWildCard(key);
        final var name = key == null ? null : key.getTaskName();
        return triggerHistoryLastStateRepository.findAll(id, name, status, page);
    }

    private Pageable applyDefaultSortIfNeeded(Pageable page) {
        if (page.getSort() == Sort.unsorted()) {
            return PageRequest.of(page.getPageNumber(), page.getPageSize(), 
                    Sort.by(Direction.DESC, "data.createdTime", "id"));
        }
        return page;
    }

    public List<TaskStatusHistoryOverview> taskStatusHistory() {
        return triggerHistoryLastStateRepository.listTriggerStatus();
    }
}
