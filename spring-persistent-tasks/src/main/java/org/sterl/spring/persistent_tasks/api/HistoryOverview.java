package org.sterl.spring.persistent_tasks.api;

import java.time.OffsetDateTime;

import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;

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
