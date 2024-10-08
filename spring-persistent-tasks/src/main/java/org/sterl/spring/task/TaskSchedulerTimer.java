package org.sterl.spring.task;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskSchedulerTimer {

    @Value("${persistent-timer.task-timeout:PT5M}")
    private Duration taskTimeout = Duration.ofMinutes(5);
    private final Collection<TaskSchedulerService> schedulerServices;

    @Scheduled(fixedDelayString = "${persistent-timer.poll-rate:30}", timeUnit = TimeUnit.SECONDS)
    void pollTriggerNexTask() {
        for (TaskSchedulerService s : schedulerServices) {
            try {
                final var count = s.triggerTasksForAllThreads();
                log.debug("Triggered {} tasks for {}.", count.size(), s.getName());
            } catch (Exception e) {
                log.error("Scheduler {} failed to trigger next tasks", s.getName(), e);
            }
        }
    }

    @Scheduled(fixedDelayString = "${persistent-timer.poll-task-timeout:300}", timeUnit = TimeUnit.SECONDS)
    void rescheduleAbandonedTasks() {
        for (TaskSchedulerService s : schedulerServices) {
            try {
                final var count = s.rescheduleAbandonedTasks(taskTimeout);
                log.debug("Found {} abandoned tasks for {}.", count.size(), s.getName());
            } catch (Exception e) {
                log.error("Scheduler {} failed schedule abandoned tasks", s.getName(), e);
            }
        }
    }
    
    @Scheduled(fixedDelayString = "${persistent-timer.clean-trigger-rate:7200}", timeUnit = TimeUnit.SECONDS)
    void cleanupFinishedTriggers() {
        // TODO
    }
}
