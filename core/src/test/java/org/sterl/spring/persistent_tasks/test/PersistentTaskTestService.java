package org.sterl.spring.persistent_tasks.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.springframework.stereotype.Service;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.scheduler.SchedulerService;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

@Service
@RequiredArgsConstructor
public class PersistentTaskTestService {

    private final List<SchedulerService> schedulers;
    private final TriggerService triggerService;
    
    @Getter @Setter
    private Duration defaultTimeout = Duration.ofSeconds(5);
    
    /**
     * Runs just the next trigger, if it is due to run.
     * 
     * @return next {@link TriggerKey} if found
     */
    public Optional<TriggerEntity> runNextTrigger() {
        return triggerService.run(triggerService.lockNextTrigger("test"));
    }
    
    /**
     * Runs all triggers which are due until the given time. One by one, so new triggers are picked up.
     * 
     * @param dueUntil date to also check for trigger in the future
     * @return the triggeres executed, to directly check if they have been successful
     */
    public List<TriggerEntity> runAllDueTrigger(OffsetDateTime dueUntil) {
        var result = new ArrayList<TriggerEntity>();
        List<TriggerEntity> trigger; 
        while( (trigger = triggerService.lockNextTrigger("test", 1, dueUntil)).size() > 0) {
            var key = triggerService.run(trigger.getFirst());
            if (key.isPresent()) result.add(key.get());
        }
        return result;
    }

    /**
     * Triggers the execution of all pending triggers.
     *
     * @return the reference to the {@link TriggerKey} of the running tasks
     */
    public List<Future<TriggerKey>> scheduleNextTriggers() {
        var result = new ArrayList<Future<TriggerKey>>();
        assertHasScheduler();
        for (SchedulerService s : schedulers) {
            result.addAll(s.triggerNextTasks());
        }
        return result;
    }

    public void assertHasScheduler() {
        assertThat(schedulers).describedAs("No schedulers found, cannot run any triggers!").isNotEmpty();
    }
    
    /**
     * Triggers the execution of all pending triggers and wait for the result.
     */
    @SneakyThrows
    public List<TriggerKey> scheduleNextTriggersAndWait() {
        return scheduleNextTriggersAndWait(defaultTimeout);
    }

    /**
     * Triggers the execution of all pending triggers and wait for the result.
     */
    @SneakyThrows
    public List<TriggerKey> scheduleNextTriggersAndWait(Duration maxWaitTime) {
        final var result = new ArrayList<TriggerKey>();
        final var timeOut = System.currentTimeMillis() + maxWaitTime.toMillis();

        List<Future<TriggerKey>> triggers;
        var isSomethingRunning = false;
        do {
            triggers = scheduleNextTriggers();
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
            
            if (System.currentTimeMillis() > timeOut) {
                throw new RuntimeException("Timeout waiting for triggers after " + maxWaitTime);
            }

        } while (!triggers.isEmpty() || isSomethingRunning);

        return result;
    }
    
    public void awaitRunningTriggers(Duration duration) {
        final var timeout = System.currentTimeMillis() + duration.toMillis();
        do {
            sleep();
        } while (hasRunningTriggers() && System.currentTimeMillis() < timeout);
        
        int runningCount = schedulers.stream().mapToInt(s -> s.getScheduler().getRunnungTasks()).sum();
        assertThat(runningCount).describedAs("Where are sill " + runningCount + " triggers running.").isZero();
    }

    public boolean hasRunningTriggers() {
        assertHasScheduler();
        var running = this.schedulers.stream()
                .map(s -> s.hasRunningTriggers())
                .filter(r -> r)
                .findAny();

        return running.isPresent() && running.get() == true;
    }

    public void assertNoMoreTriggers() {
        var trigger = runNextTrigger();
        assertThat(trigger).isEmpty();
    }
    public void assertNextTaskSuccess() {
        assertHasNextTask(TriggerStatus.SUCCESS, null);
    }

    public void assertHasNextTask() {
        var trigger = runNextTrigger();
        assertThat(trigger).isPresent();
    }
    /**
     * Runs the next trigger and ensures where is one.
     * <p>
     * Note: Failed triggers, which have retries left will be in WAITING state
     * </p>
     * 
     * @param status optional status to check
     * @param key optional key to check
     */
    public void assertHasNextTask(TriggerStatus status, TriggerKey key) {
        var trigger = runNextTrigger();
        assertThat(trigger).isPresent();
        if (status != null) {
            assertThat(trigger.get().status()).isEqualTo(status);
        }
        if (key != null) assertThat(trigger.get().getKey()).isEqualTo(key);
    }
    
    private void sleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }
}