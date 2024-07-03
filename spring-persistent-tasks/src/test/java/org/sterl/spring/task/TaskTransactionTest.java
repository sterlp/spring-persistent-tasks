package org.sterl.spring.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.sterl.spring.sample_app.person.PersonRepository;
import org.sterl.spring.task.api.TaskId.TaskTriggerBuilder;

@Import(TaskTransactionConfig.class)
class TaskTransactionTest extends AbstractSpringTest {

    @Autowired private TaskSchedulerService subject;
    @Autowired private AtomicBoolean sendError;
    @Autowired private PersonRepository personRepository;
    
    @BeforeEach
    void setup() {
        personRepository.deleteAllInBatch();
        sendError.set(false);
    }
    
    @Test
    void testSaveEntity() throws Exception {
        // GIVEN
        final var trigger = TaskTriggerBuilder.newTrigger("savePerson").state("Paul").build();
        
        // WHEN
        subject.trigger(trigger);
        subject.triggerNextTask().get();
        
        // THEN
        assertThat(personRepository.count()).isOne();
    }
    
    @Test
    void testRollbackAndRetry() throws Exception {
        // GIVEN
        final var trigger = TaskTriggerBuilder.newTrigger("savePerson").state("Paul").build();
        sendError.set(true);
        // WHEN
        final var triggerId = subject.trigger(trigger);
        subject.triggerNextTask().get();
        // THEN
        assertThat(personRepository.count()).isZero();

        // WHEN
        sendError.set(false);
        subject.triggerNextTask().get();
        // THEN
        assertThat(personRepository.count()).isOne();
        assertThat(subject.get(triggerId).get().getExecutionCount()).isEqualTo(2);
    }

}
