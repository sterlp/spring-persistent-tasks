package org.sterl.spring.persistent_tasks.api;

import java.time.OffsetDateTime;

public record TriggerGroup(
        long count, String groupByValue, 
        long sumDurationMs, int sumRunCount,
        OffsetDateTime minRunAt, OffsetDateTime minCreatedTime,
        OffsetDateTime minStart, OffsetDateTime maxEnd) 
{}
