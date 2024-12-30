package org.sterl.spring.persistent_tasks.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.RetryStrategy;
import org.sterl.spring.persistent_tasks.api.SpringBeanTask;
import org.sterl.spring.persistent_tasks.api.TaskId.TaskTriggerBuilder;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;
import org.sterl.spring.sample_app.person.PersonBE;
import org.sterl.spring.sample_app.person.PersonRepository;

class SchedulerServiceTransactionTest extends AbstractSpringTest {

    private SchedulerService subject;
    @Autowired private AtomicBoolean sendError;
    @Autowired private PersonRepository personRepository;

    @Configuration
    static class Config {
        @Bean
        AtomicBoolean sendError() {
            return new AtomicBoolean(false);
        }
        @Bean
        SpringBeanTask<String> savePerson(PersonRepository personRepository, AtomicBoolean sendError) {
            return new SpringBeanTask<>() {
                @Transactional
                @Override
                public void accept(String name) {
                    personRepository.save(new PersonBE(name));
                    if (sendError.get()) {
                        throw new RuntimeException("Error requested for " + name);
                    }
                }
                public RetryStrategy retryStrategy() {
                    return RetryStrategy.THREE_RETRIES_IMMEDIATELY;
                }
            };
        }
    }

    @BeforeEach
    public void beforeEach() throws Exception {
        super.beforeEach();
        subject = schedulerService;
        personRepository.deleteAllInBatch();
        sendError.set(false);
    }

    @Test
    void testSaveEntity() throws Exception {
        // GIVEN
        final var trigger = TaskTriggerBuilder.newTrigger("savePerson").state("Paul").build();

        // WHEN
        subject.runOrQueue(trigger).get();

        // THEN
        assertThat(personRepository.count()).isOne();
    }

    @Test
    void testRollbackAndRetry() throws Exception {
        // GIVEN
        final var triggerRequest = TaskTriggerBuilder.newTrigger("savePerson").state("Paul").build();
        sendError.set(true);
        // WHEN
        var key = subject.runOrQueue(triggerRequest);
        // THEN
        key.get();
        assertThat(persistentTaskService.getLastTriggerData(key.get()).get().getStatus())
            .isEqualTo(TriggerStatus.WAITING);

        // WHEN
        sendError.set(false);
        var executed = persistentTaskService.executeTriggersAndWait();

        // THEN
        assertThat(executed).hasSize(1);
        assertExecutionCount(key.get(), 2);
        assertThat(personRepository.count()).isOne();
    }

    private void assertExecutionCount(TriggerKey triggerKey, int count) throws InterruptedException, ExecutionException {
        var data = persistentTaskService.getLastTriggerData(triggerKey);
        assertThat(data).isPresent();
        assertThat(data.get().getExecutionCount()).isEqualTo(count);
    }
}
