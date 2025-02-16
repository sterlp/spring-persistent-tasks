package org.sterl.spring.persistent_tasks.task;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.task.ComplexPersistentTask;
import org.sterl.spring.persistent_tasks.api.task.PersistentTask;
import org.sterl.spring.persistent_tasks.api.task.PersistentTaskBase;
import org.sterl.spring.persistent_tasks.task.component.TaskTransactionComponent;
import org.sterl.spring.persistent_tasks.task.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskTransactionComponent taskTransactionComponent;
    private final TaskRepository taskRepository;

    public Set<TaskId<? extends Serializable>> findAllTaskIds() {
        return this.taskRepository.all();
    }

    public <T extends Serializable> Optional<PersistentTaskBase<T>> get(TaskId<T> id) {
        return taskRepository.get(id);
    }
    
    public <T extends Serializable> Optional<TransactionTemplate> getTransactionTemplate(
            PersistentTaskBase<T> task) {
        return taskTransactionComponent.getTransactionTemplate(task);
    }

    /**
     * Check if the {@link PersistentTaskBase} is known or not.
     *
     * @param <T> the state type
     * @param id the {@link TaskId} of the {@link PersistentTaskBase}
     * @throws IllegalStateException if the id is unknown
     * @return the {@link PersistentTaskBase} registered to the given id
     */
    @NonNull
    public <T extends Serializable> PersistentTaskBase<T> assertIsKnown(@NonNull TaskId<T> id) {
        final var task = taskRepository.get(id);
        if (task.isEmpty()) {
            throw new IllegalStateException("PersistentTaskBase with ID " + id
                    + " is unknown. Known tasks: " + taskRepository.all());
        }
        return task.get();
    }

    /**
     * A way to manually register a PersistentTaskBase, usually better to use {@link PersistentTaskBase}.
     */
    public TaskId<Serializable> register(String name, Consumer<Serializable> task) {
        return register(name, new PersistentTask<Serializable>() {
            @Override
            public void accept(Serializable state) {
                task.accept(state);
            }
        });
    }
    /**
     * A way to manually register a PersistentTaskBase, usually not needed as spring beans will be added automatically.
     */
    @SuppressWarnings("unchecked")
    public <T extends Serializable> TaskId<T> register(String name, PersistentTaskBase<T> task) {
        var id = (TaskId<T>)TaskId.of(name);
        return register(id, task);
    }
    /**
     * A way to manually register a PersistentTaskBase, usually not needed as spring beans will be added automatically.
     */
    public <T extends Serializable> TaskId<T> register(TaskId<T> id, PersistentTaskBase<T> task) {
        // init any transaction as needed
        taskTransactionComponent.getTransactionTemplate(task);
        return taskRepository.addTask(id, task);
    }
    /**
     * A way to manually register a PersistentTaskBase, usually not needed as spring beans will be added automatically.
     */
    @SuppressWarnings("unchecked")
    public <T extends Serializable> TaskId<T> replace(String name, PersistentTask<T> task) {
        var id = (TaskId<T>)TaskId.of(name);
        taskRepository.remove(id);
        return register(id, task);
    }
    
    /**
     * A way to manually register a PersistentTaskBase, usually not needed as spring beans will be added automatically.
     */
    @SuppressWarnings("unchecked")
    public <T extends Serializable, R extends Serializable> TaskId<T> replaceComplex(String name, 
            ComplexPersistentTask<T, R> task) {
        var id = (TaskId<T>)TaskId.of(name);
        taskRepository.remove(id);
        return register(id, task);
    }
}
