package org.sterl.spring.persistent_tasks.trigger;

import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.sterl.spring.persistent_tasks.shared.TimersEnabled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Timer component that triggers scheduled operations for the TriggerService.
 * Contains no business logic - only delegates to service and component classes.
 */
@TimersEnabled
@Component
@RequiredArgsConstructor
@Slf4j
public class TriggerTimer {

    private final TriggerService triggerService;

    /**
     * Checks for expired awaiting triggers and marks them as timed out.
     */
    @Scheduled(fixedDelayString = "${spring.persistent-tasks.poll-awaiting-trigger-timeout:300}", timeUnit = TimeUnit.SECONDS)
    void checkAwaitingTriggersForTimeout() {
        int expired = triggerService.expireTimeoutTriggers().size();
        if (expired > 0) log.info("{} triggers have not received the signal, wait expired!", expired);
        else log.debug("No expired triggers found.");
    }

    /**
     * Ensures all non-suspended cron triggers have corresponding database triggers.
     * Delegates to QueueCronTriggerComponent for actual logic.
     */
    @Scheduled(fixedDelayString = "${spring.persistent-tasks.queue-cron-triggers:60}", timeUnit = TimeUnit.SECONDS)
    void queueCronTrigger() {
        int created = triggerService.queueCronTrigger();
        if (created > 0) {
            log.info("Created {} cron triggers", created);
        } else {
            log.debug("No new cron triggers needed.");
        }
    }
}
