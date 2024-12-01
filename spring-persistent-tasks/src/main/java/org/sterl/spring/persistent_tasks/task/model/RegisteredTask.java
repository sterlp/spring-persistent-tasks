package org.sterl.spring.persistent_tasks.task.model;

import java.io.Serializable;
import java.util.function.Consumer;

import org.sterl.spring.persistent_tasks.api.RetryStrategy;
import org.sterl.spring.persistent_tasks.api.SpringBeanTask;
import org.sterl.spring.persistent_tasks.api.Task;
import org.sterl.spring.persistent_tasks.api.TaskId;

import lombok.Getter;

public class RegisteredTask<T extends Serializable> implements Task<T> {

    @Getter
    private final TaskId<T> id;
    private final SpringBeanTask<T> fun;
    @Getter
    private RetryStrategy retryStrategy = RetryStrategy.TRY_THREE_TIMES;

    public RegisteredTask(String name, SpringBeanTask<T> fun) {
        super();
        this.id = new TaskId<>(name);
        this.fun = fun;
    }

    public RegisteredTask(String name, Consumer<T> consumer) {
        this(name, s -> {
            consumer.accept(s);
        });
    }

    @Override
    public void accept(T state) {
        this.fun.accept(state);
    }
    @Override
    public RetryStrategy retryStrategy() {
        return this.fun.retryStrategy();
    }
}
