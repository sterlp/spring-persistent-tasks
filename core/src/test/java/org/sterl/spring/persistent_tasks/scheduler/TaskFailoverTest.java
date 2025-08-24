package org.sterl.spring.persistent_tasks.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;

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

        var running = persistentTaskTestService.scheduleNextTriggers();
        assertThat(running.size()).isEqualTo(1);
        // AND we wait a bit
        Thread.sleep(250);
        final var timeout = OffsetDateTime.now();

        triggerService.queue(slowTaskId.newTrigger(20000L).build());
        running = persistentTaskTestService.scheduleNextTriggers();
        assertThat(running.size()).isEqualTo(1);
        // AND
        assertThat(triggerService.countTriggers(TriggerStatus.RUNNING))
            .isEqualTo(2);
        
        // WHEN
        final var tasks = schedulerB.rescheduleAbandonedTriggers(timeout);

        // THEN
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getId()).isEqualTo(willTimeout.getId());
        // AND
        assertThat(triggerService.countTriggers(TriggerStatus.RUNNING))
            .isEqualTo(1);
        assertThat(triggerService.countTriggers(TriggerStatus.WAITING))
            .isEqualTo(1);
        // AND
        var timeoutTrigger = historyService.findAllDetailsForInstance(willTimeout.getId(),
                Pageable.ofSize(10)).getContent().getFirst();
        assertThat(timeoutTrigger.getStatus()).isEqualTo(TriggerStatus.FAILED);
        assertThat(timeoutTrigger.getMessage()).contains("Trigger abandoned");
    }
}
