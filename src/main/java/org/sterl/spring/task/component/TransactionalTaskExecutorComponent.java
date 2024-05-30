package org.sterl.spring.task.component;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.task.api.Task;
import org.sterl.spring.task.api.TaskTrigger;
import org.sterl.spring.task.model.TaskStatus;
import org.sterl.spring.task.model.TaskTriggerEntity;
import org.sterl.spring.task.model.TaskTriggerId;
import org.sterl.spring.task.repository.TaskRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionalTaskExecutorComponent {
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private final AtomicInteger runningTasks = new AtomicInteger(0);
    private final StateSerializer serializer = new StateSerializer();

    private final TaskRepository taskRepository;
    private final EditTaskInstanceComponent editTaskInstanceComponent;
    private final TransactionTemplate trx;
    
    public Future<?> execute(TaskTriggerEntity trigger) {
        return executor.submit(() -> runInTransaction(trigger));
    }
    
    public int getRunningTasks() {
        return runningTasks.get();
    }

    private void runInTransaction(TaskTriggerEntity trigger) {
            int count = runningTasks.incrementAndGet();
            log.debug("Running task={} - totalActive={}", trigger, count);
            Task<Serializable> task = taskRepository.get(trigger.newTaskId());
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

    private void handleTaskException(TaskTriggerEntity trigger, Task<Serializable> task, Exception e) {
        if (task.retryStrategy().shouldRetry(trigger.getExecutionCount(), e)) {
            log.warn("Task={} failed, retry will be done!", trigger.getId(), e);
            editTaskInstanceComponent.completeWithRetry(
                    trigger.getId(), e, task.retryStrategy().retryAt(trigger.getExecutionCount(), e));
        } else {
            log.error("Task={} failed", trigger.getId(), e);
            editTaskInstanceComponent.completeTaskWithStatus(trigger.getId(), TaskStatus.FAILED, e);
        }
    }

    private void triggerAllNoResult(Collection<TaskTrigger<?>> triggers) {
        triggers.forEach(t -> taskRepository.assertIsKnown(t.taskId()));
        editTaskInstanceComponent.triggerAll(triggers);
    }
    
    private void success(TaskTriggerId id) {
        editTaskInstanceComponent.completeTaskWithStatus(id, TaskStatus.SUCCESS, null);
    }

}
