package org.sterl.spring.task.api;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Represents the ID of a task, which is currently not running.
 */
public record TaskId<T extends Serializable>(String name, String group) implements Serializable {
    public static final String DEFAULT_GROUP = "none";

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TaskTriggerBuilder<T extends Serializable> {
        private final TaskId<T> taskId;
        private String id;
        private T state;
        private OffsetDateTime when = OffsetDateTime.now(); 
        private int priority = TaskTrigger.DEFAULT_PRIORITY;
        
        public static <T extends Serializable> TaskTriggerBuilder<T> newTrigger(String name) {
            return new TaskTriggerBuilder<>(new TaskId<T>(name, DEFAULT_GROUP));
        }
        public TaskTrigger<T> build() {
            return new TaskTrigger<T>(
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
        public TaskTriggerBuilder<T> when(OffsetDateTime when) {
            this.when = when;
            return this;
        }
    }
    public TaskTriggerBuilder<T> newTrigger() {
        return new TaskTriggerBuilder<>(this);
    }
}
