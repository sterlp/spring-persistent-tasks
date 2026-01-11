package org.sterl.spring.persistent_tasks.task.config;

import java.io.Serializable;
import java.time.Duration;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.sterl.spring.persistent_tasks.api.CronTriggerBuilder;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.task.PersistentTask;
import org.sterl.spring.persistent_tasks.task.TaskService;
import org.sterl.spring.persistent_tasks.trigger.CronTrigger;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class TaskConfig {

    private final TriggerService triggerService;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Autowired
    void configureSimpleTasks(GenericApplicationContext context,
            TaskService taskService) {
        final var simpleTasks = context.getBeansOfType(PersistentTask.class);
        for(Entry<String, PersistentTask> t : simpleTasks.entrySet()) {
            var id = taskService.register(t.getKey(), t.getValue());

            addTaskIdIfMissing(context, id);
            registerCronTriggerIfAnnotated(t.getValue(), id);
        }
    }

    @SuppressWarnings("rawtypes")
    private void registerCronTriggerIfAnnotated(PersistentTask task, TaskId<?> taskId) {
        var cronAnnotation = AnnotationUtils.findAnnotation(task.getClass(),
                CronTrigger.class);

        if (cronAnnotation == null) return;

        CronTriggerBuilder<?> builder = taskId.newCron();

        // Set ID if provided
        if (cronAnnotation.id() != null && !cronAnnotation.id().isBlank()) {
            builder.id(cronAnnotation.id());
        }

        // Set cron expression or fixed delay
        if (cronAnnotation.cron() != null && !cronAnnotation.cron().isBlank()) {
            builder.cron(cronAnnotation.cron());
        } else if (cronAnnotation.fixedDelay() > 0) {
            long millis = cronAnnotation.timeUnit().toMillis(cronAnnotation.fixedDelay());
            builder.after(Duration.ofMillis(millis));
        } else {
            throw new IllegalArgumentException("@CronTrigger on "
                    + taskId + " has neither cron nor fixedDelay configured");
        }

        var cronTrigger = builder.build();
        triggerService.register(cronTrigger);
    }
    private void addTaskIdIfMissing(GenericApplicationContext context, 
            TaskId<Serializable> id) {
        final var taskIdContextName = id.name() + "Id";
        if (!context.containsBean(taskIdContextName)) {
            log.info("Adding {} with name={} to spring context", id, taskIdContextName);
            context.registerBean(taskIdContextName, TaskId.class, () -> id);
        }
    }
}
