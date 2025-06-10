package org.sterl.spring.persistent_tasks.trigger;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerRequest;
import org.sterl.spring.persistent_tasks.api.TriggerSearch;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.shared.stereotype.TransactionalService;
import org.sterl.spring.persistent_tasks.task.TaskService;
import org.sterl.spring.persistent_tasks.trigger.component.EditTriggerComponent;
import org.sterl.spring.persistent_tasks.trigger.component.FailTriggerComponent;
import org.sterl.spring.persistent_tasks.trigger.component.LockNextTriggerComponent;
import org.sterl.spring.persistent_tasks.trigger.component.ReadTriggerComponent;
import org.sterl.spring.persistent_tasks.trigger.component.RunTriggerComponent;
import org.sterl.spring.persistent_tasks.trigger.component.StateSerializer;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@TransactionalService
@RequiredArgsConstructor
@Slf4j
public class TriggerService {

    private final TaskService taskService;
    @Getter
    private final StateSerializer stateSerializer = new StateSerializer();
    private final RunTriggerComponent runTrigger;
    private final ReadTriggerComponent readTrigger;
    private final EditTriggerComponent editTrigger;
    private final FailTriggerComponent failTrigger;
    private final LockNextTriggerComponent lockNextTrigger;

    /**
     * Executes the given trigger directly in the current thread
     * and handle any errors etc.
     *
     * @param trigger the {@link TriggerEntity} to run
     * @return the reference to the found an executed {@link TriggerEntity}
     */
    @Transactional(propagation = Propagation.NEVER)
    public Optional<TriggerEntity> run(@Nullable TriggerEntity trigger) {
        return runTrigger.execute(trigger);
    }

    /**
     * The main purpose of this method is to simplify testing and run just one trigger.
     * 
     * @param triggerKey the key to trigger which should be executed
     * @param runningOn just any string, could be test for testing, usually the scheduler name
     * @return the reference to the found an executed {@link TriggerEntity}
     */
    @Transactional(propagation = Propagation.NEVER)
    public Optional<TriggerEntity> run(TriggerKey triggerKey, String runningOn) {
        final TriggerEntity trigger = lockNextTrigger.lock(triggerKey, runningOn);
        if (trigger == null) {
            return Optional.empty();
        }
        return run(trigger);
    }
    
    @Transactional(propagation = Propagation.NEVER)
    public Optional<TriggerEntity> run(@Nullable TriggerRequest<?> request, String runningOn) {
        var trigger = queue(request);
        trigger = lockNextTrigger.lock(trigger.getKey(), runningOn);
        return run(trigger);
    }

    public TriggerEntity markTriggersAsRunning(TriggerEntity trigger, String runOn) {
        return trigger.runOn(runOn);
    }
    
    public int markTriggersAsRunning(Collection<TriggerKey> keys, String runOn) {
        return this.editTrigger.markTriggersAsRunning(keys, runOn);
    }

    public TriggerEntity lockNextTrigger(String runOn) {
        final List<TriggerEntity> r = lockNextTrigger.loadNext(runOn, 1, OffsetDateTime.now());
        return r.isEmpty() ? null : r.get(0);
    }

    public List<TriggerEntity> lockNextTrigger(String runOn, int count, OffsetDateTime timeDueAt) {
        return lockNextTrigger.loadNext(runOn, count, timeDueAt);
    }

    public Optional<TriggerEntity> get(TriggerKey triggerKey) {
        return readTrigger.get(triggerKey);
    }

    @Transactional(readOnly = true , timeout = 10)
    public Page<TriggerEntity> searchTriggers(@Nullable TriggerSearch search, Pageable page) {
        return this.readTrigger.searchTriggers(search, page);
    }
    
    @Transactional(readOnly = true , timeout = 10)
    public Page<TriggerEntity> findAllTriggers(TaskId<?> task, Pageable page) {
        return this.readTrigger.listTriggers(task, page);
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

    /**
     * Adds or updates an existing trigger based on its {@link TriggerKey}
     * 
     * @param <T> the state type
     * @param tigger the {@link TriggerRequest} to save
     * @return the saved {@link TriggerEntity}
     * @throws IllegalStateException if the trigger already exists and is {@link TriggerStatus#RUNNING}
     */
    public <T extends Serializable> TriggerEntity queue(TriggerRequest<T> tigger) {
        taskService.assertIsKnown(tigger.taskId());
        return editTrigger.addTrigger(tigger);
    }
    
    /**
     * Will resume any found
     * @param trigger
     * @return
     */
    public Page<TriggerEntity> resume(TriggerRequest<?> trigger) {
        if (trigger.key().getId() == null && trigger.correlationId() == null) {
            throw new IllegalArgumentException("Trigger ID or correlationId required to resume: " + trigger);
        }
        taskService.assertIsKnown(trigger.taskId());
        return editTrigger.resume(trigger);
    }

    /**
     * If you changed your mind, cancel the persistentTask
     */
    public Optional<TriggerEntity> cancel(TriggerKey key) {
        return editTrigger.cancelTask(key, null);
    }

    public List<TriggerEntity> cancel(Collection<TriggerKey> key) {
        return key.stream().map(t -> editTrigger.cancelTask(t, null))
           .filter(Optional::isPresent)
           .map(Optional::get)
           .toList();
    }

    /**
     * Counts the trigger using the name only from the {@link TaskId}
     *
     * @param taskId to get the {@link TaskId#name()}
     * @return the amount of stored tasks
     */
    @Transactional(timeout = 5, readOnly = true)
    public long countTriggers(@Nullable TaskId<?> taskId) {
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
    public List<TriggerEntity> rescheduleAbandoned(OffsetDateTime timeout) {
        final List<TriggerEntity> result = readTrigger.findTriggersLastPingAfter(
                timeout);
        final var e = new IllegalStateException("Trigger abandoned - timeout: " + timeout);
        result.forEach(t -> {
            var task = taskService.get(t.newTaskId());
            var state = stateSerializer.deserializeOrNull(t.getData().getState());
            failTrigger.execute(task.orElse(null), t, state, e);
        });
        log.debug("rescheduled {} triggers", result.size());
        return result;
    }
    
    public List<TriggerEntity> expireTimeoutTriggers() {
        return readTrigger.findTriggersTimeoutOut(20)
                          .stream()
                          .map(editTrigger::expireTrigger)
                          .toList();
    }

    public long countTriggers() {
        return readTrigger.countByStatus(null);
    }

    public Optional<TriggerEntity> updateRunAt(TriggerKey key, OffsetDateTime time) {
        return readTrigger.get(key).map(t -> {
            if (t.getData().getStatus() == TriggerStatus.RUNNING) {
                throw new IllegalStateException("Cannot update status of " + key
                        + " as the current status is: " + t.getData().getStatus());
            }
            t.getData().setRunAt(time);
            return t;
        });
    }
}
