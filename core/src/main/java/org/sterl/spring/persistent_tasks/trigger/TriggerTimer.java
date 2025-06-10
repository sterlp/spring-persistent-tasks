package org.sterl.spring.persistent_tasks.trigger;

import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.sterl.spring.persistent_tasks.shared.TimersEnabled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@TimersEnabled
@Component
@RequiredArgsConstructor
@Slf4j
public class TriggerTimer {
    
    private final TriggerService triggerService;

    @Scheduled(fixedDelayString = "${spring.persistent-tasks.poll-awaiting-trigger-timeout:300}", timeUnit = TimeUnit.SECONDS)
    void checkAwaitingTriggersForTimeout() {
        int expired = triggerService.expireTimeoutTriggers().size();
        if (expired > 0) log.info("{} triggers have not received the signal, wait expired!", expired);
        else log.debug("No expired triggers found.");
    }
}
