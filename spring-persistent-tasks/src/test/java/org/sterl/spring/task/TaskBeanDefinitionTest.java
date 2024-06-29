package org.sterl.spring.task;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.sterl.spring.task.api.Task;
import org.sterl.spring.task.api.TaskId.TaskTriggerBuilder;
import org.sterl.spring.task.repository.TaskRepository;
import org.sterl.test.AsyncAsserts;

@Import(TaskBeanDefinitionConfig.class)
class TaskBeanDefinitionTest extends AbstractSpringTest {
    
    @Autowired private AsyncAsserts asserts;
    @Autowired private Task<String> task1;
    @Autowired private TaskSchedulerService subject;
    @Autowired private TaskRepository taskRepository;

    @BeforeEach
    void setup() {
        asserts.clear();
    }
    
    @Test
    void testTriggerChainTask() throws Exception {
        // GIVEN
        final var trigger = task1.newTrigger().state("aa").build();
        
        // WHEN
        subject.trigger(trigger);
        subject.triggerNextTask().get();
        subject.triggerNextTask().get();
        
        // THEN
        asserts.awaitOrdered("task1::aa", "task2::task1::aa");
    }
    
    @Test
    void testTriggerSpringSimpleTask() throws Exception {
        // GIVEN
        final var trigger = TaskTriggerBuilder.newTrigger("task3").state("trigger3").build();
        
        // WHEN
        subject.trigger(trigger);
        subject.triggerNextTask().get();
        
        // THEN
        assertThat(taskRepository.contains("task3")).isTrue();
        asserts.awaitValue("task3::trigger3");
    }

}
