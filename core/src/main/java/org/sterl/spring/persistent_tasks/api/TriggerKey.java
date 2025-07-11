package org.sterl.spring.persistent_tasks.api;

import java.io.Serializable;
import java.time.OffsetDateTime;

import org.springframework.lang.Nullable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Unique key of a trigger during it's execution. But it after that the same key
 * can be added if needed. Ensures that only one trigger with the same key
 * is currently scheduled for execution.
 */
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerKey implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String taskName;
    
    public static TriggerKey of(@Nullable String id, TaskId<? extends Serializable> taskId) {
        return new TriggerKey(id, taskId.name());
    }

    public TaskId<Serializable> toTaskId() {
        if (taskName == null) return null;
        return new TaskId<>(taskName);
    }
    /**
     * Builds a trigger for the given persistentTask name
     */
    public TriggerKey(String taskName) {
        this.taskName = taskName;
    }

    /**
     * Just triggers the given persistentTask to be executed using <code>null</code> as state.
     */
    public <T extends Serializable> TriggerRequest<T> newTrigger(TaskId<T> taskId) {
        return newTrigger(taskId, null);
    }

    public <T extends Serializable> TriggerRequest<T> newTrigger(TaskId<T> taskId, T state) {
        return newTrigger(null, taskId, state);
    }

    public <T extends Serializable> TriggerRequest<T> newTrigger(String id, TaskId<T> taskId, T state) {
        return newTrigger(id, taskId, state, OffsetDateTime.now());
    }

    public <T extends Serializable> TriggerRequest<T> newTrigger(String id, TaskId<T> taskId, T state, OffsetDateTime when) {
        return taskId.newTrigger() //
                .id(id) //
                .state(state) //
                .when(when) //
                .build(); //
    }
}