package org.sterl.spring.persistent_tasks.task.repository;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.task.PersistentTask;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TaskRepository {
    private final Map<TaskId<? extends Serializable>, PersistentTask<? extends Serializable>> persistentTasks = new ConcurrentHashMap<>();

    public <T extends Serializable> PersistentTask<? extends Serializable> remove(TaskId<T> taskId) {
        if (taskId == null) {
            return null;
        }
        return persistentTasks.remove(taskId);
    }

    public <T extends Serializable> TaskId<T> addTask(@NonNull TaskId<T> taskId, PersistentTask<T> task) {
        if (contains(taskId)) {
            throw new IllegalStateException("The " + taskId + " is already used!");
        }
        log.info("Adding {} to={}", taskId, task.getClass());
        this.persistentTasks.put(taskId, task);
        return taskId;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public <T extends Serializable> Optional<PersistentTask<T>> get(@NonNull TaskId<T> taskId) {
        assert taskId != null;
        return Optional.ofNullable((PersistentTask<T>)persistentTasks.get(taskId));
    }

    /**
     * Removes all persistentTasks, should only be used for testing!
     */
    public void clear() {
        log.warn("*** All persistentTasks {} will be removed now! ***", persistentTasks.size());
        persistentTasks.clear();
    }

    public boolean contains(String name) {
        return contains(new TaskId<>(name));
    }

    public boolean contains(TaskId<? extends Serializable> id) {
        return persistentTasks.containsKey(id);
    }

    public Set<TaskId<? extends Serializable>> all() {
        return new HashSet<>(persistentTasks.keySet());
    }
}
