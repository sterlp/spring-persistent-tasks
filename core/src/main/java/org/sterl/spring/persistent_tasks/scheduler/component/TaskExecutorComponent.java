package org.sterl.spring.persistent_tasks.scheduler.component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.scheduler.config.SchedulerThreadFactory;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;
import org.sterl.spring.persistent_tasks.trigger.model.RunningTriggerEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * The executor of a scheduler
 * <p>
 * Not a spring bean!
 * </p>
 */
@Slf4j
public class TaskExecutorComponent {

    private final String schedulerName;
    private final TriggerService triggerService;
    private final SchedulerThreadFactory threadFactory;
    private final AtomicInteger maxThreads = new AtomicInteger(0);
    @Getter
    @Setter
    private Duration maxShutdownWaitTime = Duration.ofSeconds(10);
    @Nullable
    private ExecutorService executor;
    private final ConcurrentHashMap<RunningTriggerEntity, Future<TriggerKey>> runningTasks = new ConcurrentHashMap<>();
    private final AtomicBoolean stopped = new AtomicBoolean(true);
    private final Lock lock = new ReentrantLock(true);

    public TaskExecutorComponent(String schedulerName, TriggerService triggerService,
            SchedulerThreadFactory threadFactory, int maxThreads) {
        super();
        this.schedulerName = schedulerName;
        this.triggerService = triggerService;
        this.maxThreads.set(maxThreads);
        this.threadFactory = threadFactory;
    }

    @NonNull
    public List<Future<TriggerKey>> submit(List<RunningTriggerEntity> trigger) {
        if (trigger == null || trigger.isEmpty())
            return Collections.emptyList();

        final List<Future<TriggerKey>> result = new ArrayList<>(trigger.size());
        for (RunningTriggerEntity triggerEntity : trigger) {
            result.add(submit(triggerEntity));
        }
        return result;
    }

    @NonNull
    public Future<TriggerKey> submit(@Nullable RunningTriggerEntity trigger) {
        if (trigger == null) {
            return CompletableFuture.completedFuture(null);
        }
        lock.lock();
        assertStarted();
        try {
            var result = executor.submit(() -> runTrigger(trigger));
            runningTasks.put(trigger, result);
            return result;
        } catch (Exception e) {
            runningTasks.remove(trigger);
            throw new RuntimeException("Failed to run " + trigger.getKey(), e);
        } finally {
            lock.unlock();
        }

    }

    private void assertStarted() {
        if (stopped.get() || executor == null) {
            throw new IllegalStateException("Executor of " + schedulerName + " is already stopped");
        }
    }

    private TriggerKey runTrigger(RunningTriggerEntity trigger) {
        try {
            triggerService.run(trigger);
            return trigger.getKey();
        } finally {
            lock.lock();
            try {
                if (runningTasks.remove(trigger) == null && runningTasks.size() > 0) {
                    var runningKeys = runningTasks.keySet().stream().map(RunningTriggerEntity::key).toList();
                    log.error("Failed to remove trigger with {} - {}", trigger.key(), runningKeys);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    public void start() {
        if (stopped.compareAndExchange(true, false)) {
            lock.lock();
            try {
                runningTasks.clear();
                executor = threadFactory.newExecutorService(getMaxThreads());
                log.info("Started {} with {} threads.", schedulerName, maxThreads.get());
                stopped.set(false);
            } finally {
                lock.unlock();
            }
        }
    }

    public void shutdown() {
        stopped.set(true);
        if (executor != null) {
            lock.lock();
            try {
                executor.shutdown();
                if (runningTasks.size() > 0) {
                    log.info("Shutdown {} with {} running tasks, waiting for {}.", schedulerName, runningTasks.size(),
                            maxShutdownWaitTime);
                    try {
                        executor.awaitTermination(maxShutdownWaitTime.getSeconds(), TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Failed to complete runnings tasks.", e.getCause() == null ? e : e.getCause());
                        shutdownNow();
                    }
                } else {
                    log.info("Shutdown {}.", schedulerName);
                }
            } finally {
                executor = null;
                runningTasks.clear();
                lock.unlock();
            }
        }
    }

    public void shutdownNow() {
        stopped.set(true);
        if (executor != null) {
            lock.lock();
            try {
                executor.shutdownNow();
                log.info("Force stop {} with {} running tasks", schedulerName, runningTasks.size());
            } finally {
                runningTasks.clear();
                executor = null;
                lock.unlock();
            }
        }
    }

    public int getFreeThreads() {
        if (stopped.get()) {
            return 0;
        }
        if (maxThreads.get() - runningTasks.size() < 0) {
            log.warn("Already {} running tasks, more than threads {} in pool.", runningTasks.size(), maxThreads.get());
        }
        return Math.max(maxThreads.get() - runningTasks.size(), 0);
    }

    public int countRunning() {
        return runningTasks.size();
    }

    public Collection<Future<TriggerKey>> getRunningTasks() {
        return runningTasks.values();
    }

    public List<RunningTriggerEntity> getRunningTriggers() {
        var doneAndNotRemovedFutures = this.runningTasks.entrySet().stream().filter(e -> e.getValue().isDone())
                .toList();

        if (doneAndNotRemovedFutures.size() > 0) {
            log.warn("Found still pending futures, maybe an issue, report a bug if so {}",
                    doneAndNotRemovedFutures.stream().map(e -> e.getKey().getKey()));
            for (var entry : doneAndNotRemovedFutures) {
                runningTasks.remove(entry.getKey());
            }
        }

        return Collections.list(this.runningTasks.keys());
    }

    public boolean isStopped() {
        return stopped.get() || maxThreads.get() <= 0;
    }

    public void setMaxThreads(int value) {
        this.maxThreads.set(value);
    }

    public int getMaxThreads() {
        return this.maxThreads.get();
    }

    public boolean isRunning(RunningTriggerEntity trigger) {
        return runningTasks.containsKey(trigger);
    }
}
