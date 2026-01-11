package org.sterl.spring.persistent_tasks.api;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Represents a schedule that can calculate the next execution time.
 * Used for recurring tasks (cron expressions or fixed intervals).
 */
public interface Schedule extends Serializable {

    /**
     * Calculates the next execution time from the given time.
     *
     * @param from the reference time to calculate from
     * @return the next execution time after the given time
     */
    OffsetDateTime next(OffsetDateTime from);

    /**
     * Returns a human-readable description of this schedule.
     *
     * @return description of the schedule
     */
    String description();
}
