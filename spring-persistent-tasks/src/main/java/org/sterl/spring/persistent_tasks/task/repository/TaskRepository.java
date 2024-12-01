package org.sterl.spring.persistent_tasks.task.repository;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.sterl.spring.persistent_tasks.api.Task;
import org.sterl.spring.persistent_tasks.api.TaskId;

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
    
    @SuppressWarnings("unchecked")
    public <T extends Serializable> Task<T> remove(Task<T> task) {
        if (task == null) return null;
        return (Task<T>)tasks.remove(task.getId());
    }

    public <T extends Serializable> TaskId<T> addTask(Task<T> task) {
        if (contains(task.getId())) {
            throw new IllegalStateException("The task id " + task.getId() + " is already used!");
        }
        log.info("Adding task={} to={}", task.getId(), task.getClass());
        this.tasks.put(task.getId(), task);
        return task.getId();
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public <T extends Serializable> Optional<Task<T>> get(@NonNull TaskId<T> taskId) {
        assert taskId != null;
        return Optional.ofNullable((Task<T>)tasks.get(taskId));
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
        return new HashSet<>(tasks.keySet());
    }
}
