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
    
    /**
     * Whether the task is transaction or not. If <code>true</code> the execution
     * is wrapped into the default transaction template together with the state update
     * and the following events:
     * <ol>
     * <li>org.sterl.spring.persistent_tasks.trigger.event.TriggerRunningEvent</li>
     * <li>org.sterl.spring.persistent_tasks.trigger.event.TriggerSuccessEvent</li>
     * <li>org.sterl.spring.persistent_tasks.trigger.event.TriggerFailedEvent</li>
     * </ol>
     * @return {@code true} if the task is transactional; {@code false} otherwise.
     */
    default boolean isTransactional() {
        return false;
    }
}
