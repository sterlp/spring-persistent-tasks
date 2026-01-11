package org.sterl.spring.persistent_tasks.api;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.scheduling.support.CronExpression;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * A schedule based on a cron expression. Always uses UTC timezone.
 * <p>
 * Examples:
 * <ul>
 *   <li>"0 0 2 * * *" - Every day at 2:00 AM UTC</li>
 *   <li>"0 0 * * * *" - Every hour on the hour</li>
 *   <li>"0 *&#47;15 * * * *" - Every 15 minutes</li>
 * </ul>
 */
@EqualsAndHashCode(of = "expression")
@ToString(of = "expression")
public final class CronSchedule implements Schedule {
    private static final long serialVersionUID = 1L;

    @Getter
    private final String expression;
    private transient CronExpression cronExpression;

    /**
     * Creates a new cron schedule.
     *
     * @param expression the cron expression (6 fields: second minute hour day month weekday)
     * @throws IllegalArgumentException if the cron expression is invalid
     */
    public CronSchedule(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("Cron expression cannot be null or empty");
        }
        this.expression = expression.trim();
        this.cronExpression = CronExpression.parse(this.expression);
    }

    @Override
    public OffsetDateTime next(OffsetDateTime from) {
        if (cronExpression == null) {
            cronExpression = CronExpression.parse(expression);
        }

        var next = cronExpression.next(OffsetDateTime.now());

        if (next == null) {
            throw new IllegalStateException("No next execution time found for cron: " + expression);
        }

        return next.truncatedTo(ChronoUnit.SECONDS);
    }

    @Override
    public String description() {
        return "cron(" + expression + ")";
    }
}
