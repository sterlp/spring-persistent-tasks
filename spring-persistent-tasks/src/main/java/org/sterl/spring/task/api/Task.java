package org.sterl.spring.task.api;

import java.io.Serializable;

import org.sterl.spring.task.api.TaskId.TaskTriggerBuilder;

public interface Task<T extends Serializable> extends SpringBeanTask<T> {
    TaskId<T> getId();

    default TaskTriggerBuilder<T> newTrigger() {
        return getId().newTrigger();
    }
}
