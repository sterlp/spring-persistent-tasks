package org.sterl.spring.persistent_tasks.task;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.api.SpringBeanTask;
import org.sterl.spring.persistent_tasks.api.Task;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.task.model.RegisteredTask;
import org.sterl.spring.persistent_tasks.task.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    
    @Transactional(readOnly = true)
    public Set<TaskId<? extends Serializable>> findAllTaskIds() {
        return this.taskRepository.all();
    }
    
    
    public <T extends Serializable> Optional<Task<T>> get(TaskId<T> id) {
        return taskRepository.get(id);
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
    public <T extends Serializable> Task<T> assertIsKnown(@NonNull TaskId<T> id) {
        final var task = taskRepository.get(id);
        if (task.isEmpty()) {
            throw new IllegalStateException("Task with ID " + id 
                    + " is unknown. Known tasks: " + taskRepository.all());
        }
        return task.get();
    }
    
    /**
     * A way to manually register a task, usually better to use {@link SpringBeanTask}.
     */
    public <T extends Serializable> TaskId<T> register(String name, Consumer<T> task) {
        RegisteredTask<T> t = new RegisteredTask<>(name, task);
        return register(t);
    }
    /**
     * A way to manually register a task, usually not needed as spring beans will be added automatically.
     */
    public <T extends Serializable> TaskId<T> register(String name, SpringBeanTask<T> task) {
        RegisteredTask<T> t = new RegisteredTask<>(name, task);
        return register(t);
    }
    /**
     * A way to manually register a task, usually not needed as spring beans will be added anyway.
     */
    public <T extends Serializable> TaskId<T> register(RegisteredTask<T> task) {
        return taskRepository.addTask(task);
    }
    
    /**
     * A way to manually register a task, usually not needed as spring beans will be added anyway.
     */
    public <T extends Serializable> TaskId<T> repalce(RegisteredTask<T> task) {
        taskRepository.remove(task);
        return register(task);
    }
    /**
     * A way to manually register a task, usually not needed as spring beans will be added automatically.
     */
    public <T extends Serializable> TaskId<T> replace(String name, SpringBeanTask<T> task) {
        RegisteredTask<T> t = new RegisteredTask<>(name, task);
        return repalce(t);
    }
}
