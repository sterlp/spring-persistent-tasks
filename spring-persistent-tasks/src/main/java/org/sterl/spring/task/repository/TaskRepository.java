package org.sterl.spring.task.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.sterl.spring.task.api.Task;
import org.sterl.spring.task.api.TaskId;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TaskRepository {
    private final Map<TaskId<? extends Serializable>, Task<?>> tasks = new ConcurrentHashMap<>();

    public TaskRepository(List<Task<?>> tasks) {
        super();
        for (Task<?> task : tasks) {
            addTask(task);
        }
    }

    public <T extends Serializable> TaskId<T> addTask(Task<T> task) {
        var old = this.tasks.put(task.getId(), task);
        log.info("Adding task={} to={}", task.getId(), task.getClass());

        if (old != null) {
            throw new IllegalStateException("The task id " + task.getId() + " is already used!");
        }
        return task.getId();
    }

    /**
     * Check if the {@link Task} is known or not.
     * 
     * @param <T> the state type
     * @param id the {@link TaskId} of the {@link Task}
     * @throws IllegalStateException if the id is unknown
     * @return the {@link Task} registered to the given id
     */
    @NonNull
    public <T extends Serializable> Task<T> assertIsKnown(TaskId<T> id) {
        Task<T> r = (Task<T>)tasks.get(id);
        if (r == null) {
            throw new IllegalStateException("Task with ID " + id 
                    + " is unknown. Known tasks: " + tasks.keySet());
        }
        return r;
    }

    public <T extends Serializable> Task<T> get(TaskId<T> taskId) {
        return assertIsKnown(taskId);
    }

    /**
     * Removes all tasks, should only be used for testing!
     */
    public void clear() {
        log.warn("*** All tasks {} will be removed now! ***", tasks.size());
        tasks.clear();
    }

    public boolean contains(String name) {
        return contains(new TaskId<>(name));
    }
    
    public boolean contains(TaskId<?> id) {
        return tasks.containsKey(id);
    }

    public Set<TaskId<? extends Serializable>> all() {
        return tasks.keySet();
    }
}
