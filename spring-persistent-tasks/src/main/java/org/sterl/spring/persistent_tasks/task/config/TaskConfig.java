package org.sterl.spring.persistent_tasks.task.config;

import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.sterl.spring.persistent_tasks.api.SpringBeanTask;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.task.model.RegisteredTask;
import org.sterl.spring.persistent_tasks.task.repository.TaskRepository;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class TaskConfig {
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
