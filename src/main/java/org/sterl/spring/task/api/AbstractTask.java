package org.sterl.spring.task.api;

import java.io.Serializable;

public abstract class AbstractTask<T extends Serializable> implements Task<T> {
    protected final TaskId<T> id;
    
    protected AbstractTask() {
        this.id = new TaskId<T>(this.getClass().getName());
    }
    protected AbstractTask(String name) {
        this.id = new TaskId<T>(name);
    }
    protected AbstractTask(TaskId<T> id) {
        this.id = id;
    }
    @Override
    public TaskId<T> getId() {
        return id;
    }
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(id=" + id + ")";
    }
}