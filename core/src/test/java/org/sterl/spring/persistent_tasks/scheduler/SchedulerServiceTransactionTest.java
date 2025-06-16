package org.sterl.spring.persistent_tasks.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.PersistentTaskService;
import org.sterl.spring.persistent_tasks.api.RetryStrategy;
import org.sterl.spring.persistent_tasks.api.TaskId.TriggerBuilder;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.api.task.PersistentTask;
import org.sterl.spring.persistent_tasks.api.task.TransactionalTask;
import org.sterl.spring.persistent_tasks.test.Countdown;
import org.sterl.spring.sample_app.person.PersonEntity;
import org.sterl.spring.sample_app.person.PersonRepository;

class SchedulerServiceTransactionTest extends AbstractSpringTest {

    private SchedulerService subject;
    private static final AtomicBoolean sendError = new AtomicBoolean(false);
    private static final Countdown COUNTDOWN = new Countdown();
    
    @Autowired 
    private PersonRepository personRepository;
    @Autowired
    private PersistentTaskService persistentTaskService;

    @Configuration
    static class Config {
        @Bean
        TransactionalTask<String> savePersonInTrx(PersonRepository personRepository) {
            return new TransactionalTask<String>() {
                @Override
                public void accept(@Nullable String name) {
                    personRepository.save(new PersonEntity(name));
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
                public void accept(@Nullable String name) {
                    trx.executeWithoutResult(t -> {
                        personRepository.save(new PersonEntity(name));
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
        final var request = TriggerBuilder.newTrigger("savePersonNoTrx").state("Paul").build();
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
        // 3. for the work & for success status
        // 4. the history
        hibernateAsserts.assertTrxCount(4);
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
        final var request = TriggerBuilder.newTrigger("savePersonInTrx").state("Paul").build();
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
        final var request = TriggerBuilder.newTrigger("savePersonInTrx").state("Paul").build();
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
    void testRunOrQueueTransactions() throws Exception {
        // GIVEN & WHEN
        var k1 = subject.runOrQueue(TriggerBuilder.newTrigger("savePersonInTrx").state("Paul").build());

        // THEN 1 to save and 1 to start it and 1 for the history
        awaidHistoryThreads();
        hibernateAsserts.assertTrxCount(3);
        assertThat(persistentTaskService.getLastTriggerData(k1).get().getStatus())
            .isEqualTo(TriggerStatus.RUNNING);

        // WHEN
        hibernateAsserts.reset();
        COUNTDOWN.countDown();
        Awaitility.await().until(() -> hibernateAsserts.getStatistics().getTransactionCount() >= 1);
        awaidHistoryThreads();
        hibernateAsserts.assertTrxCount(1);

        // THEN
        assertThat(personRepository.count()).isEqualTo(1);
        // AND
        assertThat(persistentTaskService.getLastTriggerData(k1).get().getStatus())
            .isEqualTo(TriggerStatus.SUCCESS);
    }
    
    @Test
    void testRunOrQueueShowsRunning() throws Exception {
        // GIVEN
        var k1 = subject.runOrQueue(TriggerBuilder.newTrigger("savePersonInTrx").state("Paul").build());
        var k2 = subject.runOrQueue(TriggerBuilder.newTrigger("savePersonInTrx").state("Paul").build());

        // WHEN
        assertThat(persistentTaskService.getLastTriggerData(k1).get().getStatus())
            .isEqualTo(TriggerStatus.RUNNING);
        assertThat(persistentTaskService.getLastTriggerData(k2).get().getStatus())
            .isEqualTo(TriggerStatus.RUNNING);

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
        final var triggerRequest = TriggerBuilder.newTrigger("savePersonInTrx")
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
        var executed = persistentTaskTestService.scheduleNextTriggersAndWait(Duration.ofSeconds(3));

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
    
    protected void awaitRunningTasks() throws TimeoutException, InterruptedException {
        final long start = System.currentTimeMillis();
        while (triggerService.countTriggers(TriggerStatus.RUNNING) > 0) {
            if (System.currentTimeMillis() - start > 2000) {
                throw new TimeoutException("Still running after 2s");
            }
            Thread.sleep(100);
        }
    }
}
