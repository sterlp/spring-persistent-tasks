package org.sterl.spring.persistent_tasks.api.event;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.sterl.spring.persistent_tasks.api.AddTriggerRequest;
import org.sterl.spring.persistent_tasks.api.TaskId.TaskTriggerBuilder;

/**
 * An event to trigger one or multiple task executions
 */
public record TriggerTaskCommand<T extends Serializable>(Collection<AddTriggerRequest<T>> triggers) {

    public static <T extends Serializable> TriggerTaskCommand<T> of(String name, T state) {
        return new TriggerTaskCommand<>(Collections.singleton(TaskTriggerBuilder
                .<T>newTrigger(name)
                .state(state)
                .build()));
    }

    public static <T extends Serializable> TriggerTaskCommand<T> of(AddTriggerRequest<T> trigger) {
        return new TriggerTaskCommand<>(Collections.singleton(trigger));
    }

    @SafeVarargs
    public static <T extends Serializable> TriggerTaskCommand<T> of(AddTriggerRequest<T>... triggers) {
        return new TriggerTaskCommand<>(Arrays.asList(triggers));
    }
}
