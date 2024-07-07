package org.sterl.spring.task;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.task.api.SpringBeanTask;
import org.sterl.spring.task.api.TaskId;
import org.sterl.spring.task.component.EditSchedulerStatusComponent;
import org.sterl.spring.task.component.EditTaskTriggerComponent;
import org.sterl.spring.task.component.LockNextTriggerComponent;
import org.sterl.spring.task.component.TransactionalTaskExecutorComponent;
import org.sterl.spring.task.model.RegisteredTask;
import org.sterl.spring.task.repository.TaskRepository;
import org.sterl.spring.task.repository.TaskSchedulerRepository;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableScheduling
@EnableAutoConfiguration
@ComponentScan
@Slf4j
public class TaskSchedulerConfig {

    @Primary
    @DependsOnDatabaseInitialization
    @Bean(destroyMethod = "stop", initMethod = "start")
    TaskSchedulerService taskSchedulerService(
            TaskRepository taskRepository,
            LockNextTriggerComponent lockNextTrigger,
            EditTaskTriggerComponent editTasks,
            TransactionalTaskExecutorComponent taskExecutor,
            EditSchedulerStatusComponent editSchedulerStatusComponent,
            TransactionTemplate trx) throws UnknownHostException {
        
        String name = null;
        if (name == null) {
            final var ip = InetAddress.getLocalHost();
            final var hostname = ip.getHostName();
            
            if (hostname == null) name = ip.toString();
            else name = hostname;
        }

        return new TaskSchedulerService(name, lockNextTrigger, editTasks,
                editSchedulerStatusComponent, taskRepository, taskExecutor, trx);
    }
    @Bean
    EditSchedulerStatusComponent editSchedulerStatusComponent(
            TaskSchedulerRepository schedulerRepository,
            TransactionalTaskExecutorComponent taskExecutor) throws UnknownHostException {

        return new EditSchedulerStatusComponent(schedulerRepository, taskExecutor);
    }

    @Autowired
    void configureSimpleTasks(GenericApplicationContext context,
            TaskRepository taskRepository) {
        final var simpleTasks = context.getBeansOfType(SpringBeanTask.class);
        for(Entry<String, SpringBeanTask> t : simpleTasks.entrySet()) {
            final var registeredTask = new RegisteredTask<>(t.getKey(), t.getValue());
            taskRepository.addTask(registeredTask);

            // register Key in Spring Context, if missing
            final var taskIdContextName = registeredTask.getId().name() + "Id";
            if (!context.containsBean(taskIdContextName)) {
                log.info("Adding TaskId={} with name={} to spring context", registeredTask.getId(), taskIdContextName);
                var beanDefinition = new GenericBeanDefinition();
                beanDefinition.setBeanClass(registeredTask.getId().getClass());
                context.registerBean(taskIdContextName, 
                        TaskId.class, () -> registeredTask.getId());
            }
        }
    }
}
