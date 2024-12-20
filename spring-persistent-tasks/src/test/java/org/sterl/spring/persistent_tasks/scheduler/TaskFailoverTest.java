package org.sterl.spring.persistent_tasks.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.TriggerId;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;
import org.sterl.spring.persistent_tasks.trigger.repository.TriggerRepository;

class TaskFailoverTest extends AbstractSpringTest {

    @Autowired private TriggerRepository triggerRepository;
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
        var trigger = triggerRepository.save(
                new TriggerEntity(new TriggerId("slowTask"))
                .runOn("schedulerB"));

        // WHEN
        Thread.sleep(19);
        // AND add one which looks like it is running :-)
        triggerRepository.save(new TriggerEntity(new TriggerId("slowTask"))
                .runOn("schedulerA"));

        // AND check status
        var state = triggerService.get(trigger.getKey());
        assertThat(state.get().getData().getStatus()).isEqualTo(TriggerStatus.RUNNING);
        assertThat(state.get().getRunningOn()).isEqualTo("schedulerB");
        assertThat(state.get().getData().getEnd()).isNull();
        // AND re-run abandoned tasks
        schedulerA.pingRegistry();
        final var tasks = schedulerA.rescheduleAbandonedTasks(Duration.ofMillis(20));

        // THEN
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getId()).isEqualTo(trigger.getId());

        // WHEN
        final var retryTime = OffsetDateTime.now();
        final List<Future<TriggerId>> runWaitingTasks = schedulerService.triggerNextTasks();

        // THEN
        assertThat(runWaitingTasks).hasSize(1);
        runWaitingTasks.get(0).get();
        assertThat(tasks.get(0).getId()).isEqualTo(trigger.getId());
        
        // AND date should be set after the retry
        var data = historyService.findLastKnownStatus(trigger.getKey()).get().getData();
        assertThat(data.getEnd()).isNotNull();
        assertThat(data.getStart()).isAfter(retryTime);
        // AND execution duration should be reflected
        assertThat(data.getEnd()).isAfter(state.get().getData().getStart());
        assertThat(data.getStatus()).isEqualTo(TriggerStatus.SUCCESS);
    }

}
