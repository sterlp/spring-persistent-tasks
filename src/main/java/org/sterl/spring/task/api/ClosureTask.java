package org.sterl.spring.task.api;

import java.io.Serializable;
import java.util.function.Function;

public class ClosureTask<T extends Serializable> implements Task<T> {

    private final TaskId<T> id;
    private final Function<T, TaskResult> fun;
    private RetryStrategy retryStrategy = RetryStrategy.TRY_THREE_TIMES;

    public ClosureTask(String name, Function<T, TaskResult> fun) {
        super();
        this.id = new TaskId<>(name);
        this.fun = fun;
    }

    public ClosureTask(String name, SimpleTask<T> consumer) {
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

    @Override
    public TaskId<T> getId() {
        return id;
    }
    
    @Override
    public RetryStrategy retryStrategy() {
        return retryStrategy;
    }
}
