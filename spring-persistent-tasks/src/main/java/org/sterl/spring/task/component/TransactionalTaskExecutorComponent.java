package org.sterl.spring.task.component;

import java.io.Serializable;
import java.time.Duration;
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
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.task.api.Task;
import org.sterl.spring.task.api.TriggerId;
import org.sterl.spring.task.model.TriggerEntity;
import org.sterl.spring.task.model.TriggerStatus;
import org.sterl.spring.task.repository.TaskRepository;

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
public class TransactionalTaskExecutorComponent {
    @Value("${persistent-timer.max-threads:10}")
    @Getter @Setter
    private int maxThreads = 10;
    @Getter @Setter
    private Duration maxShutdownWaitTime = Duration.ofSeconds(10);
    private ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
    private final AtomicInteger runningTasks = new AtomicInteger(0);
    private final StateSerializer serializer = new StateSerializer();
    private final AtomicBoolean stopped = new AtomicBoolean(true);

    private final TaskRepository taskRepository;
    private final EditTaskTriggerComponent editTaskTriggerComponent;
    private final TransactionTemplate trx;
    
    @NonNull
    public Future<?> execute(@Nullable TriggerEntity trigger) {
        if (trigger == null) return CompletableFuture.completedFuture(null);
        runningTasks.incrementAndGet();
        return executor.submit(() -> runInTransaction(trigger));
    }

    @PostConstruct
    public void start() {
        if (stopped.compareAndExchange(true, false)) {
            synchronized(stopped) {
                log.info("Starting with {} threads", maxThreads);
                executor = Executors.newFixedThreadPool(maxThreads);
            }
        }
    }

    @PreDestroy
    public void stop() {
        if (stopped.compareAndExchange(false, true)) {
            synchronized (stopped) {
                executor.shutdown();
                waitForRunningTasks();
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
    
    public int getFreeThreads() {
        if (stopped.get()) return 0;
        return Math.max(maxThreads - runningTasks.get(), 0);
    }

    public int getRunningTasks() {
        return runningTasks.get();
    }
    
    public boolean isStopped() {
        return stopped.get() || maxThreads <= 0;
    }

    private void runInTransaction(TriggerEntity trigger) {
        log.debug("Running task={} - totalActive={}", trigger, runningTasks.get());
        final Task<Serializable> task = taskRepository.get(trigger.newTaskId());
        try {
            trx.executeWithoutResult(t -> {
                task.accept(serializer.deserialize(trigger.getState()));
                success(trigger.getId());
            });
        } catch (Exception e) {
            handleTaskException(trigger, task, e);
        } finally {
            runningTasks.decrementAndGet();
        }
    }

    private void handleTaskException(TriggerEntity trigger, Task<Serializable> task, Exception e) {
        if (task.retryStrategy().shouldRetry(trigger.getExecutionCount(), e)) {
            log.warn("Task={} failed, retry will be done!", 
                    trigger, e);
            editTaskTriggerComponent.completeWithRetry(
                    trigger.getId(), e, task.retryStrategy().retryAt(trigger.getExecutionCount(), e));
        } else {
            log.error("Task={} failed, no retry!", trigger, e);
            editTaskTriggerComponent.completeTaskWithStatus(trigger.getId(), TriggerStatus.FAILED, e);
        }
    }

    private void success(TriggerId id) {
        editTaskTriggerComponent.completeTaskWithStatus(id, TriggerStatus.SUCCESS, null);
    }

}
