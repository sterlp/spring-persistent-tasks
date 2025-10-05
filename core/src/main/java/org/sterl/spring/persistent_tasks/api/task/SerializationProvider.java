package org.sterl.spring.persistent_tasks.api.task;

import java.io.Serializable;

/**
 * /**
 * Any task may provide an own serialization class.
 * 
 * @since 2.3.0
 * @param <T> the type of the task state
 */
public interface SerializationProvider<T extends Serializable> {
    StateSerializer<T> getSerializer();
}
