package org.sterl.spring.task;

import org.springframework.context.annotation.Bean;
import org.sterl.spring.task.api.AbstractTask;
import org.sterl.spring.task.api.SimpleTask;
import org.sterl.spring.task.api.Task;
import org.sterl.spring.task.api.TaskId.TaskTriggerBuilder;
import org.sterl.spring.task.api.TaskResult;
import org.sterl.test.AsyncAsserts;

public class TaskBeanDefinitionConfig {
    @Bean
    Task<String> task1(AsyncAsserts asserts) {
        return new AbstractTask<String>("task1") {
            @Override
            public TaskResult execute(String state) {
                asserts.info("task1::" + state);
                return TaskResult.of(TaskTriggerBuilder
                        .newTrigger("task2")
                        .state("task1::" + state)
                        .build());
            }
        };
    }
    @Bean
    Task<String> task2(AsyncAsserts asserts) {
        return new AbstractTask<String>("task2") {
            @Override
            public TaskResult execute(String state) {
                asserts.info("task2::" + state);
                return TaskResult.DONE;
            }
        };
    }
    @Bean("task3")
    SimpleTask<String> task3(AsyncAsserts asserts) {
        return s -> asserts.info("task3::" + s);
    }
}
