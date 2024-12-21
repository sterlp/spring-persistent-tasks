package org.sterl.spring.persistent_tasks.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
import org.sterl.spring.persistent_tasks.shared.model.TriggerData;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;
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
                    return RetryStrategy.TRY_THREE_TIMES_IMMEDIATELY;
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
        final Optional<Future<TriggerKey>> t = subject.runOrQueue(trigger);

        // THEN
        assertThat(t).isPresent();
        t.get().get();
        assertThat(personRepository.count()).isOne();
    }

    @Test
    void testRollbackAndRetry() throws Exception {
        // GIVEN
        final var trigger = TaskTriggerBuilder.newTrigger("savePerson").state("Paul").build();
        sendError.set(true);
        // WHEN
        Optional<Future<TriggerKey>> triggerKey = subject.runOrQueue(trigger);
        // THEN
        assertExecutionCount(triggerKey, 1);
        assertThat(personRepository.count()).isZero();

        // WHEN
        sendError.set(false);
        subject.triggerNextTasks().get(0).get();
        // THEN
        assertExecutionCount(triggerKey, 2);
        assertThat(personRepository.count()).isOne();
    }

    private void assertExecutionCount(Optional<Future<TriggerKey>> refId, int count) throws InterruptedException, ExecutionException {
        final TriggerKey triggerKey = refId.get().get();
        final Optional<TriggerEntity> t = triggerService.get(triggerKey);
        TriggerData data;
        if (t.isEmpty()) {
            data = historyService.findLastKnownStatus(triggerKey).get().getData();
        } else {
            data = t.get().getData();
        }
            
        assertThat(data.getExecutionCount()).isEqualTo(count);
    }
}
