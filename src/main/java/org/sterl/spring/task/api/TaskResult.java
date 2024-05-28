package org.sterl.spring.task.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public record TaskResult(Collection<TaskTrigger<?>> triggers) {

    public static final TaskResult DONE = new TaskResult(Collections.emptyList());
    
    public static TaskResult of(TaskTrigger<?> trigger) {
        if (trigger == null) return DONE;
        return new TaskResult(Arrays.asList(trigger));
    }
}
