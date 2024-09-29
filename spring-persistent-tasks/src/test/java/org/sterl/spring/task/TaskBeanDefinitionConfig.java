package org.sterl.spring.task;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.sterl.spring.task.api.SpringBeanTask;
import org.sterl.spring.task.api.event.TriggerTaskEvent;
import org.sterl.test.AsyncAsserts;

import lombok.RequiredArgsConstructor;

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

    @Component(Task3.NAME)
    @RequiredArgsConstructor
    public static class Task3 implements SpringBeanTask<String> {
        public static final String NAME = "task3";
        private final AsyncAsserts asserts;
        @Override
        public void accept(String state) {
            asserts.info(NAME + "::" + state);
        }
    }
}
