package org.sterl.spring.persistent_tasks.api;

import java.io.Serializable;

/**
 * A spring persistentTask which state is saved in a {@link Trigger}.
 * 
 * @param <T> the state type
 */
@FunctionalInterface
public interface PersistentTask<T extends Serializable> {
    void accept(T state);

    default RetryStrategy retryStrategy() {
        return RetryStrategy.THREE_RETRIES;
    }
    
    /**
     * Whether the persistentTask is transaction or not. If <code>true</code> the execution
     * is wrapped into the default transaction template together with the state update
     * and the following events:
     * <ol>
     * <li>org.sterl.spring.persistent_tasks.trigger.event.TriggerRunningEvent</li>
     * <li>org.sterl.spring.persistent_tasks.trigger.event.TriggerSuccessEvent</li>
     * </ol>
     * @return {@code true} if the persistentTask is transactional; {@code false} otherwise.
     */
    default boolean isTransactional() {
        return false;
    }
}
