package org.sterl.spring.persistent_tasks.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.TriggerId;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerStatus;
import org.sterl.spring.persistent_tasks.trigger.repository.TriggerRepository;

class TaskFailoverTest extends AbstractSpringTest {

    @Autowired TriggerRepository triggerRepository;
    private SchedulerService schedulerA;

    @BeforeEach
    void setup() {
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
        var trigger = triggerRepository.save(TriggerEntity.builder()
                .id(new TriggerId("slowTask"))
                .build()
                .runOn("schedulerB"));

        // WHEN
        Thread.sleep(19);
        // AND add one which looks like it is running :-)
        triggerRepository.save(TriggerEntity.builder()
                .id(new TriggerId("slowTask"))
                .build()
                .runOn("schedulerA"));

        // AND check status
        Optional<TriggerEntity> state = triggerService.get(trigger.getId());
        assertThat(state).isPresent();
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
        state = triggerService.get(trigger.getId());
        assertThat(state.get().getData().getEnd()).isNotNull();
        assertThat(state.get().getData().getStart()).isAfter(retryTime);
        // AND execution duration should be reflected
        assertThat(state.get().getData().getEnd()).isAfter(state.get().getData().getStart());
        assertThat(state.get().getData().getStatus()).isEqualTo(TriggerStatus.SUCCESS);
    }

}
