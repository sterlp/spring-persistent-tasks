package org.sterl.spring.persistent_tasks.task.config;

import java.io.Serializable;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.sterl.spring.persistent_tasks.api.PersistentTask;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.task.TaskService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class TaskConfig {
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Autowired
    void configureSimpleTasks(GenericApplicationContext context,
            TaskService taskService) {
        final var simpleTasks = context.getBeansOfType(PersistentTask.class);
        for(Entry<String, PersistentTask> t : simpleTasks.entrySet()) {
            var id = taskService.register(t.getKey(), t.getValue());

            addTaskIdIfMissing(context, id, t.getValue());
        }
    }
    private void addTaskIdIfMissing(GenericApplicationContext context, 
            TaskId<Serializable> id, PersistentTask<?> task) {
        final var taskIdContextName = id.name() + "Id";
        if (!context.containsBean(taskIdContextName)) {
            log.info("Adding {} with name={} to spring context", id, taskIdContextName);
            context.registerBean(taskIdContextName, TaskId.class, () -> id);
        }
    }
}
