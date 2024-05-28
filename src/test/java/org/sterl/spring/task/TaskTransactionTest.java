package org.sterl.spring.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.sterl.spring.task.api.AbstractTask;
import org.sterl.spring.task.api.RetryStrategy;
import org.sterl.spring.task.api.Task;
import org.sterl.spring.task.api.TaskId.TaskTriggerBuilder;
import org.sterl.spring.task.api.TaskResult;
import org.sterl.spring.task.person.PersonBE;
import org.sterl.spring.task.person.PersonRepository;

@SpringBootTest
class TaskTransactionTest {

    @TestConfiguration
    static class Config {
        @Bean
        AtomicBoolean sendError() {
            return new AtomicBoolean(false);
        }
        @Bean
        Task<String> savePerson(PersonRepository personRepository, AtomicBoolean sendError) {
            return new AbstractTask<String>("savePerson") {
                @Override
                public TaskResult execute(String name) {
                    personRepository.save(new PersonBE(name));
                    if (sendError.get()) throw new RuntimeException("Error requested for " + name);
                    return TaskResult.DONE;
                }
                public RetryStrategy retryStrategy() {
                    return RetryStrategy.TRY_THREE_TIMES_IMMEDIATELY;
                }
            };
        }
    }
    
    @Autowired TaskSchedulerService subject;
    @Autowired AtomicBoolean sendError;
    @Autowired PersonRepository personRepository;
    
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
        subject.triggerNexTask().get();
        
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
        subject.triggerNexTask().get();
        // THEN
        assertThat(personRepository.count()).isZero();

        // WHEN
        sendError.set(false);
        subject.triggerNexTask().get();
        // THEN
        assertThat(personRepository.count()).isOne();
        assertThat(subject.get(triggerId).get().getExecutionCount()).isEqualTo(2);
    }

}
