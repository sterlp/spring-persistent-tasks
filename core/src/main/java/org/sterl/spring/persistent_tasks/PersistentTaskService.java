package org.sterl.spring.persistent_tasks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerRequest;
import org.sterl.spring.persistent_tasks.api.TriggerSearch;
import org.sterl.spring.persistent_tasks.api.event.TriggerTaskCommand;
import org.sterl.spring.persistent_tasks.history.HistoryService;
import org.sterl.spring.persistent_tasks.history.model.CompletedTriggerEntity;
import org.sterl.spring.persistent_tasks.scheduler.SchedulerService;
import org.sterl.spring.persistent_tasks.shared.model.TriggerEntity;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;
import org.sterl.spring.persistent_tasks.trigger.model.RunningTriggerEntity;

import lombok.RequiredArgsConstructor;

/**
 * Abstraction to {@link SchedulerService} or {@link TriggerService}
 * depends on if the {@link SchedulerService} is available.
 */
@Service
@RequiredArgsConstructor
public class PersistentTaskService {

    private final Optional<SchedulerService> schedulerService;
    private final TriggerService triggerService;
    private final HistoryService historyService;

    /**
     * Returns the last known {@link TriggerEntity} to a given key. First running triggers are checked.
     * Maybe out of the history event from a retry execution of the very same id.
     *
     * @param key the {@link TriggerKey} to look for
     * @return the {@link TriggerEntity} to the {@link TriggerKey}
     */
    public Optional<TriggerEntity> getLastTriggerData(TriggerKey key) {
        final Optional<RunningTriggerEntity> trigger = triggerService.get(key);
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

    public Optional<TriggerEntity> getLastDetailData(TriggerKey key) {
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
    public <T extends Serializable> List<TriggerKey> queue(Collection<TriggerRequest<T>> triggers) {
        if (triggers == null || triggers.isEmpty()) {
            return Collections.emptyList();
        }

        return triggers.stream() //
            .map(t -> triggerService.queue(t)) //
            .map(RunningTriggerEntity::getKey) //
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
    public <T extends Serializable> TriggerKey queue(TriggerRequest<T> trigger) {
        return triggerService.queue(trigger).getKey();
    }

    /**
     * Runs the given trigger if a free threads are available
     * and the runAt time is not in the future.
     * @return the reference to the {@link TriggerKey}
     */
    public <T extends Serializable> TriggerKey runOrQueue(
            TriggerRequest<T> triggerRequest) {
        if (schedulerService.isPresent()) {
            schedulerService.get().runOrQueue(triggerRequest);
        } else {
            triggerService.queue(triggerRequest);
        }
        return triggerRequest.key();
    }

    /**
     * Returns all triggers for a correlationId sorted by the creation time.
     * Data is limited to overall 300 elements.
     * 
     * @param correlationId the id to search for
     * @return the found {@link TriggerEntity} sorted by create time ASC
     */
    @Transactional(readOnly = true, timeout = 5)
    public List<TriggerEntity> findAllTriggerByCorrelationId(String correlationId) {
        if (StringUtils.isAllBlank(correlationId)) return Collections.emptyList();
        
        final var search = TriggerSearch.byCorrelationId(correlationId);

        final var running = triggerService.searchTriggers(search, PageRequest.of(0, 100, TriggerSearch.DEFAULT_SORT))
                .stream().map(RunningTriggerEntity::getData)
                .toList();

        final var done = historyService.searchTriggers(search, PageRequest.of(0, 200, TriggerSearch.DEFAULT_SORT))
            .stream().map(CompletedTriggerEntity::getData)
            .toList();

        final var result = new ArrayList<TriggerEntity>(running.size() + done.size());
        result.addAll(done);
        result.addAll(running);
        return result;
    }

    @Transactional(readOnly = true, timeout = 5)
    public Optional<TriggerEntity> findLastTriggerByCorrelationId(String correlationId) {
        final var page = PageRequest.of(0, 1, TriggerSearch.sortByCreatedTime(Direction.DESC));
        final var search = TriggerSearch.byCorrelationId(correlationId);
        
        var result = triggerService.searchTriggers(search, page)
                                   .stream().map(RunningTriggerEntity::getData)
                                   .toList();

        if (result.isEmpty()) {
            result = historyService.searchTriggers(search, page)
                                   .stream().map(CompletedTriggerEntity::getData)
                                   .toList();
        }
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }
}
