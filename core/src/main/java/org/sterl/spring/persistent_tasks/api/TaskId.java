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

    public TriggerRequest<T> newUniqueTrigger(T state) {
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
        private String correlationId;
        private String tag;
        private TriggerStatus status = TriggerStatus.WAITING;
        private T state;
        private OffsetDateTime when = OffsetDateTime.now();
        private int priority = TriggerRequest.DEFAULT_PRIORITY;

        public static <T extends Serializable> TriggerBuilder<T> newTrigger(String name) {
            return new TriggerBuilder<>(new TaskId<T>(name));
        }
        public static <T extends Serializable> TriggerBuilder<T> newTrigger(String name, T state) {
            return new TriggerBuilder<>(new TaskId<T>(name)).state(state);
        }
        public static <T extends Serializable> TriggerBuilder<T> newTrigger(TriggerKey key, T state) {
            return new TriggerBuilder<>(new TaskId<T>(key.getTaskName())).id(key.getId()).state(state);
        }
        public TriggerRequest<T> build() {
            var key = TriggerKey.of(id, taskId);
            return new TriggerRequest<>(key, status, state, when, priority, correlationId, tag);
        }
        /**
         * The ID of this task, same queued ids are replaced.
         */
        public TriggerBuilder<T> id(String id) {
            this.id = id;
            return this;
        }
        /**
         * An unique ID which is taken over to a chain/set of tasks.
         * If task is triggered it in a task, this ID is taken over.
         */
        public TriggerBuilder<T> correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }
        public TriggerBuilder<T> tag(String tag) {
            this.tag = tag;
            return this;
        }
        public TriggerBuilder<T> state(T state) {
            this.state = state;
            return this;
        }
        /**
         * The higher the {@link #priority} the earlier this task is picked.
         * Same as JMS priority. Default is also 4, like in JMS.
         * 
         * @param priority custom priority e.g. 0-9, also higher numbers are supported
         * @return this {@link TriggerBuilder}
         */
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
            this.status = TriggerStatus.WAITING;
            return this;
        }
        /**
         * synonym for {@link #runAt(OffsetDateTime)}
         */
        public TriggerBuilder<T> runAfter(Duration duration) {
            runAt(OffsetDateTime.now().plus(duration));
            return this;
        }
        /**
         * Creates a trigger which waits for an external signal and
         * will run into {@link TriggerStatus#EXPIRED_SIGNAL} if no signal happens.
         */
        public TriggerBuilder<T> waitForSignal(OffsetDateTime timeout) {
            this.when = timeout;
            this.status = TriggerStatus.AWAITING_SIGNAL;
            return this;
        }
    }
}
