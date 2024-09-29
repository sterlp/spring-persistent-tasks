package org.sterl.spring.task;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.task.api.SpringBeanTask;
import org.sterl.spring.task.api.TaskId;
import org.sterl.spring.task.api.Trigger;
import org.sterl.spring.task.api.TriggerId;
import org.sterl.spring.task.api.event.TriggerTaskEvent;
import org.sterl.spring.task.component.EditSchedulerStatusComponent;
import org.sterl.spring.task.component.EditTaskTriggerComponent;
import org.sterl.spring.task.component.LockNextTriggerComponent;
import org.sterl.spring.task.component.ReadTriggerComponent;
import org.sterl.spring.task.component.TransactionalTaskExecutorComponent;
import org.sterl.spring.task.model.RegisteredTask;
import org.sterl.spring.task.model.TaskSchedulerEntity;
import org.sterl.spring.task.model.TaskSchedulerEntity.TaskSchedulerStatus;
import org.sterl.spring.task.model.TriggerEntity;
import org.sterl.spring.task.model.TriggerStatus;
import org.sterl.spring.task.repository.TaskRepository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class TaskSchedulerService {

    @Getter
    private final String name;
    private final ReadTriggerComponent readTriggerComponent;
    private final LockNextTriggerComponent lockNextTriggerComponent;
    private final EditTaskTriggerComponent editTaskTriggerComponent;
    private final EditSchedulerStatusComponent editSchedulerStatusComponent;
    private final TaskRepository taskRepository;
    private final TransactionalTaskExecutorComponent taskExecutor;
    private final TransactionTemplate trx;
    
    @PostConstruct
    public void start() {
        taskExecutor.start();
        final var s = pingRegistry();
        log.info("Started {}", s);
    }
    @PreDestroy
    public void stop() {
        editSchedulerStatusComponent.checkinToRegistry(name, TaskSchedulerStatus.OFFLINE);
        taskExecutor.stop();
    }
    
    public TaskSchedulerEntity pingRegistry() {
        return editSchedulerStatusComponent.checkinToRegistry(name, TaskSchedulerStatus.ONLINE);
    }
    
    /**
     * Checks if any job is still running or waiting for it's execution.
     */
    public boolean hasTriggers() {
        if (taskExecutor.getRunningTasks() > 0) return true;
        return editTaskTriggerComponent.hasTriggers();
    }
    
    public Optional<TriggerEntity> get(TriggerId id) {
        return readTriggerComponent.get(id);
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
     * Just triggers the given task to be executed using <code>null</code> as state.
     */
    public <T extends Serializable> TriggerId trigger(TaskId<T> taskId) {
        return trigger(taskId, null);
    }

    public <T extends Serializable> TriggerId trigger(TaskId<T> taskId, T state) {
        return trigger(UUID.randomUUID().toString(), taskId, state);
    }
    
    public <T extends Serializable> TriggerId trigger(String id, TaskId<T> taskId, T state) {
        return trigger(id, taskId, state, OffsetDateTime.now());
    }
    
    public <T extends Serializable> TriggerId trigger(String id, TaskId<T> taskId, T state, OffsetDateTime when) {
        return trigger(taskId.newTrigger()
                .id(id)
                .state(state)
                .when(when)
                .build());
    }
    
    @EventListener
    public void trigger(TriggerTaskEvent<Serializable> event) {
        triggerAll(event.triggers());
    }

    public <T extends Serializable> TriggerId trigger(Trigger<T> tigger) {
        taskRepository.assertIsKnown(tigger.taskId());
        return editTaskTriggerComponent.addTrigger(tigger);
    }

    @NonNull
    public <T extends Serializable> List<TriggerId>  triggerAll(Collection<Trigger<T>> triggers) {
        triggers.forEach(t -> taskRepository.assertIsKnown(t.taskId()));
        return editTaskTriggerComponent.addTriggers(triggers);
    }
    
    /**
     * Consumes triggers as long as we find any or the threads are all busy.
     */
    @NonNull
    public List<Future<?>> triggerTasksForAllThreads() {
        final var now = OffsetDateTime.now();
        final TaskSchedulerEntity runningOn = pingRegistry();
        if (taskExecutor.getFreeThreads() <= 0) return Collections.emptyList();

        List<Future<?>> result = new ArrayList<>();
        TriggerEntity task = null;
        do {
            task = lockNextTriggerComponent.loadNext(runningOn, now);
            if (task != null) {
                result.add(this.taskExecutor.execute(task));
            }
        } while(taskExecutor.getFreeThreads() > 0 && task != null);
        
        // only if we triggered anything
        if (!result.isEmpty()) pingRegistry();
        return result;
    }

    /**
     * Simply triggers the next task which is now due to be executed
     */
    @NonNull
    public Future<?> triggerNextTask() {
        return triggerNextTask(OffsetDateTime.now());
    }
    
    /**
     * Like {@link #triggerNextTask()} but allows to set the time e.g. to the future to trigger
     * tasks which wouldn't be triggered now.
     */
    @NonNull
    public Future<?> triggerNextTask(OffsetDateTime timeDue) {
        var trigger = trx.execute(t -> {
            final var runningOn = pingRegistry();
            TriggerEntity result;
            if (taskExecutor.getFreeThreads() > 0) {
                result = lockNextTriggerComponent.loadNext(runningOn, timeDue);
            } else {
                result = null;
                log.debug("triggerNexTask({}) skipped as no free thread is available.", timeDue);
            }
            if (result != null) {
                runningOn.setRunnungTasks(taskExecutor.getRunningTasks() + 1);
            }
            return result;
        });
        return this.taskExecutor.execute(trigger);
    }

    /**
     * If you changed your mind, cancel the task
     */
    public void cancel(TriggerId id) {
        editTaskTriggerComponent.completeTaskWithStatus(id, TriggerStatus.CANCELED, null);
    }
    
    @Transactional
    public List<TriggerEntity> rescheduleAbandonedTasks(Duration timeout) {
        final var offlineScheduler = editSchedulerStatusComponent.setSchedulersOffline(timeout);
        if (offlineScheduler > 0) log.info("Found {} offline scheduler(s).", offlineScheduler);
        final var tasks = editTaskTriggerComponent.findTasksInTimeout(timeout);
        tasks.forEach(t -> {
            t.setRunningOn(null);
            t.getData().setStatus(TriggerStatus.NEW);
            t.getData().setExceptionName("Abandoned tasks");
        });
        log.info("Reschedule {} abandoned tasks.", tasks.size());
        return tasks;
        
    }
    public Set<TaskId<? extends Serializable>> findAllTaskIds() {
        return this.taskRepository.all();
    }
    public Page<TriggerEntity> findAllTriggers(Pageable page) {
        return this.editTaskTriggerComponent.listTriggers(page);
    }
    public void deleteAllTriggers() {
        this.editTaskTriggerComponent.deleteAll();
    }
    /**
     * Counts the trigger using the name only from the {@link TaskId}
     * 
     * @param taskId to get the {@link TaskId#name()}
     * @return the amount of stored tasks
     */
    public int countTriggers(@Nullable TaskId<String> taskId) {
        if (taskId == null || taskId.name() == null) return 0;
        return this.readTriggerComponent.countByName(taskId.name());
    }
    /**
     * Counts the stored triggers by their status including the history.
     * 
     * @param status the status to count
     * @return the found amount or <code>0</code> if the given status is <code>null</code>
     */
    public int countTriggers(TriggerStatus status) {
        if (status == null) return 0;
        return readTriggerComponent.countByStatus(status);
    }
}
