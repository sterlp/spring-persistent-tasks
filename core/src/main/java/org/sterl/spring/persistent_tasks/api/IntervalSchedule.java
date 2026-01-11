package org.sterl.spring.persistent_tasks.api;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * A schedule based on a fixed time interval.
 * <p>
 * Examples:
 * <ul>
 *   <li>Duration.ofHours(1) - Every hour</li>
 *   <li>Duration.ofMinutes(30) - Every 30 minutes</li>
 *   <li>Duration.ofDays(1) - Every day</li>
 * </ul>
 */
public final class IntervalSchedule implements Schedule {
    private static final long serialVersionUID = 1L;

    private final Duration interval;

    /**
     * Creates a new interval schedule.
     *
     * @param interval the time interval between executions
     * @throws IllegalArgumentException if interval is null, zero, or negative
     */
    public IntervalSchedule(Duration interval) {
        if (interval == null) {
            throw new IllegalArgumentException("Interval cannot be null");
        }
        if (interval.isZero() || interval.isNegative()) {
            throw new IllegalArgumentException("Interval must be positive: " + interval);
        }
        this.interval = interval;
    }

    @Override
    public OffsetDateTime next(OffsetDateTime from) {
        return from.plus(interval).truncatedTo(ChronoUnit.SECONDS);
    }

    @Override
    public String description() {
        return "every(" + formatDuration(interval) + ")";
    }

    private String formatDuration(Duration d) {
        long days = d.toDays();
        if (days > 0 && d.equals(Duration.ofDays(days))) {
            return days + (days == 1 ? " day" : " days");
        }

        long hours = d.toHours();
        if (hours > 0 && d.equals(Duration.ofHours(hours))) {
            return hours + (hours == 1 ? " hour" : " hours");
        }

        long minutes = d.toMinutes();
        if (minutes > 0 && d.equals(Duration.ofMinutes(minutes))) {
            return minutes + (minutes == 1 ? " minute" : " minutes");
        }

        long seconds = d.getSeconds();
        return seconds + (seconds == 1 ? " second" : " seconds");
    }

    @Override
    public String toString() {
        return description();
    }

    public Duration getInterval() {
        return interval;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntervalSchedule that = (IntervalSchedule) o;
        return Objects.equals(interval, that.interval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interval);
    }
}
