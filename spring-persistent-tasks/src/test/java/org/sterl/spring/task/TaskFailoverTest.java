package org.sterl.spring.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.task.api.TaskId.TaskTriggerBuilder;
import org.sterl.spring.task.model.TriggerStatus;
import org.sterl.spring.task.repository.TriggerRepository;

@Import(TaskFailoverConfig.class)
class TaskFailoverTest extends AbstractSpringTest {

    @Autowired private TriggerRepository triggerRepository;
    @Autowired private TransactionTemplate trx;

    @Autowired @Qualifier("schedulerA") private TaskSchedulerService schedulerA;
    @Autowired @Qualifier("schedulerB") private TaskSchedulerService schedulerB;
    
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
        schedulerA.triggerNextTask();
        schedulerA.stop();
        schedulerB.triggerNextTask().get();
        
        Thread.sleep(60);
        // AND simulate the scheduler died
        trx.executeWithoutResult(t -> {
            assertThat(triggerRepository.findById(id)).isPresent();
            triggerRepository.findById(id).ifPresent(e -> e.setStatus(TriggerStatus.OPEN));
        });
        // AND re-run abandoned tasks
        schedulerB.pingRegistry();
        final var tasks = schedulerB.rescheduleAbandonedTasks(Duration.ofMillis(49));
        
        // THEN
        assertThat(tasks).hasSize(1);
    }

}
