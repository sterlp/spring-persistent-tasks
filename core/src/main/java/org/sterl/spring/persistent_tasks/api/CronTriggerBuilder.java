package org.sterl.spring.persistent_tasks.api;

import java.io.Serializable;
import java.time.Duration;
import java.util.function.Supplier;

import org.sterl.spring.persistent_tasks.trigger.model.CronTriggerEntity;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Fluent builder for creating scheduled (recurring) triggers.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class CronTriggerBuilder<T extends Serializable> {
    @NonNull
    private final TaskId<T> taskId;
    private String id;
    private Schedule schedule;
    private Supplier<T> stateProvider;
    private String tag;
    private int priority = TriggerRequest.DEFAULT_PRIORITY;
    
    /**
     * Sets the unique ID for this scheduled trigger.
     * If not set, defaults to {@link Schedule#description()}.
     */
    public CronTriggerBuilder<T> id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Sets a fixed state for all trigger executions.
     */
    public CronTriggerBuilder<T> state(T state) {
        this.stateProvider = () -> state;
        return this;
    }

    /**
     * Sets a state provider that's called for each trigger execution.
     * Useful for dynamic state generation.
     */
    public CronTriggerBuilder<T> stateProvider(Supplier<T> stateProvider) {
        this.stateProvider = stateProvider;
        return this;
    }

    /**
     * Sets the schedule using a cron expression.
     *
     * @param cronExpression cron expression (6 fields: second minute hour day month weekday)
     */
    public CronTriggerBuilder<T> cron(String cronExpression) {
        this.schedule = new CronSchedule(cronExpression);
        return this;
    }

    /**
     * Sets the schedule using a fixed interval.
     *
     * @param interval duration between executions
     */
    public CronTriggerBuilder<T> every(Duration interval) {
        this.schedule = new IntervalSchedule(interval);
        return this;
    }

    /**
     * Sets a custom tag for created triggers.
     */
    public CronTriggerBuilder<T> tag(String tag) {
        this.tag = tag;
        return this;
    }

    /**
     * Sets the priority for created triggers (higher = more important).
     */
    public CronTriggerBuilder<T> priority(int priority) {
        this.priority = priority;
        return this;
    }

    public CronTriggerEntity<T> build() {
        if (schedule == null) {
            throw new IllegalStateException("Schedule must be set using .cron() or .every()");
        }
        if (id == null) {
            id = schedule.description();
        }

        return CronTriggerEntity.<T>builder()
                .id(id)
                .taskId(taskId)
                .schedule(schedule)
                .stateProvider(stateProvider)
                .tag(tag == null ? "cron" : tag)
                .priority(priority)
                .build();

    }
}