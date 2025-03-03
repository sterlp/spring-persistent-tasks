package org.sterl.spring.persistent_tasks.api.event;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.sterl.spring.persistent_tasks.api.AddTriggerRequest;
import org.sterl.spring.persistent_tasks.api.TaskId.TriggerBuilder;

/**
 * An event to trigger one or multiple persistentTask executions
 */
public record TriggerTaskCommand<T extends Serializable>(
        Collection<AddTriggerRequest<T>> triggers) implements PersistentTasksEvent {
    
    public int size() {
        return triggers == null ? 0 : triggers.size();
    }

    public static <T extends Serializable> TriggerTaskCommand<T> of(String name, T state) {
        return new TriggerTaskCommand<>(Collections.singleton(TriggerBuilder
                .<T>newTrigger(name)
                .state(state)
                .build()));
    }

    public static <T extends Serializable> TriggerTaskCommand<T> of(Collection<AddTriggerRequest<T>> triggers) {
        return new TriggerTaskCommand<>(triggers);
    }

    public static <T extends Serializable> TriggerTaskCommand<T> of(AddTriggerRequest<T> trigger) {
        return new TriggerTaskCommand<>(Collections.singleton(trigger));
    }

    @SafeVarargs
    public static <T extends Serializable> TriggerTaskCommand<T> of(AddTriggerRequest<T>... triggers) {
        return new TriggerTaskCommand<>(Arrays.asList(triggers));
    }
}
