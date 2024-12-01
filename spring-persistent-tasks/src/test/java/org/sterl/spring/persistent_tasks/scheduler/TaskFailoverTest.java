package org.sterl.spring.persistent_tasks.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.TaskId.TaskTriggerBuilder;
import org.sterl.spring.persistent_tasks.scheduler.SchedulerService;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerStatus;
import org.sterl.spring.persistent_tasks.trigger.repository.TriggerRepository;

class TaskFailoverTest extends AbstractSpringTest {

    private SchedulerService schedulerA = schedulerService;
    
    @Test
    void nameTest() throws Exception {
        assertThat(schedulerA.getName()).isEqualTo("schedulerA");
        assertThat(schedulerB.getName()).isEqualTo("schedulerB");
    }

    @Test
    void rescheduleAbandonedTasksTest() throws Exception {
        // GIVEN
        final var trigger = TaskTriggerBuilder.newTrigger("slowTask").state(1000L).build();
        final var id = schedulerA.trigger(trigger);
        
        // WHEN
        schedulerA.runOrQueue(trigger);
        schedulerA.stop();
        schedulerB.triggerNextTask().get();
        
        Thread.sleep(60);
        // AND check status
        trx.executeWithoutResult(t -> {
            final var triggerEntity = triggerRepository.findById(id);
            assertThat(triggerEntity).isPresent();
            assertThat(triggerEntity.get().getData().getEnd()).isNull();
            assertThat(triggerEntity.get().getData().getStatus()).isEqualTo(TriggerStatus.RUNNING);
        });
        // AND re-run abandoned tasks
        schedulerB.pingRegistry();
        final var tasks = schedulerB.rescheduleAbandonedTasks(Duration.ofMillis(49));
        
        // THEN
        assertThat(tasks).hasSize(1);
    }

}
