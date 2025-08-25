package org.sterl.spring.persistent_tasks.history;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.lang.Nullable;
import org.sterl.spring.persistent_tasks.api.TaskStatusHistoryOverview;
import org.sterl.spring.persistent_tasks.api.TriggerGroup;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerSearch;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.api.event.TriggerTaskCommand;
import org.sterl.spring.persistent_tasks.history.model.CompletedTriggerEntity;
import org.sterl.spring.persistent_tasks.history.model.HistoryTriggerEntity;
import org.sterl.spring.persistent_tasks.history.model.QCompletedTriggerEntity;
import org.sterl.spring.persistent_tasks.history.repository.CompletedTriggerRepository;
import org.sterl.spring.persistent_tasks.history.repository.HistoryTriggerRepository;
import org.sterl.spring.persistent_tasks.shared.QueryHelper;
import org.sterl.spring.persistent_tasks.shared.model.HasTrigger;
import org.sterl.spring.persistent_tasks.shared.stereotype.TransactionalService;

import com.querydsl.jpa.impl.JPAQuery;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@TransactionalService
@RequiredArgsConstructor
public class HistoryService {
    private final EntityManager em;
    private final CompletedTriggerRepository completedTriggerRepository;
    private final HistoryTriggerRepository historyTriggerRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    
    public Optional<CompletedTriggerEntity> findStatus(long triggerId) {
        return completedTriggerRepository.findById(triggerId);
    }
    
    public Optional<CompletedTriggerEntity> findLastKnownStatus(TriggerKey triggerKey) {
        final var page = PageRequest.of(0, 1).withSort(Direction.DESC, "data.createdTime", "id");
        final var result = completedTriggerRepository.listKnownStatusFor(triggerKey, page);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getContent().get(0));
    }

    public void deleteAll() {
        completedTriggerRepository.deleteAllInBatch();
        historyTriggerRepository.deleteAllInBatch();
    }
    
    public long deleteAllOlderThan(OffsetDateTime age) {
        var result = historyTriggerRepository.deleteOlderThan(age);
        result += completedTriggerRepository.deleteOlderThan(age);
        return result;
    }

    /**
     * Counts the <b>unique</b> triggers in the history.
     */
    public long countTriggers(TriggerStatus status) {
        return historyTriggerRepository.countByStatus(status);
    }

    public Page<HistoryTriggerEntity> findAllDetailsForInstance(long instanceId, Pageable page) {
        page = QueryHelper.applySortIfEmpty(page, Sort.by(Direction.DESC, "id"));
        return historyTriggerRepository.findAllByInstanceId(instanceId, page);
    }

    public Optional<TriggerKey> reQueue(Long id, OffsetDateTime runAt) {
        final var lastState = completedTriggerRepository.findById(id);
        if (lastState.isEmpty()) return Optional.empty();

        final var data = lastState.get().getData();
        final var trigger = lastState.get().newTaskId().newTrigger()
            .state(data.getState())
            .runAt(runAt)
            .priority(data.getPriority())
            .id(data.getKey().getId())
            .build();

        applicationEventPublisher.publishEvent(TriggerTaskCommand.of(trigger));
        return Optional.of(trigger.key());
    }

    public long countTriggers(TriggerKey key) {
        return completedTriggerRepository.countByKey(key);
    }

    /**
     * Checks for the last known state in the history.
     * 
     * @param search the trigger search, can partly <code>null</code>
     * @param page page informations
     * @return the found data, looking only the last states
     */
    public Page<CompletedTriggerEntity> searchTriggers(
            @Nullable TriggerSearch search, Pageable page) {

        page = applyDefaultSortIfNeeded(page);
        Page<CompletedTriggerEntity> result;

        if (search != null && search.hasValue()) {
            var p = completedTriggerRepository.buildSearch(
                    QCompletedTriggerEntity.completedTriggerEntity.data, 
                    search);
            result = completedTriggerRepository.findAll(p, page);
        } else {
            result = completedTriggerRepository.findAll(page);
        }
        return result;
    }
    
    public Page<TriggerGroup> searchGroupedTriggers(TriggerSearch search, Pageable page) {
        return completedTriggerRepository.findByGroup(
                new JPAQuery<HasTrigger>(em).from(QCompletedTriggerEntity.completedTriggerEntity), 
                QCompletedTriggerEntity.completedTriggerEntity.data, 
                QCompletedTriggerEntity.completedTriggerEntity.data.correlationId, 
                search, 
                page);
    }

    private Pageable applyDefaultSortIfNeeded(Pageable page) {
        if (page.getSort() == Sort.unsorted()) {
            return PageRequest.of(page.getPageNumber(), page.getPageSize(), 
                    Sort.by(Direction.DESC, "data.createdTime", "id"));
        }
        return page;
    }

    public List<TaskStatusHistoryOverview> taskStatusHistory() {
        return completedTriggerRepository.listTriggerStatus();
    }
}
