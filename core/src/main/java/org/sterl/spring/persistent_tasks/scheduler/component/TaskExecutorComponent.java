package org.sterl.spring.persistent_tasks.scheduler.component;

import java.io.Closeable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

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
public class TaskExecutorComponent implements Closeable {

    private final String schedulerName;
    private final TriggerService triggerService;
    private final AtomicInteger maxThreads = new AtomicInteger(0);
    @Getter
    @Setter
    private Duration maxShutdownWaitTime = Duration.ofSeconds(10);
    @Nullable
    private ExecutorService executor;
    // also the LOCK object ...
    private final ConcurrentHashMap<TriggerEntity, Future<TriggerKey>> runningTasks = new ConcurrentHashMap<>();
    private final AtomicBoolean stopped = new AtomicBoolean(true);

    public TaskExecutorComponent(String schedulerName, TriggerService triggerService, int maxThreads) {
        super();
        this.schedulerName = schedulerName;
        this.triggerService = triggerService;
        this.maxThreads.set(maxThreads);
    }

    @NonNull
    public List<Future<TriggerKey>> submit(List<TriggerEntity> trigger) {
        if (trigger == null || trigger.isEmpty())
            return Collections.emptyList();

        final List<Future<TriggerKey>> result = new ArrayList<>(trigger.size());
        for (TriggerEntity triggerEntity : trigger) {
            result.add(submit(triggerEntity));
        }
        return result;
    }

    @NonNull
    public Future<TriggerKey> submit(@Nullable TriggerEntity trigger) {
        if (trigger == null) {
            return CompletableFuture.completedFuture(null);
        }
        if (stopped.get() || executor == null) {
            throw new IllegalStateException("Executor of " + schedulerName + " is already stopped");
        }

        try {
            runningTasks.put(trigger, executor.submit(() -> runTrigger(trigger)));
            return runningTasks.get(trigger);
        } catch (Exception e) {
            runningTasks.remove(trigger);
            throw new RuntimeException("Failed to run " + trigger.getKey(), e);
        }

    }

    private TriggerKey runTrigger(TriggerEntity trigger) {
        try {
            triggerService.run(trigger);
            return trigger.getKey();
        } finally {
            if (runningTasks.remove(trigger) == null) {
                log.error("Failed to remove trigger with {}", trigger.key());
            }
        }
    }

    public void start() {
        synchronized (runningTasks) {
            if (stopped.compareAndExchange(true, false)) {
                runningTasks.clear();
                executor = Executors.newFixedThreadPool(maxThreads.get());
                log.info("Started {} with {} threads.", schedulerName, maxThreads.get());
            }
        }
    }

    @Override
    public void close() {
        if (stopped.compareAndExchange(false, true)) {
            ExecutorService executorRef;
            synchronized (runningTasks) {
                executorRef = executor;
                executor = null;
            }
            
            if (executorRef != null) {
                executorRef.shutdown();
                if (runningTasks.size() > 0) {
                    log.info("Shutdown {} with {} running tasks, waiting for {}.", schedulerName, runningTasks.size(),
                            maxShutdownWaitTime);

                    try {
                        executorRef.awaitTermination(maxShutdownWaitTime.getSeconds(), TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("Failed to complete runnings tasks.", e.getCause() == null ? e : e.getCause());
                        shutdownNow();
                    } finally {
                        executorRef = null;
                        runningTasks.clear();
                    }
                }
            }
        }
    }

    public void shutdownNow() {
        synchronized (runningTasks) {
            if (executor != null) {
                executor.shutdownNow();
                log.info("Force stop {} with {} running tasks", schedulerName, runningTasks.size());
                runningTasks.clear();
                executor = null;
                stopped.set(true);
            }
        }
    }

    public int getFreeThreads() {
        if (stopped.get()) {
            return 0;
        }
        return Math.max(maxThreads.get() - runningTasks.size(), 0);
    }

    public int countRunning() {
        return runningTasks.size();
    }

    public Collection<Future<TriggerKey>> getRunningTasks() {
        return runningTasks.values();
    }

    public boolean isStopped() {
        return stopped.get() || maxThreads.get() <= 0;
    }

    public List<TriggerEntity> getRunningTriggers() {
        return Collections.list(this.runningTasks.keys());
    }

    public void setMaxThreads(int value) {
        this.maxThreads.set(value);
    }

    public int getMaxThreads() {
        return isStopped() ? 0 : this.maxThreads.get();
    }

    public boolean isRunning(TriggerEntity trigger) {
        return runningTasks.containsKey(trigger);
    }
}
