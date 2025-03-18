package org.sterl.spring.persistent_tasks.scheduler;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

import org.slf4j.event.Level;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.api.AddTriggerRequest;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.scheduler.component.EditSchedulerStatusComponent;
import org.sterl.spring.persistent_tasks.scheduler.component.RunOrQueueComponent;
import org.sterl.spring.persistent_tasks.scheduler.component.TaskExecutorComponent;
import org.sterl.spring.persistent_tasks.scheduler.entity.SchedulerEntity;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerAddedEvent;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Use this service if direct access to the Scheduler is required. <br>
 * <b>Note:</b> This Service is optional, as it could be disabled if no
 * background tasks should be execute on this note. As so the
 * {@link TriggerService} should be preferred to queue tasks.
 */
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    @Getter
    private final String name;
    private final TriggerService triggerService;
    
    private final TaskExecutorComponent taskExecutor;
    private final EditSchedulerStatusComponent editSchedulerStatus;

    private final RunOrQueueComponent runOrQueue;

    private final TransactionTemplate trx;

    @PostConstruct
    public void start() {
        taskExecutor.start();
        editSchedulerStatus.checkinToRegistry(name, 0, taskExecutor.getMaxThreads());
    }

    public void setMaxThreads(int value) {
        this.taskExecutor.setMaxThreads(value);
    }

    @PreDestroy
    public void stop() {
        taskExecutor.close();
        editSchedulerStatus.offline(name);
        runOrQueue.clear();
    }

    public void shutdownNow() {
        var running = taskExecutor.getRunningTasks();
        taskExecutor.shutdownNow();
        log.info("Force stop {} with {} running tasks", name, running);
        editSchedulerStatus.offline(name);
    }

    public SchedulerEntity getScheduler() {
        return editSchedulerStatus.checkinToRegistry(name, 
                taskExecutor.countRunning(), taskExecutor.getMaxThreads());
    }

    public Optional<SchedulerEntity> findStatus(String name) {
        if (name == null) return Optional.empty();
        else if (name.equals(this.name)) return Optional.of(getScheduler());
        else return editSchedulerStatus.find(name);
    }

    /**
     * Simply triggers the next persistentTask which is now due to be executed
     */
    @NonNull
    public List<Future<TriggerKey>> triggerNextTasks() {
        return triggerNextTasks(OffsetDateTime.now());
    }

    /**
     * Like {@link #triggerNextTasks()} but allows to set the time e.g. to the
     * future to trigger tasks which wouldn't be triggered now.
     * <p>
     * This method should not be called in a transaction!
     * </p>
     */
    @Transactional(propagation = Propagation.NEVER)
    @NonNull
    public List<Future<TriggerKey>> triggerNextTasks(OffsetDateTime timeDue) {
        if (taskExecutor.getFreeThreads() > 0) {
            final var result = trx.execute(t -> {
                var triggers = triggerService.lockNextTrigger(name, taskExecutor.getFreeThreads(), timeDue);
                editSchedulerStatus.checkinToRegistry(name, 
                        taskExecutor.countRunning() + triggers.size(), taskExecutor.getMaxThreads());
                return triggers;
            });

            return taskExecutor.submit(result);
        } else {
            log.info("No free threads {}/{} right now to run jobs due for: {}",
                    taskExecutor.getFreeThreads(),
                    taskExecutor.getMaxThreads(),
                    timeDue);
            editSchedulerStatus.checkinToRegistry(name, taskExecutor.countRunning(), taskExecutor.getMaxThreads());
            return Collections.emptyList();
        }
    }

    /**
     * Runs the given trigger if a free threads are available and the runAt time is
     * not in the future.
     * 
     * @return the reference to the {@link Future} with the key, if no threads are
     *         available it is resolved
     */
    @Transactional(timeout = 10)
    public <T extends Serializable> TriggerKey runOrQueue(AddTriggerRequest<T> triggerRequest) {
        return runOrQueue.execute(triggerRequest);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void checkIfTriggerShouldRun(TriggerAddedEvent addedTrigger) {
        if (runOrQueue.checkIfTrigerShouldRun(addedTrigger.id())) {
            editSchedulerStatus.checkinToRegistry(name, taskExecutor.countRunning(), taskExecutor.getMaxThreads());
        }
    }

    public SchedulerEntity getStatus() {
        return editSchedulerStatus.get(name);
    }

    @Transactional
    public List<TriggerEntity> rescheduleAbandonedTasks(OffsetDateTime timeout) {
        var schedulers = editSchedulerStatus.findOnlineSchedulers(timeout);

        final List<TriggerKey> runningKeys = this.taskExecutor.getRunningTriggers().stream().map(TriggerEntity::getKey)
                .toList();

        int running = triggerService.markTriggersAsRunning(runningKeys, name);
        log.atLevel(running > 0 ? Level.INFO : Level.DEBUG).log("({}) - {} trigger(s) are running on {} schedulers", 
                running, runningKeys, schedulers);
        return triggerService.rescheduleAbandonedTasks(timeout);
    }

    public List<SchedulerEntity> listAll() {
        return editSchedulerStatus.listAll();
    }
    public Collection<Future<TriggerKey>> getRunning() {
        return taskExecutor.getRunningTasks();
    }
    public List<TriggerEntity> getRunningTriggers() {
        return taskExecutor.getRunningTriggers();
    }
    public boolean hasRunningTriggers() {
        return taskExecutor.countRunning() > 0 || runOrQueue.hasWaitingTriggers();
    }

}
