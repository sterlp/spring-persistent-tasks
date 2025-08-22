package org.sterl.spring.persistent_tasks.scheduler;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.sterl.spring.persistent_tasks.scheduler.config.ConditionalSchedulerServiceByProperty;
import org.sterl.spring.persistent_tasks.shared.TimersEnabled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@TimersEnabled
@ConditionalSchedulerServiceByProperty
@Service
@RequiredArgsConstructor
@Slf4j
class SchedulerTimer {

    @Value("${spring.persistent-tasks.trigger-timeout:PT5M}")
    private Duration taskTimeout = Duration.ofMinutes(5);
    private final Collection<SchedulerService> schedulerServices;

    @Scheduled(fixedDelayString = "${spring.persistent-tasks.poll-rate:60}", timeUnit = TimeUnit.SECONDS)
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

    @Scheduled(fixedDelayString = "${spring.persistent-tasks.poll-abandoned-triggers:300}", timeUnit = TimeUnit.SECONDS)
    void rescheduleAbandonedTasks() {
        var timeout = OffsetDateTime.now().minus(taskTimeout);
        for (SchedulerService s : schedulerServices) {
            try {
                final var count = s.rescheduleAbandonedTriggers(timeout);
                log.info("Found {} abandoned tasks for {}. Timeout={}", 
                        count.size(), s.getName(), taskTimeout);
            } catch (Exception e) {
                log.error("Scheduler {} failed schedule abandoned tasks", s.getName(), e);
            }
        }
    }
}
