package org.sterl.spring.persistent_tasks.scheduler;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.AddTriggerRequest;
import org.sterl.spring.persistent_tasks.api.TriggerId;
import org.sterl.spring.persistent_tasks.scheduler.component.EditSchedulerStatusComponent;
import org.sterl.spring.persistent_tasks.scheduler.component.TaskExecutorComponent;
import org.sterl.spring.persistent_tasks.scheduler.entity.SchedulerEntity;
import org.sterl.spring.persistent_tasks.scheduler.entity.SchedulerEntity.TaskSchedulerStatus;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Use this service if direct access to the Scheduler is required.
 * <br>
 * <b>Note:</b> This Service is optional, as it could be disabled if no background
 * tasks should be execute on this note. As so the {@link TriggerService} should be
 * preferred to queue tasks.
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

    @PostConstruct
    public void start() {
        taskExecutor.start();
        final var s = editSchedulerStatus.checkinToRegistry(name, TaskSchedulerStatus.ONLINE);
        log.info("Started {} on {} threads.", s, taskExecutor.getMaxThreads());
    }

    @PreDestroy
    public void stop() {
        taskExecutor.close();
        var s = editSchedulerStatus.checkinToRegistry(name, TaskSchedulerStatus.OFFLINE);
        log.info("Stopped {}", s);
    }

    public void shutdownNow() {
        taskExecutor.shutdownNow();
        var s = editSchedulerStatus.checkinToRegistry(name, TaskSchedulerStatus.OFFLINE);
        log.info("Force stop {}", s);
    }

    public SchedulerEntity pingRegistry() {
        var result = editSchedulerStatus.checkinToRegistry(name, TaskSchedulerStatus.ONLINE);
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
     * Simply triggers the next task which is now due to be executed
     */
    @NonNull
    public List<Future<TriggerId>> triggerNextTasks() {
        return triggerNextTasks(OffsetDateTime.now());
    }

    /**
     * Like {@link #triggerNextTask()} but allows to set the time e.g. to the future to trigger
     * tasks which wouldn't be triggered now.
     */
    @NonNull
    public List<Future<TriggerId>> triggerNextTasks(OffsetDateTime timeDue) {
        var triggers = trx.execute(t -> {
            List<TriggerEntity> result;
            // in any case we say hello
            final var runningOn = pingRegistry();
            if (taskExecutor.getFreeThreads() > 0) {
                result = triggerService.lockNextTrigger(
                        name, taskExecutor.getFreeThreads(), timeDue);
                runningOn.setRunnungTasks(taskExecutor.getRunningTasks() + result.size());
            } else {
                result = Collections.emptyList();
                log.debug("triggerNextTasks({}) skipped as no free threads are available.", timeDue);
            }
            return result;
        });
        return taskExecutor.submit(triggers);
    }

    /**
     * Runs the next trigger if free threads are available.
     */
    public Optional<Future<TriggerId>> runOrQueue(AddTriggerRequest<? extends Serializable> trigger) {
        return trx.execute(t -> {
            Optional<Future<TriggerId>> result = Optional.empty();
            final TriggerId id = triggerService.queue(trigger);
            if (taskExecutor.getFreeThreads() > 0) {
                var toRun = triggerService.markTriggerInExecution(id, name).get();
                result = Optional.of(taskExecutor.submit(toRun));
                pingRegistry();
            } else {
                log.debug("Currently not enough free thread available {} of {} in use. Task {} queued.", 
                        taskExecutor.getFreeThreads(), taskExecutor.getMaxThreads(), id);
            }
            return result;
        });
    }

    public <T extends Serializable> TriggerId queue(TaskId<T> taskId, T state) {
        return triggerService.queue(taskId.newUniqueTrigger(state));
    }

    public SchedulerEntity getStatus() {
        return editSchedulerStatus.get(name);
    }

    @Transactional
    public List<TriggerEntity> rescheduleAbandonedTasks(Duration timeout) {
        final var onlineSchedulers = editSchedulerStatus.findOnlineSchedulers(timeout);
        final List<TriggerEntity> result = triggerService.rescheduleAbandonedTasks(onlineSchedulers.online());
        log.info("Reschedule {} abandoned triggers for {}.", result.size(), onlineSchedulers);
        return result;
    }
}
