package org.sterl.spring.persistent_tasks;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.sterl.spring.persistent_tasks.api.RetryStrategy;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.api.event.TriggerTaskCommand;
import org.sterl.spring.persistent_tasks.api.task.PersistentTask;
import org.sterl.spring.persistent_tasks.api.task.RunningTriggerContextHolder;

class TaskSchedulerServiceTest extends AbstractSpringTest {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Test
    void testFailedTasksAreRetried() throws Exception {
        // GIVEN
        TaskId<String> task = taskService.<String>replace("foo",
                new PersistentTask<String>() {
                    @Override
                    public void accept(String state) {
                        asserts.info(state);
                        throw new RuntimeException("NOPE!");
                    }

                    @Override
                    public RetryStrategy retryStrategy() {
                        return RetryStrategy.THREE_RETRIES_IMMEDIATELY;
                    }
                });
        var runTrigger = triggerService.queue(task.newTrigger().state("hallo").build());

        // WHEN
        persistentTaskService.executeTriggersAndWait();
        persistentTaskService.executeTriggersAndWait();

        // THEN
        assertThat(asserts.getCount("hallo")).isEqualTo(4);
        assertThat(triggerService.countTriggers()).isZero();
        // AND
        var trigger = historyService.findStatus(runTrigger.getId()).get();
        assertThat(trigger.getData().getExecutionCount()).isEqualTo(4);
        assertThat(trigger.getData().getExceptionName()).isEqualTo(RuntimeException.class.getName());
        assertThat(trigger.getData().getLastException()).contains("NOPE!");
    }

    @Test
    void testLockTriggerInSchedulers() throws Exception {
        // GIVEN
        final TaskId<String> taskId = taskService.<String>replace("multi-threading", s -> asserts.info(s));
        for (int i = 1; i <= 100; ++i) {
            triggerService.queue(taskId.newUniqueTrigger("t" + i));
        }

        // WHEN
        ArrayList<Callable<?>> lockInvocations = new ArrayList<>();
        for (int i = 1; i <= 100; ++i) {
            lockInvocations.add(() -> runNextTrigger());
        }

        persistentTaskService.executeTriggersAndWait();

        // THEN
        for (int i = 1; i <= 100; ++i) {
            asserts.awaitValueOnce("t" + i);
        }
        assertThat(historyService.countTriggers(TriggerStatus.SUCCESS)).isEqualTo(100);
    }
    
    @Test
    void testChainedTasks() throws Exception {
        // GIVEN
        final AtomicReference<String> correlationFound = new AtomicReference<>();

        final TaskId<Integer> task1 = taskService.replace("chainTask1", s -> {
            var state = RunningTriggerContextHolder.getContext();
            asserts.info(state.getData() + "::chainTask1");
            eventPublisher.publishEvent(
                    TriggerTaskCommand.of("chainTask2", state.getData() + "::chainTask1",
                    UUID.randomUUID().toString())); // should be ignored!
        });

        taskService.replace("chainTask2", s -> {
            var state = RunningTriggerContextHolder.getContext();
            correlationFound.set(state.getCorrelationId());
            asserts.info("chainTask1::" + state.getData());
            assertThat(state.getCorrelationId()).isEqualTo(RunningTriggerContextHolder.getCorrelationId());
        });
        final var correlationId = UUID.randomUUID().toString();

        // WHEN
        persistentTaskService.runOrQueue(task1.newTrigger(234).correlationId(correlationId).build());

        // THEN
        asserts.awaitOrdered("234::chainTask1", "chainTask1::234::chainTask1");
        assertThat(correlationId).isEqualTo(correlationFound.get());
        // AND
        var trigger= persistentTaskService.findAllTriggerByCorrelationId(correlationId);
        assertThat(trigger).hasSize(2);
        assertThat(trigger.get(0).getCorrelationId()).isEqualTo(correlationId);
        assertThat(trigger.get(1).getCorrelationId()).isEqualTo(correlationId);
    }
}
