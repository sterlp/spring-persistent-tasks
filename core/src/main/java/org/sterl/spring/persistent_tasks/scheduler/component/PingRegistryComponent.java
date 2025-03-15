package org.sterl.spring.persistent_tasks.scheduler.component;

import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.scheduler.entity.SchedulerEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Ping method of a scheduler
 */
@RequiredArgsConstructor
@Slf4j
public class PingRegistryComponent {
    private final String schedulerName;
    private final TaskExecutorComponent taskExecutor;
    private final EditSchedulerStatusComponent editSchedulerStatus;

    @Transactional(timeout = 5)
    public SchedulerEntity execute() {
        var result = editSchedulerStatus.checkinToRegistry(schedulerName);
        result.setRunnungTasks(taskExecutor.getRunningTasks());
        result.setTasksSlotCount(taskExecutor.getMaxThreads());
        log.debug("Ping {}", result);
        return result;
    }
}
