package org.sterl.spring.persistent_tasks.api;

import java.time.OffsetDateTime;

public record TaskHistoryOverview(
        String taskName,
        long executionCount,
        OffsetDateTime firstRun,
        OffsetDateTime lastRun,
        double maxDurationMs,
        double minDurationMs,
        double avgDurationMs
        ) {
}
