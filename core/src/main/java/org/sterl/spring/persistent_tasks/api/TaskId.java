package org.sterl.spring.persistent_tasks.api;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Represents the ID of a persistentTask, which is currently not running.
 */
public record TaskId<T extends Serializable>(String name) implements Serializable {

    public TriggerBuilder<T> newTrigger() {
        return new TriggerBuilder<>(this);
    }
    
    public TriggerBuilder<T> newTrigger(T state) {
        return new TriggerBuilder<>(this).state(state);
    }

    public AddTriggerRequest<T> newUniqueTrigger(T state) {
        return new TriggerBuilder<>(this).state(state).build();
    }

    public static TaskId<Serializable> of(String taskId) {
        if (taskId == null || taskId.isBlank()) return null;
        return new TaskId<>(taskId);
    }
    
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TriggerBuilder<T extends Serializable> {
        private final TaskId<T> taskId;
        private String id;
        private T state;
        private OffsetDateTime when = OffsetDateTime.now();
        private int priority = AddTriggerRequest.DEFAULT_PRIORITY;

        public static <T extends Serializable> TriggerBuilder<T> newTrigger(String name) {
            return new TriggerBuilder<>(new TaskId<T>(name));
        }
        public static <T extends Serializable> TriggerBuilder<T> newTrigger(String name, T state) {
            return new TriggerBuilder<>(new TaskId<T>(name)).state(state);
        }
        public AddTriggerRequest<T> build() {
            var key = TriggerKey.of(id, taskId);
            return new AddTriggerRequest<>(key, state, when, priority);
        }
        public TriggerBuilder<T> id(String id) {
            this.id = id;
            return this;
        }
        public TriggerBuilder<T> state(T state) {
            this.state = state;
            return this;
        }
        public TriggerBuilder<T> priority(int priority) {
            this.priority = priority;
            return this;
        }
        /**
         * synonym for {@link #runAt(OffsetDateTime)}
         */
        public TriggerBuilder<T> when(OffsetDateTime when) {
            return runAt(when);
        }
        public TriggerBuilder<T> runAt(OffsetDateTime when) {
            this.when = when;
            return this;
        }
        public TriggerBuilder<T> runAfter(Duration duration) {
            runAt(OffsetDateTime.now().plus(duration));
            return this;
        }
    }
}
