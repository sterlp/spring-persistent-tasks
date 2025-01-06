package org.sterl.spring.persistent_tasks;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.Test;
import org.sterl.spring.persistent_tasks.api.PersistentTask;
import org.sterl.spring.persistent_tasks.api.RetryStrategy;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;

class TaskSchedulerServiceTest extends AbstractSpringTest {

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
}
