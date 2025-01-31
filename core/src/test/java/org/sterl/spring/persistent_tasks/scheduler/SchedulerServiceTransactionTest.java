package org.sterl.spring.persistent_tasks.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.PersistentTask;
import org.sterl.spring.persistent_tasks.api.RetryStrategy;
import org.sterl.spring.persistent_tasks.api.TaskId.TaskTriggerBuilder;
import org.sterl.spring.persistent_tasks.api.TransactionalTask;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.sample_app.person.PersonBE;
import org.sterl.spring.sample_app.person.PersonRepository;
import org.sterl.test.Countdown;

class SchedulerServiceTransactionTest extends AbstractSpringTest {

    private SchedulerService subject;
    private static final AtomicBoolean sendError = new AtomicBoolean(false);
    private static final Countdown COUNTDOWN = new Countdown();
    @Autowired private PersonRepository personRepository;

    @Configuration
    static class Config {
        @Bean
        TransactionalTask<String> savePersonInTrx(PersonRepository personRepository) {
            return new TransactionalTask<String>() {
                @Override
                public void accept(String name) {
                    personRepository.save(new PersonBE(name));
                    COUNTDOWN.await();
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
        PersistentTask<String> savePersonNoTrx(TransactionTemplate trx,
                PersonRepository personRepository) {
            return new PersistentTask<>() {
                @Override
                public void accept(String name) {
                    trx.executeWithoutResult(t -> {
                        personRepository.save(new PersonBE(name));
                        COUNTDOWN.await();
                        if (sendError.get()) {
                            throw new RuntimeException("Error requested for " + name);
                        }
                    });
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
        COUNTDOWN.reset();
        sendError.set(false);
    }

    @Test
    void testSaveNoTransactions() throws Exception {
        // GIVEN
        final var request = TaskTriggerBuilder.newTrigger("savePersonNoTrx").state("Paul").build();
        var trigger = triggerService.queue(request);

        // WHEN
        hibernateAsserts.reset();
        COUNTDOWN.countDown();
        schedulerService.triggerNextTasks().forEach(t -> {
            try {t.get();} catch (Exception ex) {throw new RuntimeException(ex);}
        });

        // THEN
        // 1. get the trigger 
        // 2. one the event running 
        // 3. for the work
        // 4. for success status
        hibernateAsserts.assertTrxCount(5);
        assertThat(personRepository.count()).isOne();
        // AND
        var data = persistentTaskService.getLastDetailData(trigger.key());
        assertThat(data.get().getStatus()).isEqualTo(TriggerStatus.SUCCESS);
        // AND
        var history = historyService.findAllDetailsForKey(trigger.key()).getContent();
        assertThat(history.get(0).getData().getStatus()).isEqualTo(TriggerStatus.SUCCESS);
        assertThat(history.get(1).getData().getStatus()).isEqualTo(TriggerStatus.RUNNING);
        assertThat(history.get(2).getData().getStatus()).isEqualTo(TriggerStatus.WAITING);
    }
    
    @Test
    void testSaveTransactions() throws Exception {
        // GIVEN
        final var request = TaskTriggerBuilder.newTrigger("savePersonInTrx").state("Paul").build();
        var trigger = triggerService.queue(request);

        // WHEN
        hibernateAsserts.reset();
        COUNTDOWN.countDown();
        schedulerService.triggerNextTasks().forEach(t -> {
            try {t.get();} catch (Exception ex) {throw new RuntimeException(ex);}
        });

        // THEN
        hibernateAsserts.assertTrxCount(3);
        assertThat(personRepository.count()).isOne();
        // AND
        var data = persistentTaskService.getLastDetailData(trigger.key());
        assertThat(data.get().getStatus()).isEqualTo(TriggerStatus.SUCCESS);
        // AND
        var history = historyService.findAllDetailsForKey(trigger.key()).getContent();
        assertThat(history.get(0).getData().getStatus()).isEqualTo(TriggerStatus.SUCCESS);
        assertThat(history.get(1).getData().getStatus()).isEqualTo(TriggerStatus.RUNNING);
        assertThat(history.get(2).getData().getStatus()).isEqualTo(TriggerStatus.WAITING);
    }
    
    @Test
    void test_fail_in_transaction() throws Exception {
        // GIVEN
        final var request = TaskTriggerBuilder.newTrigger("savePersonInTrx").state("Paul").build();
        var trigger = triggerService.queue(request);
        sendError.set(true);

        // WHEN
        hibernateAsserts.reset();
        COUNTDOWN.countDown();
        schedulerService.triggerNextTasks().forEach(t -> {
            try {t.get();} catch (Exception ex) {throw new RuntimeException(ex);}
        });

        // THEN
        // 1. Get the trigger
        // 2. Running history
        // 3. Run the trigger which will fail
        // 4. Update the status to failed and write the history
        hibernateAsserts.assertTrxCount(4);
        // AND
        var data = persistentTaskService.getLastDetailData(trigger.key());
        assertThat(data.get().getStatus()).isEqualTo(TriggerStatus.FAILED);
        assertThat(triggerService.get(trigger.getKey()).get().getRunningOn()).isNull();
        assertThat(triggerService.get(trigger.getKey()).get().status()).isEqualTo(TriggerStatus.WAITING);
        // AND
        var history = historyService.findAllDetailsForKey(trigger.key()).getContent();
        assertThat(history.get(0).getData().getStatus()).isEqualTo(TriggerStatus.FAILED);
        assertThat(history.get(1).getData().getStatus()).isEqualTo(TriggerStatus.RUNNING);
        assertThat(history.get(2).getData().getStatus()).isEqualTo(TriggerStatus.WAITING);
    }
    
    @Test
    void testRunOrQueueShowsRunning() throws Exception {
        // GIVEN
        var k1 = subject.runOrQueue(TaskTriggerBuilder.newTrigger("savePersonInTrx").state("Paul").build());
        var k2 = subject.runOrQueue(TaskTriggerBuilder.newTrigger("savePersonInTrx").state("Paul").build());

        // WHEN
        assertThat(persistentTaskService.getLastTriggerData(k1).get().getStatus())
            .isEqualTo(TriggerStatus.RUNNING);
        assertThat(persistentTaskService.getLastTriggerData(k2).get().getStatus())
            .isEqualTo(TriggerStatus.RUNNING);

        // THEN
        Thread.sleep(150); // wait for the history async events
        hibernateAsserts.assertTrxCount(7);
        
        // WHEN
        COUNTDOWN.countDown();
        awaitRunningTasks();
        // THEN
        assertThat(personRepository.count()).isEqualTo(2);
        // AND
        assertThat(persistentTaskService.getLastTriggerData(k1).get().getStatus())
            .isEqualTo(TriggerStatus.SUCCESS);
        assertThat(persistentTaskService.getLastTriggerData(k2).get().getStatus())
            .isEqualTo(TriggerStatus.SUCCESS);
    }

    @Test
    void testRollbackAndRetry() throws Exception {
        // GIVEN
        final var triggerRequest = TaskTriggerBuilder.newTrigger("savePersonInTrx")
                .state("Paul").build();
        sendError.set(true);

        // WHEN
        var key = subject.runOrQueue(triggerRequest);
        COUNTDOWN.countDown();
        awaitRunningTasks();

        // THEN
        var history = historyService.findAllDetailsForKey(key).getContent();
        assertThat(history.get(0).getData().getStatus())
            .isEqualTo(TriggerStatus.FAILED);
        assertThat(history.get(1).getData().getStatus())
            .isEqualTo(TriggerStatus.RUNNING);
        assertThat(history.get(2).getData().getStatus())
            .isEqualTo(TriggerStatus.WAITING);

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
