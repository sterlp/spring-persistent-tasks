package org.sterl.spring.task.component;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.task.api.TriggerId;
import org.sterl.spring.task.model.TriggerEntity;
import org.sterl.spring.task.model.TriggerHistoryEntity;
import org.sterl.spring.task.model.TriggerStatus;
import org.sterl.spring.task.repository.TriggerHistoryRepository;
import org.sterl.spring.task.repository.TriggerRepository;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Component
@Transactional
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
}
