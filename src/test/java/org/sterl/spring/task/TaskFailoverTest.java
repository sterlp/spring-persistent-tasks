package org.sterl.spring.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.UnknownHostException;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.task.api.SimpleTask;
import org.sterl.spring.task.api.TaskId.TaskTriggerBuilder;
import org.sterl.spring.task.component.EditSchedulerStatusComponent;
import org.sterl.spring.task.component.EditTaskTriggerComponent;
import org.sterl.spring.task.component.LockNextTriggerComponent;
import org.sterl.spring.task.component.TransactionalTaskExecutorComponent;
import org.sterl.spring.task.model.TriggerStatus;
import org.sterl.spring.task.repository.TaskRepository;
import org.sterl.spring.task.repository.TaskSchedulerRepository;
import org.sterl.spring.task.repository.TriggerRepository;

@SpringBootTest
class TaskFailoverTest {
    
    @Autowired TransactionTemplate trx;
    @Autowired TriggerRepository triggerRepository;

    @TestConfiguration
    static class Config {
        @Bean
        AsyncAsserts asserts() {
            return new AsyncAsserts();
        }
        
        @Bean
        SimpleTask<Long> slowTask(AsyncAsserts asserts) {
            return s -> {
                try {
                    Thread.sleep(s.longValue());
                    asserts.info("Complete " + s);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    throw new RuntimeException("OH NO!");
                }
            };
        }
        
        @Bean(destroyMethod = "stop", initMethod = "start")
        TaskSchedulerService schedulerA(
                TaskSchedulerRepository schedulerRepository,
                TaskRepository taskRepository,
                LockNextTriggerComponent lockNextTrigger,
                EditTaskTriggerComponent editTasks,
                TransactionTemplate trx) throws UnknownHostException {

            final var taskExecutor = new TransactionalTaskExecutorComponent(taskRepository, editTasks, trx);
            taskExecutor.setMaxShutdownWaitTime(Duration.ofSeconds(0));

            return new TaskSchedulerService("schedulerA", lockNextTrigger, editTasks, 
                    new EditSchedulerStatusComponent(schedulerRepository, taskExecutor), 
                    taskRepository, 
                    taskExecutor);
        }
        @Bean(destroyMethod = "stop", initMethod = "start")
        TaskSchedulerService schedulerB(
                TaskSchedulerRepository schedulerRepository,
                TaskRepository taskRepository,
                LockNextTriggerComponent lockNextTrigger,
                EditTaskTriggerComponent editTasks,
                TransactionTemplate trx) throws UnknownHostException {

            final var taskExecutor = new TransactionalTaskExecutorComponent(taskRepository, editTasks, trx);
            taskExecutor.setMaxShutdownWaitTime(Duration.ofSeconds(0));

            return new TaskSchedulerService("schedulerB", lockNextTrigger, editTasks, 
                    new EditSchedulerStatusComponent(schedulerRepository, taskExecutor), 
                    taskRepository, 
                    taskExecutor);
        }
    }
    
    @Autowired TaskSchedulerService schedulerA;
    @Autowired TaskSchedulerService schedulerB;
    
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
            triggerRepository.findById(id).ifPresent(e -> e.setStatus(TriggerStatus.RUNNING));
        });
        // AND re-run abandoned tasks
        schedulerB.pingRegisgtry();
        final var tasks = schedulerB.rescheduleAbandonedTasks(Duration.ofMillis(49));
        
        // THEN
        assertThat(tasks).hasSize(1);
        
        System.err.println(schedulerB.get(id));
        System.err.println(schedulerA == schedulerB);
    }

}
