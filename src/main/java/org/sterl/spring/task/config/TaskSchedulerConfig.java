package org.sterl.spring.task.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sterl.spring.task.TaskSchedulerService;
import org.sterl.spring.task.api.ClosureTask;
import org.sterl.spring.task.api.SimpleTask;
import org.sterl.spring.task.component.EditTaskInstanceComponent;
import org.sterl.spring.task.component.LockNextTriggerComponent;
import org.sterl.spring.task.component.TransactionalTaskExecutorComponent;
import org.sterl.spring.task.repository.TaskRepository;

@Configuration
public class TaskSchedulerConfig {
    @Bean
    TaskSchedulerService taskSchedulerService(
            TaskRepository taskRepository,
            LockNextTriggerComponent lockNextTrigger,
            EditTaskInstanceComponent editTasks,
            TransactionalTaskExecutorComponent taskExecutor) throws UnknownHostException {
        String name = null;
        if (name == null) {
            final var ip = InetAddress.getLocalHost();
            final var hostname = ip.getHostName();
            
            if (hostname == null) name = ip.toString();
            else name = hostname;
        }
        return new TaskSchedulerService(name, lockNextTrigger, editTasks, taskRepository, taskExecutor);
    }

    @Autowired
    void configureSimpleTasks(AnnotationConfigApplicationContext context,
            TaskRepository taskRepository) {
        final var simpleTasks = context.getBeansOfType(SimpleTask.class);
        for(Entry<String, SimpleTask> t : simpleTasks.entrySet()) {
            taskRepository.addTask(new ClosureTask<>(t.getKey(), t.getValue()));
        }
    }
}
