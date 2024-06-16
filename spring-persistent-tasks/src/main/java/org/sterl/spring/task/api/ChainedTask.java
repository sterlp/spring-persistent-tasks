package org.sterl.spring.task.api;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

@FunctionalInterface
public interface ChainedTask<T extends Serializable> {
    Optional<Collection<TaskTrigger<?>>> execute(T state);
}
