package org.sterl.spring.persistent_tasks.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.scheduler.entity.SchedulerEntity;
import org.sterl.spring.persistent_tasks.scheduler.entity.SchedulerEntity.TaskSchedulerStatus;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerStatus;

class SchedulerServiceTest extends AbstractSpringTest {

    private SchedulerService subject;

    @BeforeEach
    void before() throws Exception {
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

        assertThat(triggerService.countTriggers(TriggerStatus.SUCCESS)).isEqualTo(4);
        assertThat(triggerService.hasPendingTriggers()).isFalse();
    }
}
