package org.sterl.spring.persistent_tasks.trigger.component;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerSearch;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.shared.stereotype.TransactionalCompontant;
import org.sterl.spring.persistent_tasks.trigger.model.QTriggerEntity;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;
import org.sterl.spring.persistent_tasks.trigger.repository.TriggerRepository;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@TransactionalCompontant
@RequiredArgsConstructor
public class ReadTriggerComponent {
    private final TriggerRepository triggerRepository;

    public long countByTaskName(@NotNull String name) {
        return triggerRepository.countByTaskName(name);
    }

    public long countByStatus(@Nullable TriggerStatus status) {
        if (status == null) return triggerRepository.count();
        return triggerRepository.countByStatus(status);
    }

    public Optional<TriggerEntity> get(TriggerKey key) {
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
    
    public List<TriggerEntity> findTriggersLastPingAfter(OffsetDateTime dateTime) {
        return triggerRepository.findTriggersLastPingAfter(dateTime);
    }

    public Page<TriggerEntity> searchTriggers(@Nullable TriggerSearch search, Pageable page) {
        page = TriggerSearch.applyDefaultSortIfNeeded(page);
        if (search != null && search.hasValue()) {
            var p = triggerRepository.buildSearch(QTriggerEntity.triggerEntity.data, search);
            return triggerRepository.findAll(p, page);
        } else {
            return triggerRepository.findAll(page);
        }
        
    }

    public Page<TriggerEntity> listTriggers(TaskId<? extends Serializable> task, Pageable page) {
        if (task == null) return triggerRepository.findAll(page);
        return triggerRepository.findAll(task.name(), page);
    }
}
