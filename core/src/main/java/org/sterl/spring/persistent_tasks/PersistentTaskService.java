package org.sterl.spring.persistent_tasks;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.api.AddTriggerRequest;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.event.TriggerTaskCommand;
import org.sterl.spring.persistent_tasks.history.HistoryService;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryLastStateEntity;
import org.sterl.spring.persistent_tasks.scheduler.SchedulerService;
import org.sterl.spring.persistent_tasks.shared.model.TriggerData;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * Abstraction to {@link SchedulerService} or {@link TriggerService}
 * depends on if the {@link SchedulerService} is available.
 */
@Service
@RequiredArgsConstructor
public class PersistentTaskService {

    private final Optional<SchedulerService> schedulerService;
    private final List<SchedulerService> schedulers;
    private final TriggerService triggerService;
    private final HistoryService historyService;

    /**
     * Returns the last known {@link TriggerData} to a given key. First running triggers are checked.
     * Maybe out of the history event from a retry execution of the very same id.
     *
     * @param key the {@link TriggerKey} to look for
     * @return the {@link TriggerData} to the {@link TriggerKey}
     */
    public Optional<TriggerData> getLastTriggerData(TriggerKey key) {
        final Optional<TriggerEntity> trigger = triggerService.get(key);
        if (trigger.isEmpty()) {
            var history = historyService.findLastKnownStatus(key);
            if (history.isPresent()) {
                return Optional.ofNullable(history.get().getData());
            }
            return Optional.empty();
        } else {
            return Optional.ofNullable(trigger.get().getData());
        }
    }

    public Optional<TriggerData> getLastDetailData(TriggerKey key) {
        var data = historyService.findAllDetailsForKey(key, Pageable.ofSize(1));
        if (data.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(data.getContent().get(0).getData());
    }

    @EventListener
    void queue(TriggerTaskCommand<? extends Serializable> event) {
        if (event.size() == 1) {
            runOrQueue(event.triggers().iterator().next());
        } else {
            queue(event.triggers());
        }
    }

    /**
     * Queues/updates the given triggers, if the {@link TriggerKey} is already present
     *
     * @param <T> the state type
     * @param triggers the triggers to add
     * @return the {@link TriggerKey}
     */
    @Transactional(timeout = 10)
    @NonNull
    public <T extends Serializable> List<TriggerKey> queue(Collection<AddTriggerRequest<T>> triggers) {
        if (triggers == null || triggers.isEmpty()) {
            return Collections.emptyList();
        }

        return triggers.stream() //
            .map(t -> triggerService.queue(t)) //
            .map(TriggerEntity::getKey) //
            .toList();
    }
    /**
     * Queues/updates the given trigger, if the {@link TriggerKey} is already present.
     *
     * @param <T> the state type
     * @param trigger the trigger to add
     * @return the {@link TriggerKey}
     */
    @Transactional(timeout = 5)
    @NonNull
    public <T extends Serializable> TriggerKey queue(AddTriggerRequest<T> trigger) {
        return triggerService.queue(trigger).getKey();
    }

    /**
     * Runs the given trigger if a free threads are available
     * and the runAt time is not in the future.
     * @return the reference to the {@link TriggerKey}
     */
    public <T extends Serializable> TriggerKey runOrQueue(
            AddTriggerRequest<T> triggerRequest) {
        if (schedulerService.isPresent()) {
            schedulerService.get().runOrQueue(triggerRequest);
        } else {
            triggerService.queue(triggerRequest);
        }
        return triggerRequest.key();
    }

    /**
     * Triggers the execution of all pending triggers.
     *
     * @return the reference to the {@link TriggerKey} of the running tasks
     */
    public List<Future<TriggerKey>> executeTriggers() {
        var result = new ArrayList<Future<TriggerKey>>();
        for (SchedulerService s : schedulers) {
            result.addAll(s.triggerNextTasks());
        }
        return result;
    }

    /**
     * Triggers the execution of all pending triggers and wait for the result.
     */
    @SneakyThrows
    public List<TriggerKey> executeTriggersAndWait() {
        final var result = new ArrayList<TriggerKey>();

        List<Future<TriggerKey>> triggers;
        var isSomethingRunning = false;
        do {
            triggers = executeTriggers();
            for (Future<TriggerKey> future : triggers) {
                try {
                    result.add(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    final Throwable cause = e.getCause();
                    throw cause == null ? e : cause;
                }
            }

            isSomethingRunning = hasRunningTriggers();
            if (isSomethingRunning) {
                Thread.sleep(Duration.ofMillis(100));
            }

        } while (!triggers.isEmpty() || isSomethingRunning);

        return result;
    }

    private boolean hasRunningTriggers() {
        var running = this.schedulers.stream()
                .map(s -> s.hasRunningTriggers())
                .filter(r -> r)
                .findAny();

        return running.isPresent() && running.get() == true;
    }

    /**
     * Returns all triggers for a correlationId sorted by the creation time.
     * @param correlationId the id to search for
     * @return the found {@link TriggerData} sorted by create time ASC
     */
    @Transactional(readOnly = true, timeout = 5)
    public List<TriggerData> findAllTriggerByCorrelationId(String correlationId) {

        var running = triggerService.findTriggerByCorrelationId(correlationId)
                .stream().map(TriggerEntity::getData)
                .toList();

        var done = historyService.findTriggerByCorrelationId(correlationId)
            .stream().map(TriggerHistoryLastStateEntity::getData)
            .toList();


        var result = new ArrayList<TriggerData>(running.size() + done.size());
        result.addAll(done);
        result.addAll(running);
        return result;
    }
}
