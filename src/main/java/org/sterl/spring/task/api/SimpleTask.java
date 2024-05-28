package org.sterl.spring.task.api;

import java.io.Serializable;
import java.util.function.Consumer;

@FunctionalInterface
public interface SimpleTask<T extends Serializable> extends Consumer<T> {
    void accept(T state);
    
    default RetryStrategy retryStrategy() {
        return RetryStrategy.TRY_THREE_TIMES;
    }
}
