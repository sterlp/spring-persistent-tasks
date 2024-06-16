package org.sterl.spring.task.component;

import java.time.OffsetDateTime;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.task.model.TaskSchedulerEntity;
import org.sterl.spring.task.model.TriggerEntity;
import org.sterl.spring.task.model.TriggerStatus;
import org.sterl.spring.task.repository.TriggerRepository;

import lombok.RequiredArgsConstructor;

@Component
@Transactional
@RequiredArgsConstructor
public class LockNextTriggerComponent {

    private static final Pageable SELECT_ONE = PageRequest.of(0, 1);
    private final TriggerRepository triggerRepository;
    
    public TriggerEntity loadNext(TaskSchedulerEntity runningOn, OffsetDateTime since) {
        final var tasks = triggerRepository.loadNextTasks(since, TriggerStatus.NEW, SELECT_ONE);
        if (tasks.isEmpty()) return null;

        final TriggerEntity taskInstance = tasks.get(0);
        taskInstance.runOn(runningOn);
        return taskInstance;
    }
}
