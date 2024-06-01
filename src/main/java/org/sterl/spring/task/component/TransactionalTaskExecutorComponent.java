package org.sterl.spring.task.component;

import java.io.Serializable;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.task.api.Task;
import org.sterl.spring.task.api.TaskTrigger;
import org.sterl.spring.task.model.TriggerStatus;
import org.sterl.spring.task.model.TriggerEntity;
import org.sterl.spring.task.model.TriggerId;
import org.sterl.spring.task.repository.TaskRepository;

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
    @Getter @Setter
    private int maxTasks = 10;
    @Getter @Setter
    private Duration maxShutdownWaitTime = Duration.ofSeconds(10);
    private ExecutorService executor = Executors.newFixedThreadPool(maxTasks);
    private final AtomicInteger runningTasks = new AtomicInteger(0);
    private final StateSerializer serializer = new StateSerializer();
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    private final TaskRepository taskRepository;
    private final EditTaskTriggerComponent editTaskTriggerComponent;
    private final TransactionTemplate trx;
    
    public Future<?> execute(TriggerEntity trigger) {
        return executor.submit(() -> runInTransaction(trigger));
    }

    @PostConstruct
    public void start() {
        if (stopped.compareAndExchange(true, false)) {
            synchronized(stopped) {
                executor = Executors.newFixedThreadPool(maxTasks);
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
        return Math.max(maxTasks - runningTasks.get(), 0);
    }

    public int getRunningTasks() {
        return runningTasks.get();
    }
    
    public boolean isStopped() {
        return stopped.get() || maxTasks <= 0;
    }

    private void runInTransaction(TriggerEntity trigger) {
        final int count = runningTasks.incrementAndGet();
        log.debug("Running task={} - totalActive={}", trigger, count);
        final Task<Serializable> task = taskRepository.get(trigger.newTaskId());
        try {
            trx.executeWithoutResult(t -> {
                final var result = task.execute(serializer.deserialize(trigger.getState()));
                success(trigger.getId());
                triggerAllNoResult(result.triggers());
            });
        } catch (Exception e) {
            handleTaskException(trigger, task, e);
        } finally {
            runningTasks.decrementAndGet();
        }
    }

    private void handleTaskException(TriggerEntity trigger, Task<Serializable> task, Exception e) {
        if (task.retryStrategy().shouldRetry(trigger.getExecutionCount(), e)) {
            log.warn("Task={} failed, retry will be done!", trigger.getId(), e);
            editTaskTriggerComponent.completeWithRetry(
                    trigger.getId(), e, task.retryStrategy().retryAt(trigger.getExecutionCount(), e));
        } else {
            log.error("Task={} failed", trigger.getId(), e);
            editTaskTriggerComponent.completeTaskWithStatus(trigger.getId(), TriggerStatus.FAILED, e);
        }
    }

    private void triggerAllNoResult(Collection<TaskTrigger<?>> triggers) {
        triggers.forEach(t -> taskRepository.assertIsKnown(t.taskId()));
        editTaskTriggerComponent.triggerAll(triggers);
    }
    
    private void success(TriggerId id) {
        editTaskTriggerComponent.completeTaskWithStatus(id, TriggerStatus.SUCCESS, null);
    }

}
