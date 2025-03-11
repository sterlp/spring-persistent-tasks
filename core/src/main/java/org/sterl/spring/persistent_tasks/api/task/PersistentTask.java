package org.sterl.spring.persistent_tasks.api.task;

import java.io.Serializable;

import org.springframework.lang.Nullable;
import org.sterl.spring.persistent_tasks.api.RetryStrategy;
import org.sterl.spring.persistent_tasks.api.Trigger;

/**
 * A Spring persistent task whose state is saved in a {@link Trigger}.
 *
 * <p>This interface defines a task that accepts a state of type <code>T</code> and
 * provides default implementations for retry strategies.
 *
 * @param <T> the type of the state, which must be {@link Serializable}
 */
@FunctionalInterface
public interface PersistentTask<T extends Serializable> {

    /**
     * Called during the task execution with the stored state.
     * <ul>
     *  <li>
     *  {@link RunningTriggerContextHolder} can be used to access the full state.
     *  </li>
     *  <li>
     *  Fire {@link org.sterl.spring.persistent_tasks.api.event.TriggerTaskCommand} events to schedule new tasks.
     *  </li>
     *  <li>
     *  Consider to use a {@link TransactionalTask} in case the triggers or the state should be written together in one transaction.
     *  </li>
     * </ul>
     * @param state the state of this trigger, can be <code>null</code>
     */
    void accept(@Nullable T state);

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
