package org.sterl.spring.persistent_tasks.api.task;

import java.io.Serializable;

/**
 * Similar to {@link PersistentTask} but specifically for transactional workloads.
 * Use this interface when the task execution should be wrapped in a transaction.
 *
 * <p>This interface ensures that the task's execution is transactional, meaning that it will
 * be executed within a transaction context, along with the state update and the dispatching of
 * relevant events.
 *
 * @param <T> the type of the state, which must be {@link Serializable}
 */
@FunctionalInterface
public interface TransactionalTask<T extends Serializable> extends PersistentTask<T> {
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
        return true;
    }
}
