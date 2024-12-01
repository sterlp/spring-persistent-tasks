package org.sterl.spring.persistent_tasks.api;

import java.io.Serializable;
import java.util.function.Consumer;

@FunctionalInterface
public interface SpringBeanTask<T extends Serializable> extends Consumer<T> {
    void accept(T state);

    default RetryStrategy retryStrategy() {
        return RetryStrategy.TRY_THREE_TIMES;
    }
}
