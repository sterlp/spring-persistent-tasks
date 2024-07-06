package org.sterl.spring.task.model;

import java.io.Serializable;
import java.util.function.Function;

import org.sterl.spring.task.api.RetryStrategy;
import org.sterl.spring.task.api.SimpleTask;
import org.sterl.spring.task.api.Task;
import org.sterl.spring.task.api.TaskId;
import org.sterl.spring.task.api.TaskResult;

import lombok.Getter;

public class RegisteredTask<T extends Serializable> implements Task<T> {

    @Getter
    private final TaskId<T> id;
    private final Function<T, TaskResult> fun;
    @Getter
    private RetryStrategy retryStrategy = RetryStrategy.TRY_THREE_TIMES;

    public RegisteredTask(String name, Function<T, TaskResult> fun) {
        super();
        this.id = new TaskId<>(name);
        this.fun = fun;
    }

    public RegisteredTask(String name, SimpleTask<T> consumer) {
        this(name, s -> {
            consumer.accept(s);
            return TaskResult.DONE;
        });
        retryStrategy = consumer.retryStrategy();
    }

    @Override
    public TaskResult execute(T state) {
        return fun.apply(state);
    }
}
