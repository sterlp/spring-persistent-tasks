package org.sterl.spring.task;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.sterl.spring.task.api.AbstractTask;
import org.sterl.spring.task.api.SimpleTask;
import org.sterl.spring.task.api.Task;
import org.sterl.spring.task.api.TaskId.TaskTriggerBuilder;
import org.sterl.spring.task.api.TaskResult;
import org.sterl.spring.task.repository.TaskRepository;

@SpringBootTest
class TaskBeanDefinitionTest {
    
    @TestConfiguration
    static class TaskConfig {
        @Bean
        AsyncAsserts asserts() {
            return new AsyncAsserts();
        }
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
    
    @Autowired AsyncAsserts asserts;
    @Autowired Task<String> task1;
    @Autowired Task<String> task2;
    @Autowired TaskSchedulerService subject;
    @Autowired TaskRepository taskRepository;

    @BeforeEach
    void setup() {
        asserts.clear();
    }
    
    @Test
    void testTriggerChainTask() throws Exception {
        // GIVEN
        final var trigger = TaskTriggerBuilder.newTrigger("task1").state("aa").build();
        
        // WHEN
        subject.trigger(trigger);
        subject.triggerNexTask().get();
        subject.triggerNexTask().get();
        
        // THEN
        asserts.awaitOrdered("task1::aa", "task2::task1::aa");
    }
    
    @Test
    void testTriggerSpringSimpleTask() throws Exception {
        // GIVEN
        final var trigger = TaskTriggerBuilder.newTrigger("task3").state("trigger3").build();
        
        // WHEN
        subject.trigger(trigger);
        subject.triggerNexTask().get();
        
        // THEN
        assertThat(taskRepository.contains("task3")).isTrue();
        asserts.awaitValue("task3::trigger3");
    }

}
