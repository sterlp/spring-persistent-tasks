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
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.shared.stereotype.TransactionalCompontant;
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

    public Optional<TriggerEntity> get(TriggerKey id) {
        return triggerRepository.findByKey(id);
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

    public Page<TriggerEntity> listTriggers(@Nullable TriggerKey key,
            @Nullable TriggerStatus status, Pageable page) {
        if (key == null && status == null) return triggerRepository.findAll(page);
        final var id = key == null ? null : key.getId();
        final var name = key == null ? null : key.getTaskName();
        return triggerRepository.findAll(id, name, status, page);
    }

    public Page<TriggerEntity> listTriggers(TaskId<? extends Serializable> task, Pageable page) {
        if (task == null) return triggerRepository.findAll(page);
        return triggerRepository.findAll(task.name(), page);
    }
}
