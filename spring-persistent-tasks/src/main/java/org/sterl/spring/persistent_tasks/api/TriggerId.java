package org.sterl.spring.persistent_tasks.api;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TriggerId implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;

    public TaskId<Serializable> toTaskId() {
        return new TaskId<>(name);
    }
    /**
     * Builds a trigger for the given task name
     */
    public TriggerId(String taskName) {
        id = UUID.randomUUID().toString();
        this.name = taskName;
    }

    /**
     * Just triggers the given task to be executed using <code>null</code> as state.
     */
    public <T extends Serializable> AddTriggerRequest<T> newTrigger(TaskId<T> taskId) {
        return newTrigger(taskId, null);
    }

    public <T extends Serializable> AddTriggerRequest<T> newTrigger(TaskId<T> taskId, T state) {
        return newTrigger(UUID.randomUUID().toString(), taskId, state);
    }

    public <T extends Serializable> AddTriggerRequest<T> newTrigger(String id, TaskId<T> taskId, T state) {
        return newTrigger(id, taskId, state, OffsetDateTime.now());
    }

    public <T extends Serializable> AddTriggerRequest<T> newTrigger(String id, TaskId<T> taskId, T state, OffsetDateTime when) {
        return taskId.newTrigger() //
                .id(id) //
                .state(state) //
                .when(when) //
                .build();
    }
}