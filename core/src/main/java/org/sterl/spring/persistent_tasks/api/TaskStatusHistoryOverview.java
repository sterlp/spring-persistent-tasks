package org.sterl.spring.persistent_tasks.api;

import java.time.OffsetDateTime;

public record TaskStatusHistoryOverview(
        String taskName,
        TriggerStatus status,
        Long executionCount,
        OffsetDateTime firstRun,
        OffsetDateTime lastRun,
        Number maxDurationMs,
        Number minDurationMs,
        Number avgDurationMs,
        Number avgExecutionCount
        ) {
}