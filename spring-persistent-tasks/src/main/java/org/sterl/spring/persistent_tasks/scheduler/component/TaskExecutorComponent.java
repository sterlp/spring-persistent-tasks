package org.sterl.spring.persistent_tasks.scheduler.component;

import java.io.Closeable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.sterl.spring.persistent_tasks.api.TriggerId;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskExecutorComponent implements Closeable {

    private final TriggerService triggerService;

    @Value("${persistent-tasks.max-threads:10}")
    @Getter
    @Setter
    private int maxThreads = 10;
    @Getter
    @Setter
    private Duration maxShutdownWaitTime = Duration.ofSeconds(10);
    private ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
    private final AtomicInteger runningTasks = new AtomicInteger(0);
    private final AtomicBoolean stopped = new AtomicBoolean(true);

    @NonNull
    public List<Future<TriggerId>> submit(List<TriggerEntity> trigger) {
        final List<Future<TriggerId>> result = new ArrayList<>(trigger.size());
        for (TriggerEntity triggerEntity : trigger) {
            result.add(submit(triggerEntity));
        }
        return result;
    }

    @NonNull
    public Future<TriggerId> submit(@Nullable TriggerEntity trigger) {
        if (trigger == null) {
            return CompletableFuture.completedFuture(null);
        }
        runningTasks.incrementAndGet();
        return executor.submit(() -> runTrigger(trigger));
    }

    private TriggerId runTrigger(TriggerEntity trigger) {
        try {
            triggerService.run(trigger);
            return trigger.getId();
        } finally {
            runningTasks.decrementAndGet();
        }
    }

    @PostConstruct
    public void start() {
        if (stopped.compareAndExchange(true, false)) {
            synchronized (stopped) {
                executor = Executors.newFixedThreadPool(maxThreads);
            }
        }
    }

    @Override
    @PreDestroy
    public void close() {
        if (stopped.compareAndExchange(false, true)) {
            synchronized (stopped) {
                executor.shutdown();
                waitForRunningTasks();
                executor = null;
            }
        }
    }

    private void waitForRunningTasks() {
        if (runningTasks.get() > 0) {
            log.info("Shutdown executor with {} running tasks, waiting for {}.",
                    runningTasks.get(), maxShutdownWaitTime);

            try {
                executor.awaitTermination(maxShutdownWaitTime.getSeconds(), TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.warn("Failed to complete runnings tasks.", e.getCause());
            } finally {
                executor.shutdownNow();
            }
        }
    }

    public void shutdownNow() {
        stopped.set(true);
        executor.shutdownNow();
    }

    public int getFreeThreads() {
        if (stopped.get()) {
            return 0;
        }
        return Math.max(maxThreads - runningTasks.get(), 0);
    }

    public int getRunningTasks() {
        return runningTasks.get();
    }

    public boolean isStopped() {
        return stopped.get() || maxThreads <= 0;
    }
}
