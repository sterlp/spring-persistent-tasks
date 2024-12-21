package org.sterl.spring.persistent_tasks.api;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Represents the ID of a task, which is currently not running.
 */
public record TaskId<T extends Serializable>(String name) implements Serializable {

    public TaskTriggerBuilder<T> newTrigger() {
        return new TaskTriggerBuilder<>(this);
    }

    public AddTriggerRequest<T> newUniqueTrigger(T state) {
        return new TaskTriggerBuilder<>(this).state(state).build();
    }

    @SuppressWarnings("rawtypes")
    public static TaskId<?> of(String taskId) {
        if (taskId == null || taskId.isBlank()) return null;
        return new TaskId(taskId);
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TaskTriggerBuilder<T extends Serializable> {
        private final TaskId<T> taskId;
        private String id;
        private T state;
        private OffsetDateTime when = OffsetDateTime.now();
        private int priority = AddTriggerRequest.DEFAULT_PRIORITY;

        public static <T extends Serializable> TaskTriggerBuilder<T> newTrigger(String name) {
            return new TaskTriggerBuilder<>(new TaskId<T>(name));
        }
        public AddTriggerRequest<T> build() {
            return new AddTriggerRequest<>(
                    id == null ? UUID.randomUUID().toString() : id,
                    taskId, state, when, priority);
        }
        public TaskTriggerBuilder<T> id(String id) {
            this.id = id;
            return this;
        }
        public TaskTriggerBuilder<T> state(T state) {
            this.state = state;
            return this;
        }
        public TaskTriggerBuilder<T> priority(int priority) {
            this.priority = priority;
            return this;
        }
        /**
         * synonym for {@link #runAt(OffsetDateTime)}
         */
        public TaskTriggerBuilder<T> when(OffsetDateTime when) {
            return runAt(when);
        }
        public TaskTriggerBuilder<T> runAt(OffsetDateTime when) {
            this.when = when;
            return this;
        }
    }
}
