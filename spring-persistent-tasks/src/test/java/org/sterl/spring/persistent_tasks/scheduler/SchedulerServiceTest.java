package org.sterl.spring.persistent_tasks.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TaskId.TaskTriggerBuilder;
import org.sterl.spring.persistent_tasks.api.TriggerId;
import org.sterl.spring.persistent_tasks.scheduler.entity.SchedulerEntity;
import org.sterl.spring.persistent_tasks.scheduler.entity.SchedulerEntity.TaskSchedulerStatus;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;

class SchedulerServiceTest extends AbstractSpringTest {

    private SchedulerService subject;

    @BeforeEach
    public void beforeEach() throws Exception {
        super.beforeEach();
        subject = schedulerService;
    }

    @Test
    void schedulerShouldBeOnlineTest() {
        // GIVEN

        // WHEN spring started
        final SchedulerEntity status = subject.getStatus();

        // THEN
        assertThat(status.getStatus()).isEqualTo(TaskSchedulerStatus.ONLINE);
    }
    
    @Test
    void testWillTriggerOnlyFreeThreadSize() throws Exception {
        // GIVEN
        for (int i = 0; i < 15; i++) {
            triggerService.queue(TaskTriggerBuilder
                    .newTrigger("slowTask")
                    .state(50L)
                    .build()
                );
        }

        // WHEN
        subject.triggerNextTasks();

        // THEN
        Thread.sleep(15);
        assertThat(triggerService.countTriggers(TriggerStatus.RUNNING)).isEqualTo(10);
        assertThat(triggerService.countTriggers(TriggerStatus.NEW)).isEqualTo(5);
        // AND
        final SchedulerEntity scheduler = subject.getScheduler();
        assertThat(scheduler.getRunnungTasks()).isEqualTo(10);
    }
    
    @Test
    void verifyRunningStatusTest() throws Exception {
        // GIVEN
        final TriggerId triggerId = triggerService.queue(TaskTriggerBuilder
                .newTrigger("slowTask")
                .state(50L)
                .build()
            );

        // WHEN
        final Future<TriggerId> running = subject.triggerNextTasks().get(0);

        // THEN
        Thread.sleep(40);
        var runningTrigger = triggerService.get(triggerId).get();
        assertThat(runningTrigger.getData().getStatus()).isEqualTo(TriggerStatus.RUNNING);
        // AND
        running.get();
        assertThat(triggerService.get(triggerId)).isEmpty();
        // AND
        var history = historyService.findLastKnownStatus(triggerId).get();
        assertThat(history.getData().getStatus()).isEqualTo(TriggerStatus.SUCCESS);
    }

    @Test
    void runSimpleTaskMultipleTimesTest() throws Exception {
        // GIVEN
        TaskId<String> taskId = taskService.replace("foo", c -> asserts.info(c));
        for (int i = 1; i < 5; ++i) {
            subject.queue(taskId, i + " state");
        }

        // WHEN
        for (int i = 1; i < 5; ++i) {
            subject.triggerNextTasks();
        }

        // THEN
        for (int i = 1; i < 5; ++i) {
            asserts.awaitValue(i + " state");
        }

        assertThat(triggerService.hasPendingTriggers()).isFalse();
        assertThat(historyService.countTriggers(TriggerStatus.SUCCESS)).isEqualTo(4);
    }
}
