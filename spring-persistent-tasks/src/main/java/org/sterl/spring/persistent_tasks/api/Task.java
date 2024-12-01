package org.sterl.spring.persistent_tasks.api;

import java.io.Serializable;

import org.sterl.spring.persistent_tasks.api.TaskId.TaskTriggerBuilder;

public interface Task<T extends Serializable> extends SpringBeanTask<T> {
    TaskId<T> getId();

    default TaskTriggerBuilder<T> newTrigger() {
        return getId().newTrigger();
    }
}
