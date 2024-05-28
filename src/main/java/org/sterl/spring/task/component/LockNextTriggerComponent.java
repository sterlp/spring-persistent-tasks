package org.sterl.spring.task.component;

import java.time.OffsetDateTime;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.task.model.TaskTriggerEntity;
import org.sterl.spring.task.model.TaskStatus;
import org.sterl.spring.task.repository.TaskInstanceRepository;

import lombok.RequiredArgsConstructor;

@Component
@Transactional
@RequiredArgsConstructor
public class LockNextTriggerComponent {

    private static final Pageable SELECT_ONE = PageRequest.of(0, 1);
    private final TaskInstanceRepository taskInstanceRepository;
    
    public TaskTriggerEntity loadNext(String name, OffsetDateTime since) {
        final var tasks = taskInstanceRepository.loadNextTasks(since, TaskStatus.NEW, SELECT_ONE);
        if (tasks.isEmpty()) return null;

        final TaskTriggerEntity taskInstance = tasks.get(0);
        taskInstance.runOn(name);
        return taskInstance;
    }
}
