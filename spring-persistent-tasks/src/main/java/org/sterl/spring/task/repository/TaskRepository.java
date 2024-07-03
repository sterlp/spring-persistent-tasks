package org.sterl.spring.task.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

        if (old != null) throw new IllegalStateException("The task id " 
                + task.getId() + " is already used!");
        return task.getId();
    }

    public <T extends Serializable> Task<T> assertIsKnown(TaskId<T> id) {
        Task<T> r = (Task<T>)tasks.get(id);
        if (r == null) throw new IllegalStateException("Task with ID " + id 
                + " is unknown. Known tasks: " + tasks.keySet());
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
        return tasks.containsKey(new TaskId<>(name));
    }

    public Set<TaskId<? extends Serializable>> all() {
        return tasks.keySet();
    }
}
