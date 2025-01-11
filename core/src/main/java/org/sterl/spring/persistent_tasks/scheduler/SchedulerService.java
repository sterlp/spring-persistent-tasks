package org.sterl.spring.persistent_tasks.scheduler;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.api.AddTriggerRequest;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.scheduler.component.EditSchedulerStatusComponent;
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
    private final TransactionTemplate trx;
    private final Map<Long, TriggerEntity> shouldRun = new ConcurrentHashMap<>();

    @PostConstruct
    public void start() {
        taskExecutor.start();
        final var s = editSchedulerStatus.checkinToRegistry(name);
        log.info("Started {} on {} threads.", s, taskExecutor.getMaxThreads());
    }

    public void setMaxThreads(int value) {
        this.taskExecutor.setMaxThreads(value);
    }

    @PreDestroy
    public void stop() {
        taskExecutor.close();
        editSchedulerStatus.offline(name);
        log.info("Stopped {}", name);
    }

    public void shutdownNow() {
        var running = taskExecutor.getRunningTasks();
        taskExecutor.shutdownNow();
        log.info("Force stop {} with {} running tasks", name, running);
        editSchedulerStatus.offline(name);
    }

    @Transactional
    public SchedulerEntity pingRegistry() {
        var result = editSchedulerStatus.checkinToRegistry(name);
        result.setRunnungTasks(taskExecutor.getRunningTasks());
        result.setTasksSlotCount(taskExecutor.getMaxThreads());
        log.debug("Ping {}", result);
        return result;
    }

    public SchedulerEntity getScheduler() {
        var result = editSchedulerStatus.get(name);
        return result;
    }

    public Optional<SchedulerEntity> findStatus(String name) {
        return editSchedulerStatus.find(name);
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
    @NonNull
    public List<Future<TriggerKey>> triggerNextTasks(OffsetDateTime timeDue) {
        if (taskExecutor.getFreeThreads() > 0) {
            final var result = trx.execute(t -> {
                var triggers = triggerService.lockNextTrigger(name, taskExecutor.getFreeThreads(), timeDue);
                pingRegistry().addRunning(triggers.size());
                return triggers;
            });

            return taskExecutor.submit(result);
        } else {
            pingRegistry();
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
        var trigger = triggerService.queue(triggerRequest);

        if (!trigger.shouldRunInFuture()) {
            if (taskExecutor.getFreeThreads() > 0) {
                trigger = triggerService.markTriggersAsRunning(trigger, name);
                pingRegistry().addRunning(1);
                shouldRun.put(trigger.getId(), trigger);
            } else {
                log.debug("Currently not enough free thread available {} of {} in use. PersistentTask {} queued.",
                        taskExecutor.getFreeThreads(), taskExecutor.getMaxThreads(), trigger.getKey());
            }
        }
        // we will listen for the commit event to execute this trigger ...
        return trigger.getKey();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void checkIfTrigerIsRunning(TriggerAddedEvent addedTrigger) {
        final var toRun = shouldRun.remove(addedTrigger.id());
        if (toRun != null) {
            log.debug("New triger added for imidiate execution {}", addedTrigger.key());
            taskExecutor.submit(toRun);
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
        log.debug("({}) - {} trigger(s) are running on {} schedulers", running, runningKeys, schedulers);
        return triggerService.rescheduleAbandonedTasks(timeout);
    }
}
