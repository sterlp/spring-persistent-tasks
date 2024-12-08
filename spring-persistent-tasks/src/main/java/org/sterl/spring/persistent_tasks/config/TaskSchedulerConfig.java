package org.sterl.spring.persistent_tasks.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.EnablePersistentTasks;
import org.sterl.spring.persistent_tasks.api.SpringBeanTask;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.scheduler.SchedulerService;
import org.sterl.spring.persistent_tasks.scheduler.component.EditSchedulerStatusComponent;
import org.sterl.spring.persistent_tasks.scheduler.component.TaskExecutorComponent;
import org.sterl.spring.persistent_tasks.scheduler.config.ConditionalSchedulerServiceByProperty;
import org.sterl.spring.persistent_tasks.task.model.RegisteredTask;
import org.sterl.spring.persistent_tasks.task.repository.TaskRepository;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableScheduling
@AutoConfigurationPackage(basePackageClasses = EnablePersistentTasks.class)
@ComponentScan(basePackageClasses = EnablePersistentTasks.class)
@Slf4j
public class TaskSchedulerConfig {

    @ConditionalSchedulerServiceByProperty
    @Primary
    @DependsOnDatabaseInitialization
    @Bean
    SchedulerService schedulerService(
            TriggerService triggerService,
            TaskExecutorComponent taskExecutor,
            EditSchedulerStatusComponent editSchedulerStatus,
            TransactionTemplate trx) throws UnknownHostException {

        String name = null;
        if (name == null) {
            final var ip = InetAddress.getLocalHost();
            final var hostname = ip.getHostName();

            if (hostname == null) {
                name = ip.toString();
            } else {
                name = hostname;
            }
        }
        return new SchedulerService(name, triggerService, taskExecutor, editSchedulerStatus, trx);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Autowired
    void configureSimpleTasks(GenericApplicationContext context,
            TaskRepository taskRepository) {
        final var simpleTasks = context.getBeansOfType(SpringBeanTask.class);
        for(Entry<String, SpringBeanTask> t : simpleTasks.entrySet()) {
            final var registeredTask = new RegisteredTask<>(t.getKey(), t.getValue());
            taskRepository.addTask(registeredTask);

            addTaskIdIfMissing(context, registeredTask);
        }
    }
    private void addTaskIdIfMissing(GenericApplicationContext context, final RegisteredTask<?> registeredTask) {
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
