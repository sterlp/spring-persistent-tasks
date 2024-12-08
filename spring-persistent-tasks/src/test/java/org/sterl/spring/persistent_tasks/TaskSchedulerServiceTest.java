package org.sterl.spring.persistent_tasks;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.Test;
import org.sterl.spring.persistent_tasks.api.RetryStrategy;
import org.sterl.spring.persistent_tasks.api.SpringBeanTask;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerStatus;

class TaskSchedulerServiceTest extends AbstractSpringTest {

    @Test
    void testFailedTasksAreRetried() throws Exception {
        // GIVEN
        TaskId<String> task = taskService.<String>replace("foo",
                new SpringBeanTask<String>() {
                    @Override
                    public void accept(String state) {
                        asserts.info(state);
                        throw new RuntimeException("NOPE!");
                    }

                    @Override
                    public RetryStrategy retryStrategy() {
                        return RetryStrategy.TRY_THREE_TIMES_IMMEDIATELY;
                    }
                });
        var id = triggerService.trigger(task.newTrigger().state("hallo").build());

        // WHEN
        runTriggersAndWait();
        runTriggersAndWait();
        runTriggersAndWait();
        runTriggersAndWait();

        // THEN
        assertThat(asserts.getCount("hallo")).isEqualTo(3);
        assertThat(triggerService.countTriggers(TriggerStatus.FAILED)).isOne();
        // AND
        var trigger = triggerService.get(id).get();
        assertThat(trigger.getData().getExecutionCount()).isEqualTo(3);
        assertThat(trigger.getData().getExceptionName()).isEqualTo(RuntimeException.class.getName());
        assertThat(trigger.getData().getLastException()).contains("NOPE!");
    }

    @Test
    void testLockTriggerInSchedulers() throws Exception {
        // GIVEN
        final TaskId<String> taskId = taskService.<String>replace("multi-threading", s -> asserts.info(s));
        for (int i = 1; i <= 100; ++i) {
            triggerService.trigger(taskId.newUniqueTrigger("t" + i));
        }

        // WHEN
        ArrayList<Callable<?>> lockInvocations = new ArrayList<>();
        for (int i = 1; i <= 100; ++i) {
            lockInvocations.add(() -> runNextTrigger());
        }

        while (triggerService.hasPendingTriggers()) {
            schedulerService.triggerNextTasks();
            schedulerB.triggerNextTasks();
            Thread.sleep(10);
        }

        // THEN
        for (int i = 1; i <= 100; ++i) {
            asserts.awaitValueOnce("t" + i);
        }
        assertThat(triggerService.countTriggers(TriggerStatus.SUCCESS)).isEqualTo(100);
    }
}
