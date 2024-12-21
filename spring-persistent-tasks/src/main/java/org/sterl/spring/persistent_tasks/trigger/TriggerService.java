package org.sterl.spring.persistent_tasks.trigger;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.api.AddTriggerRequest;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.event.TriggerTaskCommand;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;
import org.sterl.spring.persistent_tasks.shared.stereotype.TransactionalService;
import org.sterl.spring.persistent_tasks.task.TaskService;
import org.sterl.spring.persistent_tasks.trigger.component.EditTriggerComponent;
import org.sterl.spring.persistent_tasks.trigger.component.LockNextTriggerComponent;
import org.sterl.spring.persistent_tasks.trigger.component.ReadTriggerComponent;
import org.sterl.spring.persistent_tasks.trigger.component.RunTriggerComponent;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import lombok.RequiredArgsConstructor;

@TransactionalService
@RequiredArgsConstructor
public class TriggerService {

    private static final String MANUAL_TAG = "manual";
    private final TaskService taskService;
    private final RunTriggerComponent runTrigger;
    private final ReadTriggerComponent readTrigger;
    private final EditTriggerComponent editTrigger;
    private final LockNextTriggerComponent lockNextTrigger;

    /**
     * Executes the given trigger directly in the current thread
     * and handle any errors etc.
     *
     * @param trigger the {@link TriggerEntity} to run
     * @return the reference to the updated {@link TriggerEntity}
     */
    @Transactional(propagation = Propagation.NEVER)
    public Optional<TriggerEntity> run(@Nullable TriggerEntity trigger) {
        return runTrigger.execute(trigger);
    }

    @Transactional(propagation = Propagation.NEVER)
    public Optional<TriggerEntity> run(TriggerKey triggerKey) {
        final TriggerEntity trigger = lockNextTrigger.lock(triggerKey, MANUAL_TAG);
        if (trigger == null) {
            return Optional.empty();
        }
        return run(trigger);
    }

    public TriggerEntity markTriggerInExecution(TriggerEntity trigger, String runOn) {
        return trigger.runOn(runOn);
    }

    public TriggerEntity lockNextTrigger() {
        final List<TriggerEntity> r = lockNextTrigger.loadNext(MANUAL_TAG, 1, OffsetDateTime.now());
        return r.isEmpty() ? null : r.get(0);
    }

    public List<TriggerEntity> lockNextTrigger(String runOn, int count, OffsetDateTime timeDueAt) {
        return lockNextTrigger.loadNext(runOn, count, timeDueAt);
    }

    public Optional<TriggerEntity> get(TriggerKey triggerKey) {
        return readTrigger.get(triggerKey);
    }

    @Transactional(readOnly = true , timeout = 10)
    public Page<TriggerEntity> findAllTriggers(TaskId<?> task, Pageable page) {
        return this.editTrigger.listTriggers(task, page);
    }

    public void deleteAll() {
        this.editTrigger.deleteAll();
    }

    /**
     * Checks if any job is still running or waiting for it's execution.
     */
    @Transactional(readOnly = true, timeout = 5)
    public boolean hasPendingTriggers() {
        return readTrigger.hasPendingTriggers();
    }

    @EventListener
    public void queue(TriggerTaskCommand<? extends Serializable> event) {
        queueAll(event.triggers());
    }

    public <T extends Serializable> TriggerEntity queue(AddTriggerRequest<T> tigger) {
        taskService.assertIsKnown(tigger.taskId());
        return editTrigger.addTrigger(tigger);
    }

    @NonNull
    public <T extends Serializable> List<TriggerEntity> queueAll(Collection<AddTriggerRequest<T>> triggers) {
        triggers.forEach(t -> taskService.assertIsKnown(t.taskId()));
        return editTrigger.addTriggers(triggers);
    }

    /**
     * If you changed your mind, cancel the task
     */
    public Optional<TriggerEntity> cancel(TriggerKey key) {
        return editTrigger.cancelTask(key);
    }

    /**
     * Counts the trigger using the name only from the {@link TaskId}
     *
     * @param taskId to get the {@link TaskId#name()}
     * @return the amount of stored tasks
     */
    @Transactional(timeout = 5, readOnly = true)
    public long countTriggers(@Nullable TaskId<String> taskId) {
        if (taskId == null || taskId.name() == null) {
            return 0L;
        }
        return this.readTrigger.countByTaskName(taskId.name());
    }
    
    public long countTriggers(@Nullable TriggerStatus status) {
        return readTrigger.countByStatus(status);
    }

    /**
     * Marks any tasks which are not on the given executors/schedulers abandoned for .
     *
     * Retry will be triggered based on the set strategy.
     */
    public List<TriggerEntity> rescheduleAbandonedTasks(Set<String> onlineRunner) {
        onlineRunner.add(MANUAL_TAG);
        final List<TriggerEntity> result = readTrigger.findNotRunningOn(onlineRunner);
        result.forEach(t -> {
            t.setRunningOn(null);
            t.getData().setStatus(TriggerStatus.NEW);
            t.getData().setExceptionName("Abandoned tasks");
        });
        return result;
    }

    public long countTriggers() {
        return readTrigger.countByStatus(null);
    }
}
