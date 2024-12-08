package org.sterl.spring.persistent_tasks.scheduler.component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.Duration;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.scheduler.entity.OnlineSchedulersEntity;
import org.sterl.spring.persistent_tasks.scheduler.entity.SchedulerEntity;
import org.sterl.spring.persistent_tasks.scheduler.entity.SchedulerEntity.TaskSchedulerStatus;
import org.sterl.spring.persistent_tasks.scheduler.repository.TaskSchedulerRepository;

import lombok.RequiredArgsConstructor;

@Component
@Transactional(timeout = 10)
@RequiredArgsConstructor
public class EditSchedulerStatusComponent {
    private final OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
    private final TaskSchedulerRepository schedulerRepository;
    private final TaskExecutorComponent taskExecutor;

    public SchedulerEntity checkinToRegistry(String name, TaskSchedulerStatus status) {
        var result = get(name);

        result.setStatus(status);
        result.setRunnungTasks(taskExecutor.getRunningTasks());
        result.setTasksSlotCount(taskExecutor.getMaxThreads());

        result.setSystemLoadAverage(os.getSystemLoadAverage());
        result.setMaxHeap(memory.getHeapMemoryUsage().getMax());
        result.setUsedHeap(memory.getHeapMemoryUsage().getUsed());

        result.setLastPing(OffsetDateTime.now());

        return schedulerRepository.save(result);
    }

    public SchedulerEntity get(String name) {
        return schedulerRepository.findById(name)
                .orElseGet(() -> new SchedulerEntity(name));
    }

    public OnlineSchedulersEntity findOnlineSchedulers(Duration lastPingWas) {
        final var timeout = OffsetDateTime.now().minus(lastPingWas);
        int countOffline = schedulerRepository.setSchedulersStatusByLastPing(timeout, TaskSchedulerStatus.OFFLINE);
        var online = schedulerRepository.findIdByStatus(TaskSchedulerStatus.ONLINE);
        return new OnlineSchedulersEntity(online, countOffline);
    }
}
