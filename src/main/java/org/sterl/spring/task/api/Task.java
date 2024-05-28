package org.sterl.spring.task.api;

import java.io.Serializable;

public interface Task<T extends Serializable> {
    TaskResult execute(T state);
    TaskId<T> getId();

    default RetryStrategy retryStrategy() {
        return RetryStrategy.TRY_THREE_TIMES;
    }
}
