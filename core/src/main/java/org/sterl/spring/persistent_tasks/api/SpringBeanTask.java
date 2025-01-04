package org.sterl.spring.persistent_tasks.api;

import java.io.Serializable;

/**
 * Same as {@link PersistentTask}
 */
@Deprecated
@FunctionalInterface
public interface SpringBeanTask<T extends Serializable> extends PersistentTask<T> {
    
}
