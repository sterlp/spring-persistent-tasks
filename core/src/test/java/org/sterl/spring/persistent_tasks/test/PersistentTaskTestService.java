package org.sterl.spring.persistent_tasks.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.stereotype.Service;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.scheduler.SchedulerService;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;
import org.sterl.spring.persistent_tasks.trigger.model.RunningTriggerEntity;

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
    public Optional<RunningTriggerEntity> runNextTrigger() {
        return triggerService.run(triggerService.lockNextTrigger("test"));
    }
    
    /**
     * Runs all triggers which are due until the given time. One by one, so new triggers are picked up.
     * 
     * @param dueUntil date to also check for trigger in the future
     * @return the triggers executed, to directly check if they have been successful
     */
    public List<RunningTriggerEntity> runAllDueTrigger(OffsetDateTime dueUntil) {
        var result = new ArrayList<RunningTriggerEntity>();
        List<RunningTriggerEntity> trigger; 
        while ( (trigger = triggerService.lockNextTrigger("test", 1, dueUntil)).size() > 0 ) {
            var key = triggerService.run(trigger.getFirst());
            if (key.isPresent()) result.add(key.get());
        }
        return result;
    }

    /**
     * Triggers the execution of all pending triggers. We also add one second
     * to ensure we select all right now created triggers too.
     *
     * @return the reference to the {@link TriggerKey} of the running tasks
     */
    public List<Future<TriggerKey>> scheduleNextTriggers() {
        var result = new ArrayList<Future<TriggerKey>>();
        assertHasScheduler();
        for (SchedulerService s : schedulers) {
            result.addAll(s.triggerNextTasks(OffsetDateTime.now().plusSeconds(1)));
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
    public Set<TriggerKey> scheduleNextTriggersAndWait() {
        return scheduleNextTriggersAndWait(defaultTimeout);
    }

    /**
     * Triggers the execution of all pending triggers and wait for the result.
     */
    @SneakyThrows
    public Set<TriggerKey> scheduleNextTriggersAndWait(Duration maxWaitTime) {
        final var result = new LinkedHashSet<TriggerKey>();
        final var timeOut = System.currentTimeMillis() + maxWaitTime.toMillis();

        List<TriggerKey> newTriggers;
        do {

            if (System.currentTimeMillis() > timeOut) {
                throw new RuntimeException("Timeout waiting for triggers after " + maxWaitTime);
            }
            // 1. the running
            newTriggers = awaitRunningTriggers(maxWaitTime);
            
            // 2. check if we have waiting new tasks
            if (newTriggers.isEmpty()) {
                newTriggers = awaitTriggers(maxWaitTime, scheduleNextTriggers());
            }
            // 3. in case we are really fast in our tests (empty tasks) we double check
            // that we have no race condition and missed a now running task
            if (newTriggers.isEmpty()) {
                newTriggers = awaitRunningTriggers(maxWaitTime);
            }
            result.addAll(newTriggers);

        } while (newTriggers.size() > 0);

        return result;
    }
    
    /**
     * Just waits for the current running triggers
     * @return return the keys of the currently scheduled triggers
     */
    public List<TriggerKey> awaitRunningTriggers() {
        return awaitRunningTriggers(defaultTimeout);
    }

    /**
     * Just waits for the current running triggers
     * 
     * @param duration how long to wait
     * @return return the keys of the currently scheduled triggers
     */
    @SneakyThrows
    public List<TriggerKey> awaitRunningTriggers(Duration duration) {
        assertHasScheduler();
        List<Future<TriggerKey>> running = this.schedulers.stream()
                .flatMap(s -> s.getRunning().stream())
                .toList();
        
        return awaitTriggers(duration, running);
    }

    public ArrayList<TriggerKey> awaitTriggers(Duration duration, List<Future<TriggerKey>> running) throws Throwable {
        final var result = new ArrayList<TriggerKey>();
        final var totalWaitUntil = System.currentTimeMillis() + duration.toMillis();
        for (Future<TriggerKey> t : running) {
            try {
                result.add(t.get(totalWaitUntil - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                final Throwable cause = e.getCause();
                throw cause == null ? e : cause;
            }
        }
        return result;
    }

    public int countRunningTriggers() {
        return schedulers.stream().mapToInt(s -> s.getRunning().size()).sum();
    }

    public boolean hasRunningTriggers() {
        assertHasScheduler();
        return countRunningTriggers() > 0 ;
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
}