package org.sterl.spring.task.api.event;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.sterl.spring.task.api.Trigger;
import org.sterl.spring.task.api.TaskId.TaskTriggerBuilder;

public record TriggerTaskEvent<T extends Serializable>(Collection<Trigger<T>> triggers) {

    public static <T extends Serializable> TriggerTaskEvent<T> of(String name, T state) {
        return new TriggerTaskEvent<T>(Collections.singleton(TaskTriggerBuilder
                .<T>newTrigger(name)
                .state(state)
                .build()));
    }
    
    public static <T extends Serializable> TriggerTaskEvent<T> of(Trigger<T> trigger) {
        return new TriggerTaskEvent<T>(Collections.singleton(trigger));
    }

    @SafeVarargs
    public static <T extends Serializable> TriggerTaskEvent<T> of(Trigger<T>... triggers) {
        return new TriggerTaskEvent<T>(Arrays.asList(triggers));
    }
}
