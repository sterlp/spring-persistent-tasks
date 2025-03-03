package org.sterl.spring.persistent_tasks.api.task;

import java.io.Serializable;

import org.springframework.lang.Nullable;
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
public interface PersistentTask<T extends Serializable> extends PersistentTaskBase<T> {
    void accept(@Nullable T state);
}
