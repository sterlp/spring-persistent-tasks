package org.sterl.spring.persistent_tasks.history;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.sterl.spring.persistent_tasks.shared.TimersEnabled;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@TimersEnabled
@Service
@RequiredArgsConstructor
@Slf4j
class HistoryTimer {

    private final HistoryService historyService;
    @Setter
    @Value("${spring.persistent-tasks.history.delete-after:PT720H}")
    private Duration historyTimeout = Duration.ofDays(30);

    @Scheduled(
            initialDelay = 1,
            fixedDelayString = "${spring.persistent-tasks.history.delete-rate:24}", timeUnit = TimeUnit.HOURS)
    void deleteOldHistory() {
        try {
            final var age = OffsetDateTime.now().minus(historyTimeout);
            var count = historyService.deleteAllOlderThan(age);
            log.debug("Deleted history {} older than {}.", count, historyTimeout);
        } catch (Exception e) {
            log.error("Failed to delete old triggers", e);
        }
    }
}
