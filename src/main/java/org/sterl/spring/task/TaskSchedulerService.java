package org.sterl.spring.task;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.sterl.spring.task.api.ClosureTask;
import org.sterl.spring.task.api.SimpleTask;
import org.sterl.spring.task.api.Task;
import org.sterl.spring.task.api.TaskId;
import org.sterl.spring.task.api.TaskTrigger;
import org.sterl.spring.task.component.EditTaskInstanceComponent;
import org.sterl.spring.task.component.LockNextTriggerComponent;
import org.sterl.spring.task.component.TransactionalTaskExecutorComponent;
import org.sterl.spring.task.model.TaskStatus;
import org.sterl.spring.task.model.TaskTriggerEntity;
import org.sterl.spring.task.model.TaskTriggerId;
import org.sterl.spring.task.repository.TaskRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class TaskSchedulerService {

    private final String name;
    private final LockNextTriggerComponent lockNextTriggerComponent;
    private final EditTaskInstanceComponent editTaskInstanceComponent;
    private final TaskRepository taskRepository;
    private final TransactionalTaskExecutorComponent taskExecutor;

    /**
     * Checks if any job is still running or waiting for it's execution.
     */
    public boolean hasTriggers() {
        if (taskExecutor.getRunningTasks() > 0) return true;
        return editTaskInstanceComponent.hasTriggers();
    }
    
    public Optional<TaskTriggerEntity> get(TaskTriggerId id) {
        return editTaskInstanceComponent.get(id);
    }

    /**
     * A way to manually register a task, usually not needed as spring beans will be added anyway.
     */
    public <T extends Serializable> TaskId<T> register(String name, SimpleTask<T> task) {
        ClosureTask<T> t = new ClosureTask<>(name, task);
        return register(t);
    }
    /**
     * A way to manually register a task, usually not needed as spring beans will be added anyway.
     */
    public <T extends Serializable> TaskId<T> register(Task<T> task) {
        return taskRepository.addTask(task);
    }

    /**
     * Just triggers the given task to be executed using <code>null</code> as state.
     */
    public <T extends Serializable> TaskTriggerId trigger(TaskId<T> taskId) {
        return trigger(taskId, null);
    }

    public <T extends Serializable> TaskTriggerId trigger(TaskId<T> taskId, T state) {
        return trigger(UUID.randomUUID().toString(), taskId, state);
    }
    
    public <T extends Serializable> TaskTriggerId trigger(String id, TaskId<T> taskId, T state) {
        return trigger(id, taskId, state, OffsetDateTime.now());
    }
    
    public <T extends Serializable> TaskTriggerId trigger(String id, TaskId<T> taskId, T state, OffsetDateTime when) {
        return trigger(taskId.newTrigger()
                .id(id)
                .state(state)
                .when(when)
                .build());
    }

    public <T extends Serializable> TaskTriggerId trigger(TaskTrigger<T> tigger) {
        taskRepository.assertIsKnown(tigger.taskId());
        return editTaskInstanceComponent.addTrigger(tigger);
    }
    
    public <T extends Serializable> List<TaskTriggerId>  triggerAll(Collection<TaskTrigger<T>> triggers) {
        triggers.forEach(t -> taskRepository.assertIsKnown(t.taskId()));
        return editTaskInstanceComponent.addTriggers(triggers);
    }

    //@Scheduled(initialDelay = 10, fixedDelay = 5)
    /**
     * Simply triggers the next task which is now due to be executed
     */
    public Future<?> triggerNexTask() {
        return triggerNexTask(OffsetDateTime.now());
    }
    
    /**
     * Like {@link #triggerNexTask()} but allows to set the time e.g. to the future to trigger
     * tasks which wouldn't be triggered now.
     */
    public Future<?> triggerNexTask(OffsetDateTime timeDue) {
        if (taskExecutor.getFreeThreads() > 0) {
            final var trigger = lockNextTriggerComponent.loadNext(name, timeDue);
            if (trigger == null) return CompletableFuture.completedFuture(null);
            return taskExecutor.execute(trigger);
        } else {
            log.debug("triggerNexTask={} skipped as no free threads are available.", timeDue);
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * If you changed your mind, cancel the task
     */
    public void cancel(TaskTriggerId id) {
        editTaskInstanceComponent.completeTaskWithStatus(id, TaskStatus.CANCELED, null);
    }
}
