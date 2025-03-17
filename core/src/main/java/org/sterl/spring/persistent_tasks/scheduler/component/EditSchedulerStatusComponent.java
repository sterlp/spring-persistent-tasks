package org.sterl.spring.persistent_tasks.scheduler.component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.scheduler.entity.SchedulerEntity;
import org.sterl.spring.persistent_tasks.scheduler.repository.TaskSchedulerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Transactional(timeout = 10)
@RequiredArgsConstructor
@Slf4j
public class EditSchedulerStatusComponent {
    private final OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
    private final TaskSchedulerRepository schedulerRepository;

    public SchedulerEntity checkinToRegistry(String name, int runningTasks, int maxTasks) {
        var result = get(name);

        result.setSystemLoadAverage(os.getSystemLoadAverage());
        result.setMaxHeap(memory.getHeapMemoryUsage().getMax());
        result.setUsedHeap(memory.getHeapMemoryUsage().getUsed());
        result.setRunningTasks(runningTasks);
        result.setTasksSlotCount(maxTasks);

        result.setLastPing(OffsetDateTime.now());
        log.debug("Ping {}", result);
        return schedulerRepository.save(result);
    }
    
    public void offline(String name) {
        schedulerRepository.deleteById(name);
    }

    public SchedulerEntity get(String name) {
        return schedulerRepository.findById(name)
                .orElseGet(() -> new SchedulerEntity(name));
    }
    
    public List<SchedulerEntity> listAll() {
        return schedulerRepository.listAll();
    }
    
    public Optional<SchedulerEntity> find(String name) {
        return schedulerRepository.findById(name);
    }

    public Set<String> findOnlineSchedulers(OffsetDateTime timeout) {
        schedulerRepository.deleteOldSchedulers(timeout);
        return schedulerRepository.findSchedulerNames();
    }
}
