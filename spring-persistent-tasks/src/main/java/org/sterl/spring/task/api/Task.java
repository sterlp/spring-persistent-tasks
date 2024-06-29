package org.sterl.spring.task.api;

import java.io.Serializable;

import org.sterl.spring.task.api.TaskId.TaskTriggerBuilder;

public interface Task<T extends Serializable> {
    TaskResult execute(T state);
    TaskId<T> getId();

    default RetryStrategy retryStrategy() {
        return RetryStrategy.TRY_THREE_TIMES;
    }
    default TaskTriggerBuilder<T> newTrigger() {
        return getId().newTrigger();
    }
}
