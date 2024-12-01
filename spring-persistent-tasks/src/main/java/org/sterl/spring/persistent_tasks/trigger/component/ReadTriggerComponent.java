package org.sterl.spring.persistent_tasks.trigger.component;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.sterl.spring.persistent_tasks.api.TriggerId;
import org.sterl.spring.persistent_tasks.shared.stereotype.TransactionalCompontant;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerHistoryEntity;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerStatus;
import org.sterl.spring.persistent_tasks.trigger.repository.TriggerHistoryRepository;
import org.sterl.spring.persistent_tasks.trigger.repository.TriggerRepository;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@TransactionalCompontant
@RequiredArgsConstructor
public class ReadTriggerComponent {
    private final TriggerRepository triggerRepository;
    private final TriggerHistoryRepository triggerHistoryRepository;

    public int countByName(@NotNull String name) {
        return triggerRepository.countByTriggerName(name);
    }

    public int countByStatus(@NotNull TriggerStatus status) {
        return triggerRepository.countByDataStatus(status);
    }

    public Optional<TriggerEntity> get(TriggerId id) {
        return triggerRepository.findById(id);
    }

    public Optional<TriggerHistoryEntity> getFromHistory(TriggerId id) {
        final var page = PageRequest.of(0, 1, Sort.by(Direction.DESC, "createDate"));
        final var result = triggerHistoryRepository.findByTriggerId(id, page);
        if (result.isEmpty()) return Optional.empty();
        return Optional.of(result.get(0));
    }
    
    /**
     * Checks if any job is still running or waiting for it's execution.
     */
    public boolean hasTriggers() {
        if (triggerRepository.countByDataStatus(TriggerStatus.NEW) > 0) return true;
        return triggerRepository.countByDataStatus(TriggerStatus.RUNNING) > 0;
    }
}
