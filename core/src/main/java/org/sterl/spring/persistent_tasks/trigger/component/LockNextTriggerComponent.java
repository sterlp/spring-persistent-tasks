package org.sterl.spring.persistent_tasks.trigger.component;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.trigger.model.RunningTriggerEntity;
import org.sterl.spring.persistent_tasks.trigger.repository.RunningTriggerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Own transaction management, as this is the whole sense of this component
 */
@Slf4j
@Component
@Transactional(timeout = 5)
@RequiredArgsConstructor
public class LockNextTriggerComponent {

    private final RunningTriggerRepository triggerRepository;

    public List<RunningTriggerEntity> loadNext(String runningOn, int count, OffsetDateTime timeDueAt) {
        final var tasks = triggerRepository.loadNextTasks(
                timeDueAt, TriggerStatus.WAITING, PageRequest.of(0, count));

        tasks.forEach(t -> t.runOn(runningOn));
        log.debug("loadNext triggers for={} found={} triggers with dueAt={}",
                runningOn, tasks.size(), timeDueAt);
        return tasks;
    }

    public RunningTriggerEntity lock(TriggerKey id, String runningOn) {
        final RunningTriggerEntity result = triggerRepository.lockByKey(id);
        if (result != null) {
            result.runOn(runningOn);
        }
        return result;
    }
}
