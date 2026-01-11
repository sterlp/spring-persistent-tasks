package org.sterl.spring.persistent_tasks.trigger.model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.function.Supplier;

import org.sterl.spring.persistent_tasks.api.Schedule;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerRequest;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import lombok.With;

/**
 * Represents a registered cron trigger definition (in-memory).
 * Cron triggers are periodically checked and create actual trigger instances in the database.
 *
 * @param <T> the state type for the task
 */
@Value
@Builder
@ToString(of = {"id", "taskId", "schedule", "suspended"})
public class CronTriggerEntity<T extends Serializable> {

    /**
     * Unique identifier for this cron trigger.
     * Used as the trigger ID when creating database trigger instances.
     */
    String id;

    /**
     * The task to execute on schedule.
     */
    TaskId<T> taskId;

    /**
     * The schedule (cron expression or interval).
     */
    Schedule schedule;

    /**
     * Optional state provider - called each time a trigger is created.
     * If null, trigger will have null state.
     */
    Supplier<T> stateProvider;

    String tag;

    /**
     * Priority for created triggers (0-9, higher = more important).
     */
    @Builder.Default
    int priority = 4;

    /**
     * If <code>true</code>, this cron trigger is suspended and won't create new triggers.
     */
    @With
    @Builder.Default
    boolean suspended = false;
    
    public TriggerKey key() {
        return TriggerKey.of(id, taskId);
    }
    
    public TriggerRequest<T> newTriggerRequest() {
        var nextRun = schedule.next(OffsetDateTime.now());
        
        return taskId.newTrigger(stateProvider == null ? null : stateProvider.get())
            .id(getId())
            .runAt(nextRun)
            .tag(getTag())
            .priority(getPriority())
            .build();
    }
}
