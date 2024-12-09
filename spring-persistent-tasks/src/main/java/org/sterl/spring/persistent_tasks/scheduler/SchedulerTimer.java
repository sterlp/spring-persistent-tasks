package org.sterl.spring.persistent_tasks.scheduler;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.sterl.spring.persistent_tasks.scheduler.config.ConditionalSchedulerServiceByProperty;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ConditionalSchedulerServiceByProperty
@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerTimer {

    @Value("${persistent-tasks.task-timeout:PT5M}")
    private Duration taskTimeout = Duration.ofMinutes(5);
    private final Collection<SchedulerService> schedulerServices;

    @Scheduled(fixedDelayString = "${persistent-tasks.poll-rate:30}", timeUnit = TimeUnit.SECONDS)
    void triggerNextTasks() {
        for (SchedulerService s : schedulerServices) {
            try {
                final var count = s.triggerNextTasks().size();
                log.debug("Triggered {} tasks for {}.", count, s.getName());
            } catch (Exception e) {
                log.error("Scheduler {} failed to trigger next tasks", s.getName(), e);
            }
        }
    }

    @Scheduled(fixedDelayString = "${persistent-tasks.poll-task-timeout:300}", timeUnit = TimeUnit.SECONDS)
    void rescheduleAbandonedTasks() {
        for (SchedulerService s : schedulerServices) {
            try {
                final var count = s.rescheduleAbandonedTasks(taskTimeout);
                log.debug("Found {} abandoned tasks for {}.", count.size(), s.getName());
            } catch (Exception e) {
                log.error("Scheduler {} failed schedule abandoned tasks", s.getName(), e);
            }
        }
    }

    @Scheduled(fixedDelayString = "${persistent-tasks.clean-trigger-rate:7200}", timeUnit = TimeUnit.SECONDS)
    void cleanupFinishedTriggers() {
        // TODO
    }
}
