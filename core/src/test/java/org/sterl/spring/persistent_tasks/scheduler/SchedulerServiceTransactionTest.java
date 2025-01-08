package org.sterl.spring.persistent_tasks.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.PersistentTask;
import org.sterl.spring.persistent_tasks.api.RetryStrategy;
import org.sterl.spring.persistent_tasks.api.TaskId.TaskTriggerBuilder;
import org.sterl.spring.persistent_tasks.api.TransactionalTask;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;
import org.sterl.spring.sample_app.person.PersonBE;
import org.sterl.spring.sample_app.person.PersonRepository;

class SchedulerServiceTransactionTest extends AbstractSpringTest {

    private SchedulerService subject;
    private static AtomicBoolean sendError = new AtomicBoolean(false);
    @Autowired private PersonRepository personRepository;

    @Configuration
    static class Config {
        @Bean
        TransactionalTask<String> savePersonInTrx(PersonRepository personRepository) {
            return new TransactionalTask<String>() {
                @Override
                public void accept(String name) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
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
        
        @Bean
        PersistentTask<String> savePersonNoTrx(PersonRepository personRepository) {
            return new PersistentTask<>() {
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
                @Override
                public boolean isTransactional() {
                    return false;
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
    void testSaveTransactions() throws Exception {
        // GIVEN
        final var request = TaskTriggerBuilder.newTrigger("savePersonNoTrx").state("Paul").build();
        var trigger = triggerService.queue(request);

        // WHEN
        hibernateAsserts.reset();
        triggerService.run(trigger);

        // THEN
        // AND one the service, one the event and one more status update
        hibernateAsserts.assertTrxCount(4);
        assertThat(personRepository.count()).isOne();
    }

    
    @Test
    void testTrxCountTriggerService() throws Exception {
        // GIVEN
        final var request = TaskTriggerBuilder.newTrigger("savePersonInTrx").state("Paul").build();
        var trigger = triggerService.queue(request);

        // WHEN
        hibernateAsserts.reset();
        triggerService.run(trigger);

        // THEN
        hibernateAsserts.assertTrxCount(1);
        assertThat(personRepository.count()).isOne();
    }
    
    @Test
    void testFailTrxCount() throws Exception {
        // GIVEN
        final var request = TaskTriggerBuilder.newTrigger("savePersonInTrx").state("Paul").build();
        var trigger = triggerService.queue(request);
        sendError.set(true);

        // WHEN
        hibernateAsserts.reset();
        triggerService.run(trigger);

        // THEN
        // first the work which runs on error
        // second the update to the trigger
        // third to write the history
        hibernateAsserts.assertTrxCount(3);
    }
    
    @Test
    void testRunOrQueue() throws Exception {
        // GIVEN
        var k1 = subject.runOrQueue(TaskTriggerBuilder.newTrigger("savePersonInTrx").state("Paul").build());
        var k2 = subject.runOrQueue(TaskTriggerBuilder.newTrigger("savePersonInTrx").state("Paul").build());

        // WHEN
        assertThat(persistentTaskService.getLastTriggerData(k1).get().getStatus())
            .isEqualTo(TriggerStatus.RUNNING);
        assertThat(persistentTaskService.getLastTriggerData(k2).get().getStatus())
            .isEqualTo(TriggerStatus.RUNNING);


        // THEN
        awaitRunningTasks();
        assertThat(personRepository.count()).isEqualTo(2);
    }

    @Test
    void testRollbackAndRetry() throws Exception {
        // GIVEN
        final var triggerRequest = TaskTriggerBuilder.newTrigger("savePersonInTrx").state("Paul").build();
        sendError.set(true);

        // WHEN
        var key = subject.runOrQueue(triggerRequest);

        // THEN
        awaitRunningTasks();
        // AND the last status before we are back to running should be FAILED
        assertThat(historyService.findAllDetailsForKey(key)
                .getContent().get(0).getData().getStatus())
            .isEqualTo(TriggerStatus.FAILED);

        // WHEN
        sendError.set(false);
        var executed = persistentTaskService.executeTriggersAndWait();

        // THEN
        assertThat(executed).hasSize(1);
        assertExecutionCount(key, 2);
        assertThat(personRepository.count()).isOne();
    }

    private void assertExecutionCount(TriggerKey triggerKey, int count) throws InterruptedException, ExecutionException {
        var data = persistentTaskService.getLastTriggerData(triggerKey);
        assertThat(data).isPresent();
        assertThat(data.get().getExecutionCount()).isEqualTo(count);
    }
}
