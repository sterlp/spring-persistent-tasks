package org.sterl.spring.task;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.sterl.spring.task.api.SpringBeanTask;
import org.sterl.spring.task.event.TriggerTaskEvent;
import org.sterl.test.AsyncAsserts;

public class TaskBeanDefinitionConfig {
    @Bean
    SpringBeanTask<String> task1(ApplicationEventPublisher publisher, AsyncAsserts asserts) {
    return (String state) -> {
            asserts.info("task1::" + state);
            publisher.publishEvent(TriggerTaskEvent.of("task2", "task1::" + state));
        };
    }

    @Bean
    SpringBeanTask<String> task2(AsyncAsserts asserts) {
        return new SpringBeanTask<String>() {
            @Override
            public void accept(String state) {
                asserts.info("task2::" + state);
            }
        };
    }
    @Bean("task3")
    SpringBeanTask<String> task3(AsyncAsserts asserts) {
        return s -> asserts.info("task3::" + s);
    }
}
