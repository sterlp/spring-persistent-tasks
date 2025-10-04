package org.sterl.spring.persistent_tasks.api.task;

import java.io.Serializable;

/**
 * Any task may provide an own serialization class.
 */
public interface SerializationProvider<T extends Serializable> {
    StateSerializer<T> getSerializer();
}
