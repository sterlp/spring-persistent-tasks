package org.sterl.spring.persistent_tasks.trigger.component;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TriggerGroup;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerSearch;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.shared.model.HasTrigger;
import org.sterl.spring.persistent_tasks.shared.stereotype.TransactionalCompontant;
import org.sterl.spring.persistent_tasks.trigger.model.QRunningTriggerEntity;
import org.sterl.spring.persistent_tasks.trigger.model.RunningTriggerEntity;
import org.sterl.spring.persistent_tasks.trigger.repository.RunningTriggerRepository;

import com.querydsl.jpa.impl.JPAQuery;

import jakarta.persistence.EntityManager;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@TransactionalCompontant
@RequiredArgsConstructor
@Slf4j
public class ReadTriggerComponent {
    private final EntityManager em;
    private final RunningTriggerRepository triggerRepository;

    public long countByTaskName(@NotNull String name) {
        return triggerRepository.countByTaskName(name);
    }

    public long countByStatus(@Nullable TriggerStatus status) {
        if (status == null) return triggerRepository.count();
        return triggerRepository.countByStatus(status);
    }

    public Optional<RunningTriggerEntity> get(TriggerKey key) {
        if (key == null || key.getId() == null) return Optional.empty();
        return triggerRepository.findByKey(key);
    }

    /**
     * Checks if any job is still running or waiting for it's execution.
     */
    public boolean hasPendingTriggers() {
        if (triggerRepository.countByStatus(TriggerStatus.ACTIVE_STATES) > 0) {
            return true;
        }
        return false;
    }
    
    public List<RunningTriggerEntity> findTriggersLastPingAfter(OffsetDateTime dateTime) {
        return triggerRepository.findTriggersLastPingAfter(dateTime);
    }

    public Page<RunningTriggerEntity> searchTriggers(@Nullable TriggerSearch search, Pageable page) {
        page = TriggerSearch.applyDefaultSortIfNeeded(page);
        if (search != null && search.hasValue()) {
            var p = triggerRepository.buildSearch(QRunningTriggerEntity.runningTriggerEntity.data, search);
            var result = triggerRepository.findAll(p, page);
            log.debug("searchTriggers={} returned size={}", search, result.getContent().size());
            return result;
        } else {
            log.debug("Empty search={}, selecting all triggers for page={}", search, page);
            return triggerRepository.findAll(page);
        }
        
    }
    
    public Page<TriggerGroup> searchGroupedTriggers(@Nullable TriggerSearch search, Pageable page) {
        return triggerRepository.findByGroup(
                new JPAQuery<HasTrigger>(em).from(QRunningTriggerEntity.runningTriggerEntity), 
                QRunningTriggerEntity.runningTriggerEntity.data, 
                QRunningTriggerEntity.runningTriggerEntity.data.correlationId, 
                search, 
                page);
    }

    public Page<RunningTriggerEntity> listTriggers(TaskId<? extends Serializable> task, Pageable page) {
        if (task == null) return triggerRepository.findAll(page);
        return triggerRepository.findAll(task.name(), page);
    }
    
    public List<RunningTriggerEntity> findTriggersTimeoutOut(int max) {
        return triggerRepository.findByStatusAndRunAtAfter(
                TriggerStatus.AWAITING_SIGNAL,
                OffsetDateTime.now(),
                Pageable.ofSize(max));
    }
}
