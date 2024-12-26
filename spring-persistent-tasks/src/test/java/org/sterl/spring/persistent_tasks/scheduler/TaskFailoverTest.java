package org.sterl.spring.persistent_tasks.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;

class TaskFailoverTest extends AbstractSpringTest {

    @Autowired 
    private TaskId<Long> slowTaskId;
    private SchedulerService schedulerA;

    @BeforeEach
    public void beforeEach() throws Exception {
        super.beforeEach();
        this.schedulerA = schedulerService;
    }

    @Test
    void nameTest() throws Exception {
        assertThat(schedulerA.getName()).isEqualTo("schedulerA");
        assertThat(schedulerB.getName()).isEqualTo("schedulerB");
    }

    @Test
    void rescheduleAbandonedTasksTest() throws Exception {
        // GIVEN
        schedulerA.setMaxThreads(1);
        schedulerB.setMaxThreads(1);
        var willTimeout = triggerService.queue(slowTaskId.newTrigger(20000L).build());

        var running = runTriggers();
        assertThat(running.size()).isEqualTo(1);
        // AND we wait a bit
        Thread.sleep(250);
        final var timeout = OffsetDateTime.now();

        triggerService.queue(slowTaskId.newTrigger(20000L).build());
        running = runTriggers();
        assertThat(running.size()).isEqualTo(1);
        // AND
        assertThat(triggerService.countTriggers(TriggerStatus.RUNNING))
            .isEqualTo(2);
        
        // WHEN
        final var tasks = schedulerB.rescheduleAbandonedTasks(timeout);

        // THEN
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getId()).isEqualTo(willTimeout.getId());
        // AND
        assertThat(triggerService.countTriggers(TriggerStatus.RUNNING))
            .isEqualTo(1);
        assertThat(triggerService.countTriggers(TriggerStatus.NEW))
            .isEqualTo(1);
    }
}
