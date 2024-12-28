package org.sterl.spring.persistent_tasks.api;

import java.time.OffsetDateTime;

public record HistoryOverview(
        long instanceId,
        String taskName,
        long entryCount,
        OffsetDateTime start,
        OffsetDateTime end,
        OffsetDateTime createdTime,
        long executionCount,
        double runningDurationInMs
        ) {
}
