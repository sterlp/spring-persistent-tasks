package org.sterl.spring.persistent_tasks;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.lang.Nullable;
import org.sterl.spring.persistent_tasks.api.RetryStrategy;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.api.task.PersistentTask;

class TaskSchedulerServiceTest extends AbstractSpringTest {
    @Test
    void testFailedTasksAreRetried() throws Exception {
        // GIVEN
        TaskId<String> task = taskService.<String>replace("foo",
                new PersistentTask<String>() {
                    @Override
                    public void accept(@Nullable String state) {
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
        persistentTaskTestService.assertHasNextTask();
        persistentTaskTestService.assertHasNextTask();

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
        var executedKeys = persistentTaskTestService.scheduleNextTriggersAndWait(Duration.ofSeconds(3));

        // THEN
        assertThat(executedKeys).hasSize(100);
        for (int i = 1; i <= 100; ++i) {
            asserts.awaitValueOnce("t" + i);
        }
        assertThat(historyService.countTriggers(TriggerStatus.SUCCESS)).isEqualTo(100);
    }
}
