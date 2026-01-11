package org.sterl.spring.persistent_tasks.api;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.f4b6a3.uuid.UuidCreator;

/**
 * Represents the ID of a persistentTask, which is currently not running.
 */
public record TaskId<T extends Serializable>(String name) implements Serializable {
    
    @SuppressWarnings("rawtypes")
    private static final Map<String, TaskId> CACHE = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> TaskId<T> of(String taskId) {
        if (taskId == null || taskId.isBlank()) return null;
        return CACHE.computeIfAbsent(taskId, s -> new TaskId<>(s));
    }

    public TriggerBuilder<T> newTrigger() {
        return new TriggerBuilder<>(this);
    }
    
    public ConTriggerBuilder<T> newCron() {
        return new ConTriggerBuilder<>(this);
    }
    
    /**
     * Creates a new trigger, the ID will stay empty and
     * will receive an UUID later.
     */
    public TriggerBuilder<T> newTrigger(T state) {
        return new TriggerBuilder<>(this).state(state);
    }

    /**
     * Creates a new trigger with an UUID.
     */
    public TriggerRequest<T> newUniqueTrigger(T state) {
        return new TriggerBuilder<>(this)
                .state(state)
                .correlationId(UuidCreator.getTimeOrderedEpochFast().toString())
                .build();
    }
}
