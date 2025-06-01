package org.sterl.spring.persistent_tasks.scheduler.component;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.sterl.spring.persistent_tasks.api.AddTriggerRequest;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * Not a spring bean!
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class RunOrQueueComponent {
    private final String schedulerName;
    private final TriggerService triggerService;
    private final TaskExecutorComponent taskExecutor;
    private final Map<Long, TriggerEntity> shouldRun = new ConcurrentHashMap<>();

    /**
     * Runs the given trigger if a free threads are available and the runAt time is
     * not in the future.
     * 
     * @return the reference to the {@link Future} with the key, if no threads are
     *         available it is resolved
     */
    public <T extends Serializable> TriggerKey execute(AddTriggerRequest<T> triggerRequest) {
        var trigger = triggerService.queue(triggerRequest);

        trigger = offerToRun(trigger);
        // we will listen for the commit event to execute this trigger ...
        return trigger.getKey();
    }

    private TriggerEntity offerToRun(TriggerEntity trigger) {
        if (!trigger.shouldRunInFuture()) {
            if (taskExecutor.getFreeThreads() > 0) {
                trigger = triggerService.markTriggersAsRunning(trigger, schedulerName);
                shouldRun.put(trigger.getId(), trigger);
                log.debug("Immediate execution for={}, waiting for commit on={}", trigger.getKey(), schedulerName);
            } else {
                log.debug("Currently not enough free thread available {} of {} in use. PersistentTask {} queued.",
                        taskExecutor.getFreeThreads(), taskExecutor.getMaxThreads(), trigger.getKey());
            }
        }
        return trigger;
    }

    public boolean checkIfTrigerShouldRun(long triggerId) {
        final var toRun = shouldRun.remove(triggerId);
        if (toRun != null) {
            taskExecutor.submit(toRun);
            log.debug("{} immediately started on={}.", toRun.key(), schedulerName);
        }
        clearNotCreatedTriggers();
        return toRun != null;
    }
    
    public boolean hasWaitingTriggers() {
        clearNotCreatedTriggers();
        return shouldRun.size() > 0;
    }

    private void clearNotCreatedTriggers() {
        if (shouldRun.size() > 0) {
            final var timeout = OffsetDateTime.now().plusSeconds(15);
            final var triggers = shouldRun.entrySet().iterator();
            while (triggers.hasNext()) {
                var entry = triggers.next();
                if (entry.getValue().getData().getCreatedTime().isAfter(timeout)) {
                    log.info("Removed {} which was not committed!", entry.getValue().key());
                    triggers.remove();
                }
            }
        }
    }

    public void clear() {
        shouldRun.clear();
    }
}
