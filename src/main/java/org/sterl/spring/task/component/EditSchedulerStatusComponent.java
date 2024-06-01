package org.sterl.spring.task.component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.Duration;
import java.time.OffsetDateTime;

import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.task.model.TaskSchedulerEntity;
import org.sterl.spring.task.model.TaskSchedulerEntity.TaskSchedulerStatus;
import org.sterl.spring.task.repository.TaskSchedulerRepository;

import lombok.RequiredArgsConstructor;

@Transactional
@RequiredArgsConstructor
public class EditSchedulerStatusComponent {
    private final OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
    private final TaskSchedulerRepository schedulerRepository;
    private final TransactionalTaskExecutorComponent taskExecutor;

    public TaskSchedulerEntity checkinToRegistry(String name, TaskSchedulerStatus status) {
        
        var result = schedulerRepository.findById(name)
                .orElseGet(() -> new TaskSchedulerEntity(name));

        result.setStatus(status);
        result.setRunnungTasks(taskExecutor.getRunningTasks());
        result.setTasksSlotCount(taskExecutor.getMaxTasks());
        
        result.setSystemLoadAverage(os.getSystemLoadAverage());
        result.setMaxHeap(memory.getHeapMemoryUsage().getMax());
        result.setUsedHeap(memory.getHeapMemoryUsage().getUsed());
        
        return schedulerRepository.save(result);
    }

    public int setSchedulersOffline(Duration lastPingWas) {
        final var timeout = OffsetDateTime.now().minus(lastPingWas);
        return schedulerRepository.setSchedulersStatusByLastPing(timeout, TaskSchedulerStatus.OFFLINE);
    }
}
