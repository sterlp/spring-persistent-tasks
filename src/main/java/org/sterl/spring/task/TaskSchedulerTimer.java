package org.sterl.spring.task;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskSchedulerTimer {

    private final Collection<TaskSchedulerService> schedulerServices;

    @Scheduled(fixedDelayString = "${persistent-timer.poll-rate:30}", timeUnit = TimeUnit.SECONDS)
    void pollTriggerNexTask() {
        for (TaskSchedulerService s : schedulerServices) {
            try {
                s.triggerNexTask();
            } catch (Exception e) {
                log.error("Scheduler {} failed to trigger next tasks", s.getClass(), e);
            }
        }
    }
}
