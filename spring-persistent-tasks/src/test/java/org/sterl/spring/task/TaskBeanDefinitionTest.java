package org.sterl.spring.task;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.sterl.spring.task.api.TaskId;
import org.sterl.spring.task.api.TaskId.TaskTriggerBuilder;
import org.sterl.spring.task.repository.TaskRepository;
import org.sterl.test.AsyncAsserts;

@Import(TaskBeanDefinitionConfig.class)
class TaskBeanDefinitionTest extends AbstractSpringTest {
    
    @Autowired private AsyncAsserts asserts;
    @Autowired private TaskSchedulerService subject;
    @Autowired private TaskRepository taskRepository;

    // ensure task in the spring context 
    @Autowired private TaskId<String> task1Id;
    @Autowired private TaskId<String> task2Id;
    
    @BeforeEach
    void setup() {
        asserts.clear();
    }
    
    @Test
    void testTaskId() {
        assertThat(task1Id.name()).isEqualTo("task1");
        assertThat(task2Id.name()).isEqualTo("task2");
    }
    
    @Test
    void testTriggerChainTask() throws Exception {
        // GIVEN
        final var trigger = task1Id.newTrigger().state("aa").build();
        System.err.println(task1Id);
        
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
