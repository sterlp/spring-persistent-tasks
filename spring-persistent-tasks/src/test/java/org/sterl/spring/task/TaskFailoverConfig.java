package org.sterl.spring.task;

import java.net.UnknownHostException;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.task.api.SpringBeanTask;
import org.sterl.spring.task.component.EditSchedulerStatusComponent;
import org.sterl.spring.task.component.EditTaskTriggerComponent;
import org.sterl.spring.task.component.LockNextTriggerComponent;
import org.sterl.spring.task.component.ReadTriggerComponent;
import org.sterl.spring.task.component.TaskExecutorComponent;
import org.sterl.spring.task.repository.TaskRepository;
import org.sterl.spring.task.repository.TaskSchedulerRepository;
import org.sterl.test.AsyncAsserts;

public class TaskFailoverConfig {
    @Bean
    SpringBeanTask<Long> slowTask(AsyncAsserts asserts) {
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
            ReadTriggerComponent readTriggerComponent,
            LockNextTriggerComponent lockNextTrigger,
            EditTaskTriggerComponent editTasks,
            TransactionTemplate trx) throws UnknownHostException {

        final var taskExecutor = new TaskExecutorComponent(taskRepository, editTasks);
        taskExecutor.setMaxShutdownWaitTime(Duration.ofSeconds(0));
        
        return new TaskSchedulerService("schedulerA", readTriggerComponent, lockNextTrigger, editTasks, 
                new EditSchedulerStatusComponent(schedulerRepository, taskExecutor), 
                taskRepository, 
                taskExecutor,
                trx);
    }
    @Bean(destroyMethod = "stop", initMethod = "start")
    TaskSchedulerService schedulerB(
            TaskSchedulerRepository schedulerRepository,
            TaskRepository taskRepository,
            ReadTriggerComponent readTriggerComponent,
            LockNextTriggerComponent lockNextTrigger,
            EditTaskTriggerComponent editTasks,
            TransactionTemplate trx) throws UnknownHostException {

        final var taskExecutor = new TaskExecutorComponent(taskRepository, editTasks);
        taskExecutor.setMaxShutdownWaitTime(Duration.ofSeconds(0));
        
        return new TaskSchedulerService("schedulerB", readTriggerComponent, lockNextTrigger, editTasks, 
                new EditSchedulerStatusComponent(schedulerRepository, taskExecutor), 
                taskRepository, 
                taskExecutor,
                trx);
    }
}
