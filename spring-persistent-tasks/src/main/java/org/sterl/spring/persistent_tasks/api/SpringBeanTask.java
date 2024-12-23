package org.sterl.spring.persistent_tasks.api;

import java.io.Serializable;
import java.util.function.Consumer;

@FunctionalInterface
public interface SpringBeanTask<T extends Serializable> extends Consumer<T> {
    @Override
    void accept(T state);

    default RetryStrategy retryStrategy() {
        return RetryStrategy.THREE_RETRIES;
    }
}
