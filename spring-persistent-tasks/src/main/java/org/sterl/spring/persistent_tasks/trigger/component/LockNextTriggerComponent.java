package org.sterl.spring.persistent_tasks.trigger.component;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.api.TriggerId;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;
import org.sterl.spring.persistent_tasks.trigger.repository.TriggerRepository;

import lombok.RequiredArgsConstructor;

/**
 * Own transaction management, as this is the whole sense of this component
 */
@Component
@Transactional(timeout = 5)
@RequiredArgsConstructor
public class LockNextTriggerComponent {

    private final TriggerRepository triggerRepository;

    public List<TriggerEntity> loadNext(String runningOn, int count, OffsetDateTime timeDueAt) {
        final var tasks = triggerRepository.loadNextTasks(
                timeDueAt, TriggerStatus.NEW, PageRequest.of(0, count));

        tasks.forEach(t -> t.runOn(runningOn));

        return tasks;
    }

    public TriggerEntity lock(TriggerId id, String runningOn) {
        final TriggerEntity result = triggerRepository.lockById(id);
        if (result != null) {
            result.runOn(runningOn);
        }
        return result;
    }
}
