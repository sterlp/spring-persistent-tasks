package org.sterl.spring.persistent_tasks.api.task;

import java.io.Serializable;

import org.sterl.spring.persistent_tasks.api.AddTriggerRequest;
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
public interface ComplexPersistentTask<T extends Serializable> extends PersistentTaskBase<T> {

    /**
     * Default execution method of a trigger, which also allows to queue the next trigger as needed.
     * @param <R> the state type of the next trigger
     * @param data the data of the current trigger
     * @return optional next trigger to queue, <code>null</code> means done.
     */
    <R extends Serializable> AddTriggerRequest<R> accept(RunningTrigger<T> data);
}
