package org.sterl.spring.persistent_tasks.trigger;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.AbstractSpringTest.TaskConfig.Task3;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TriggerId;
import org.sterl.spring.persistent_tasks.api.TaskId.TaskTriggerBuilder;
import org.sterl.spring.persistent_tasks.task.repository.TaskRepository;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerStatus;

//@Import(TaskBeanDefinitionConfig.class)
class TriggerServiceTest extends AbstractSpringTest {
    
    @Autowired private TriggerService subject;
    @Autowired private TaskRepository taskRepository;

    // ensure task in the spring context 
    @Autowired private TaskId<String> task1Id;
    @Autowired private TaskId<String> task2Id;
    @Autowired private TaskId<String> task3Id;
    
    @Test
    void testTaskId() {
        assertThat(task1Id.name()).isEqualTo("task1");
        assertThat(task2Id.name()).isEqualTo("task2");
        assertThat(task3Id.name()).isEqualTo("task3");
    }
    
    @Test
    void testAddTrigger() throws Exception {
        // GIVEN
        final var triggerTime = OffsetDateTime.now().minusMinutes(1);
        final var trigger = task1Id.newTrigger()
                    .triggerTime(triggerTime)
                    .build();
        
        // WHEN
        final var triggerId = subject.trigger(trigger);
        
        // THEN
        final var e = subject.get(triggerId);
        assertThat(e).isPresent();
        assertThat(e.get().getData().getTriggerTime()).isEqualTo(triggerTime);
        assertThat(e.get().getData().getCreated()).isNotNull();
        assertThat(e.get().getData().getStart()).isNull();
        assertThat(e.get().getData().getEnd()).isNull();
        assertThat(e.get().getData().getExecutionCount()).isZero();
    }
    
    @Test
    void canCreateAnTrigger() {
        // GIVEN
        TaskId<String> taskId = taskService.replace("foo", c -> asserts.info("foo"));
        taskService.<String>replace("bar", c -> asserts.info("bar"));

        // WHEN
        subject.trigger(taskId.newTrigger().build());
        subject.trigger(taskId.newTrigger().build());

        // THEN
        assertThat(subject.countTriggers(taskId)).isEqualTo(2);
    }
    
    @Test
    void testTriggerSpringSimpleTask() throws Exception {
        // GIVEN
        final var trigger = TaskTriggerBuilder.newTrigger(Task3.NAME).state("trigger3").build();
        
        // WHEN
        var id = subject.trigger(trigger);
        subject.run(subject.get(id).get());
        
        // THEN
        assertThat(taskRepository.contains(Task3.NAME)).isTrue();
        asserts.awaitValue(Task3.NAME + "::trigger3");
    }
    
    @Test
    void runSimpleTaskTest() throws Exception {
        // GIVEN
        TaskId<String> taskId = taskService.replace("foo", c -> asserts.info("foo"));
        taskService.<String>replace("bar", c -> asserts.info("bar"));
        TriggerId triggerId = subject.trigger(taskId.newTrigger().build());

        // WHEN
        subject.run(triggerId);

        // THEN
        assertThat(subject.countTriggers(TriggerStatus.SUCCESS)).isOne();
        assertThat(subject.get(triggerId).get().getData().getExecutionCount()).isEqualTo(1);
        asserts.assertValue("foo");
        asserts.assertMissing("bar");
    }
    
    @Test
    void testTriggerChainTask() throws Exception {
        // GIVEN
        final var trigger = task1Id.newTrigger().state("aa").build();
        
        // WHEN
        final var triggerId = subject.trigger(trigger);
        subject.run(subject.lockNextTrigger());
        subject.run(subject.lockNextTrigger());
        
        // THEN
        // AND
        asserts.awaitOrdered("task1::aa", "task2::task1::aa");
        // AND
        final var e = subject.get(triggerId);
        assertThat(e).isPresent();
        assertThat(e.get().getData().getCreated()).isNotNull();
        assertThat(e.get().getData().getStart()).isNotNull();
        assertThat(e.get().getData().getEnd()).isNotNull();
        assertThat(e.get().getData().getExecutionCount()).isOne();
    }
}
