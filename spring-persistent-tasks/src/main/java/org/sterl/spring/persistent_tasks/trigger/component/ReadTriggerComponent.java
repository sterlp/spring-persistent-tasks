package org.sterl.spring.persistent_tasks.trigger.component;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.lang.Nullable;
import org.sterl.spring.persistent_tasks.api.TriggerId;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;
import org.sterl.spring.persistent_tasks.shared.stereotype.TransactionalCompontant;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;
import org.sterl.spring.persistent_tasks.trigger.repository.TriggerRepository;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@TransactionalCompontant
@RequiredArgsConstructor
public class ReadTriggerComponent {
    private final TriggerRepository triggerRepository;
    

    public long countByName(@NotNull String name) {
        return triggerRepository.countByTriggerName(name);
    }

    public long countByStatus(@Nullable TriggerStatus status) {
        if (status == null) return triggerRepository.count();
        return triggerRepository.countByDataStatusIn(EnumSet.of(status));
    }

    public Optional<TriggerEntity> get(TriggerId id) {
        return triggerRepository.findById(id);
    }

    /**
     * Checks if any job is still running or waiting for it's execution.
     */
    public boolean hasPendingTriggers() {
        if (triggerRepository.countByDataStatusIn(TriggerStatus.ACTIVE_STATES) > 0) {
            return true;
        }
        return false;
    }

    public List<TriggerEntity> findNotRunningOn(Set<String> names) {
        return triggerRepository.findNotRunningOn(names, TriggerStatus.RUNNING);
    }
}
