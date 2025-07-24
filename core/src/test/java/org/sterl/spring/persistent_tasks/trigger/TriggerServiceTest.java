package org.sterl.spring.persistent_tasks.trigger;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.AbstractSpringTest.TaskConfig.Task3;
import org.sterl.spring.persistent_tasks.PersistentTaskService;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TaskId.TriggerBuilder;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerRequest;
import org.sterl.spring.persistent_tasks.api.TriggerSearch;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.history.repository.CompletedTriggerRepository;
import org.sterl.spring.persistent_tasks.task.exception.CancelTaskException;
import org.sterl.spring.persistent_tasks.task.exception.FailTaskNoRetryException;
import org.sterl.spring.persistent_tasks.task.repository.TaskRepository;
import org.sterl.spring.persistent_tasks.trigger.component.StateSerializer.DeSerializationFailedException;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerAddedEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerCanceledEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerExpiredEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerFailedEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerResumedEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerSuccessEvent;
import org.sterl.spring.persistent_tasks.trigger.model.RunningTriggerEntity;
import org.sterl.spring.persistent_tasks.trigger.repository.RunningTriggerRepository;

import com.github.f4b6a3.uuid.UuidCreator;

class TriggerServiceTest extends AbstractSpringTest {

    @Autowired
    private PersistentTaskService persistentTaskService;
    
    @Autowired
    private TriggerService subject;
    @Autowired
    private RunningTriggerRepository triggerRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private CompletedTriggerRepository completedTriggerRepository;
    
    @Autowired
    private ApplicationEvents events;

    // ensure persistentTask in the spring context
    @Autowired
    private TaskId<String> task1Id;
    @Autowired
    private TaskId<String> task2Id;
    @Autowired
    private TaskId<String> task3Id;

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
        final var trigger = task1Id.newTrigger().runAt(triggerTime).build();

        // WHEN
        final var triggerId = subject.queue(trigger).getKey();

        // THEN
        hibernateAsserts.assertTrxCount(1);
        // one for the trigger and just one for the history
        hibernateAsserts.assertInsertCount(2);
        // AND
        assertThat(completedTriggerRepository.count()).isZero();
        // AND
        assertThat(events.stream(TriggerAddedEvent.class).count()).isOne();
        // AND
        final var e = subject.get(triggerId);
        assertThat(e).isPresent();
        assertThat(e.get().getData().getRunAt().toEpochSecond()).isEqualTo(triggerTime.toEpochSecond());
        assertThat(e.get().getData().getCreatedTime()).isNotNull();
        assertThat(e.get().getData().getStart()).isNull();
        assertThat(e.get().getData().getEnd()).isNull();
        assertThat(e.get().getData().getExecutionCount()).isZero();
    }

    @Test
    void testCreateTrigger() {
        // GIVEN
        TaskId<String> taskId = taskService.replace("foo", c -> asserts.info("foo"));
        taskService.<String>replace("bar", c -> asserts.info("bar"));

        // WHEN
        subject.queue(taskId.newTrigger().build());
        var t2 = subject.queue(taskId.newTrigger("foo").build());

        // THEN
        assertThat(subject.countTriggers(taskId)).isEqualTo(2);
        assertThat(t2.getId()).isNotNull();
        assertThat(t2.getKey().getId()).isNotNull();
        assertThat(t2.getKey().getTaskName()).isNotNull();

        // AND
        assertThat(events.stream(TriggerAddedEvent.class).count()).isEqualTo(2);
        var t = subject.get(t2.getKey()).get();
        assertThat(t.getData().getState()).isEqualTo(subject.getStateSerializer().serialize("foo"));
    }

    @Test
    void testReplaceTrigger() {
        // GIVEN
        TaskId<String> taskId = taskService.replace("foo", c -> asserts.info("foo"));
        taskService.<String>replace("bar", c -> asserts.info("bar"));

        // WHEN
        subject.queue(taskId.newTrigger().id("1").build());
        subject.queue(taskId.newTrigger().id("2").build());
        subject.queue(taskId.newTrigger().id("2").build());

        // THEN
        assertThat(subject.countTriggers(taskId)).isEqualTo(2);

        // AND
        assertThat(events.stream(TriggerAddedEvent.class).count()).isEqualTo(3);
    }
    
    @Test
    void testCancelTrigger() {
        // GIVEN
        TaskId<String> taskId = taskService.replace("foo", c -> asserts.info("foo"));
        taskService.<String>replace("bar", c -> asserts.info("bar"));
        var key1 = subject.queue(taskId.newTrigger().build()).getKey();
        var key2 = subject.queue(taskId.newTrigger().build()).getKey();

        // WHEN
        final var canceled = subject.cancel(key1);

        // THEN
        assertThat(canceled).isPresent();
        assertThat(canceled.get().getKey()).isEqualTo(key1);
        
        assertThat(subject.get(key1)).isEmpty();
        assertThat(subject.get(key2)).isPresent();
        
        // AND
        assertThat(events.stream(TriggerCanceledEvent.class).count()).isOne();
    }

    @Test
    void testTriggerSpringSimpleTask() throws Exception {
        // GIVEN
        final var trigger = TriggerBuilder.newTrigger(Task3.NAME).state("trigger3").build();

        // WHEN
        subject.run(subject.queue(trigger));

        // THEN
        assertThat(taskRepository.contains(Task3.NAME)).isTrue();
        asserts.awaitValue(Task3.NAME + "::trigger3");
        // AND
        assertThat(events.stream(TriggerSuccessEvent.class).count()).isOne();
        assertThat(events.stream(TriggerFailedEvent.class).count()).isZero();
    }

    @Test
    void testRunSimpleTask() throws Exception {
        // GIVEN
        TaskId<String> taskId = taskService.replace("foo", c -> asserts.info("foo"));
        taskService.<String>replace("bar", c -> asserts.info("bar"));
        TriggerKey triggerKey = subject.queue(taskId.newTrigger().build()).getKey();

        // WHEN
        subject.run(triggerKey, "test");

        // THEN
        assertThat(historyService.countTriggers(TriggerStatus.SUCCESS)).isOne();
        final var historyEntity = historyService.findLastKnownStatus(triggerKey).get();
        assertThat(historyEntity.getData().getExecutionCount()).isEqualTo(1);
        assertThat(historyEntity.getData().getEnd()).isAfterOrEqualTo(historyEntity.getData().getStart());
        assertThat(historyEntity.getData().getRunningDurationInMs()).isNotNull();
        assertThat(historyEntity.getData().getExecutionCount()).isEqualTo(1);
        asserts.assertValue("foo");
        asserts.assertMissing("bar");
    }

    @Test
    void testTriggerChainTask() throws Exception {
        // GIVEN
        final var trigger = task1Id.newTrigger().state("aa").build();

        // WHEN
        final var triggerId = subject.queue(trigger).getKey();
        subject.run(subject.lockNextTrigger("test"));
        subject.run(subject.lockNextTrigger("test"));

        // THEN
        // AND
        asserts.awaitOrdered("task1::aa", "task2::task1::aa");
        // AND
        final var e = historyService.findLastKnownStatus(triggerId);
        assertThat(e).isPresent();
        assertThat(e.get().getData().getCreatedTime()).isNotNull();
        assertThat(e.get().getData().getStart()).isNotNull();
        assertThat(e.get().getData().getEnd()).isNotNull();
        assertThat(e.get().getData().getExecutionCount()).isOne();
    }

    @Test
    void testFailedIsOnRetry() throws Exception {
        // GIVEN
        TaskId<String> task = taskService.<String>replace("foo", c -> {
            throw new IllegalArgumentException("Nope! " + c);
        });

        // WHEN
        var trigger = subject.queue(task.newTrigger().state("Hallo :-)").build());
        subject.run(subject.lockNextTrigger("test"));

        // THEN
        trigger = triggerService.get(trigger.getKey()).get();
        assertThat(trigger.getData().getRunAt()).isAfter(OffsetDateTime.now());
        assertThat(trigger.getData().getStatus()).isEqualTo(TriggerStatus.WAITING);
        // AND
        assertThat(events.stream(TriggerSuccessEvent.class).count()).isZero();
        assertThat(events.stream(TriggerFailedEvent.class).count()).isOne();
    }

    @Test
    void testFailedSavingException() throws Exception {
        // GIVEN
        TaskId<String> task = taskService.<String>replace("foo", c -> {
            throw new IllegalArgumentException("Nope! " + c);
        });

        // WHEN
        var trigger = subject.queue(task.newTrigger().state("Hallo :-)").build());
        subject.run(subject.lockNextTrigger("test"));

        // THEN
        trigger = triggerService.get(trigger.getKey()).get();
        assertThat(trigger.getData().getExecutionCount()).isEqualTo(1);
        assertThat(trigger.getData().getExceptionName()).isEqualTo(IllegalArgumentException.class.getName());
        assertThat(trigger.getData().getLastException()).contains("Nope! Hallo :-)");
    }
    
    @Test
    void testFailedTriggerHasDuration() throws Exception {
        // GIVEN
        TaskId<String> task = taskService.<String>replace("foo", c -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            throw new IllegalArgumentException("Nope! " + c);
        });

        // WHEN
        var trigger = subject.queue(task.newTrigger().state("Hallo :-)").build());
        subject.run(subject.lockNextTrigger("test"));

        // THEN
        trigger = triggerService.get(trigger.getKey()).get();
        assertThat(trigger.getData().getEnd()).isAfter(trigger.getData().getStart());
        assertThat(trigger.getData().getRunningDurationInMs())
            .isEqualTo(Duration.between(
                    trigger.getData().getStart(),
                    trigger.getData().getEnd()).toMillis());
    }

    @Test
    void testTriggerPriority() throws Exception {
        // GIVEN
        TaskId<String> task = taskService.<String>replace("aha", s -> asserts.info(s));
        var triggers = Arrays.asList(
                task.newTrigger().state("mid").priority(5).build(), //
                task.newTrigger().state("low").priority(4).build(), //
                task.newTrigger().state("high").priority(6).build() //
            );
        
        var keys = triggers.stream() //
            .map(t -> subject.queue(t)) //
            .map(RunningTriggerEntity::getKey) //
            .toList();

        // WHEN
        persistentTaskTestService.runNextTrigger();
        persistentTaskTestService.runNextTrigger();
        persistentTaskTestService.runNextTrigger();

        // THEN
        assertThat(historyService.findLastKnownStatus(keys.get(0)).get().getData().getPriority()).isEqualTo(5);
        assertThat(historyService.findLastKnownStatus(keys.get(1)).get().getData().getPriority()).isEqualTo(4);
        assertThat(historyService.findLastKnownStatus(keys.get(2)).get().getData().getPriority()).isEqualTo(6);
        asserts.awaitOrdered("high", "mid", "low");
        assertThat(historyService.countTriggers(TriggerStatus.SUCCESS)).isEqualTo(3);
    }

    @Test
    void testTriggerCreationJoinsTransaction() throws Exception {
        // GIVEN
        final var taskId = taskService.<String>replace("aha", s -> asserts.info(s));

        // WHEN
        try {
            trx.executeWithoutResult(t -> {
                subject.queue(taskId.newUniqueTrigger("nope1"));
                subject.queue(taskId.newUniqueTrigger("nope2"));
                throw new RuntimeException("we are doomed!");
            });
        } catch (Exception idc) {}

        // THEN
        assertThat(triggerService.countTriggers()).isZero();
    }

    @Test
    void testOverrideTriggersUsingSameId() throws Exception {
        // GIVEN
        final TaskId<String> taskId = taskService.<String>replace("send_email", s -> asserts.info(s));

        // WHEN
        subject.queue(taskId.newTrigger()
                .id("paul@sterl.org")
                .state("pau@sterl.org") // bad state
                .build());
        subject.queue(taskId.newTrigger()
                .id("paul@sterl.org")
                .state("paul@sterl.org") // fixed state
                .build());

        assertThat(triggerService.countTriggers()).isEqualTo(1);

        var e1 = persistentTaskTestService.runNextTrigger();
        var e2 = persistentTaskTestService.runNextTrigger();

        // THEN
        asserts.awaitValueOnce("paul@sterl.org");
        assertThat(historyService.countTriggers(TriggerStatus.SUCCESS)).isEqualTo(1);

        // AND
        assertThat(e1).isPresent();
        assertThat(e2).isEmpty();
    }

    @Test
    void testLockTrigger() throws Exception {
        // GIVEN
        try (final var executor = Executors.newFixedThreadPool(100)) {
            final TaskId<String> taskId = taskService.<String>replace("multi-threading", s -> asserts.info(s));
            for (int i = 1; i <= 100; ++i) {
                triggerService.queue(taskId.newUniqueTrigger("t" + i));
            }

            // WHEN
            ArrayList<Callable<Optional<RunningTriggerEntity> >> lockInvocations = new ArrayList<>();
            for (int i = 1; i <= 100; ++i) {
                lockInvocations.add(() -> triggerService.run(triggerService.lockNextTrigger("test")));
            }
            Thread.sleep(500);
            executor.invokeAll(lockInvocations);
            executor.invokeAll(lockInvocations);
            executor.invokeAll(lockInvocations);
            // start any others - as MSSQL and mySQL has no row lock, only table locks implemented
            var s = persistentTaskTestService.scheduleNextTriggersAndWait(Duration.ofSeconds(2));
            System.err.println("---------> " + s.size());

            // THEN
            for (int i = 1; i <= 100; ++i) {
                asserts.awaitValueOnce("t" + i);
            }
            assertThat(asserts.getCount()).isEqualTo(100);
            assertThat(historyService.countTriggers(TriggerStatus.SUCCESS)).isEqualTo(100);
        }
    }
    
    @Test
    void testQueuedInFuture() {
        // GIVEN
        final TriggerRequest<String> triggerRequest = Task3.ID
                .newTrigger("Hallo")
                .runAfter(Duration.ofMinutes(5))
                .build();
        var triggerQueued = subject.queue(triggerRequest);
        
        // WHEN
        var executed = persistentTaskTestService.runAllDueTrigger(OffsetDateTime.now());
        
        // THEN
        assertThat(executed).isEmpty();
        asserts.assertMissing(Task3.NAME + "::Hallo");
        assertThat(triggerService.countTriggers(TriggerStatus.WAITING)).isOne();
        
        // WHEN
        executed = persistentTaskTestService.runAllDueTrigger(OffsetDateTime.now().plusMinutes(5));
        assertThat(executed).contains(triggerQueued);
    }

    @Test
    void testRescheduleAbandonedTasks() {
        // GIVEN
        var now = OffsetDateTime.now();
        var t1 = new RunningTriggerEntity(new TriggerKey(UuidCreator.getTimeOrdered().toString(), "fooTask"))
                        .runOn("fooScheduler");
        t1.setLastPing(now.minusSeconds(60));
        triggerRepository.save(t1);
        
        var t2 = new RunningTriggerEntity(new TriggerKey(UuidCreator.getTimeOrdered().toString(), "barTask"))
                .runOn("barScheduler");
        t2.setLastPing(now.minusSeconds(58));
        triggerRepository.save(t2);

        // WHEN
        final var rescheduledTasks = subject.rescheduleAbandoned(now.minusSeconds(59));

        // THEN
        assertThat(rescheduledTasks).hasSize(1);
        assertThat(rescheduledTasks.get(0).getKey()).isEqualTo(t1.getKey());
    }
    
    @Test
    void testUnknownTriggersNoRetry() {
        // GIVEN
        var t = triggerRepository.save(
                new RunningTriggerEntity(new TriggerKey(UuidCreator.getTimeOrdered().toString(), "fooTask-unknown")));
        
        // WHEN
        persistentTaskTestService.runNextTrigger();
        
        // WHEN
        var triggerData = persistentTaskService.getLastTriggerData(t.getKey()).get();
        assertThat(triggerData.getStatus()).isEqualTo(TriggerStatus.FAILED);
        assertThat(triggerData.getExceptionName()).isEqualTo(IllegalStateException.class.getName());
    }
    
    @Test
    void testBadStateNoRetry() {
        var t = triggerRepository.save(new RunningTriggerEntity(
                new TriggerKey(UuidCreator.getTimeOrdered().toString(), "slowTask")
            ).withState(new byte[] {12, 54})
        );
        
        // WHEN
        persistentTaskTestService.runNextTrigger();
        
        // WHEN
        var triggerData = persistentTaskService.getLastTriggerData(t.getKey()).get();
        assertThat(triggerData.getStatus()).isEqualTo(TriggerStatus.FAILED);
        assertThat(triggerData.getExceptionName()).isEqualTo(DeSerializationFailedException.class.getName());
        // AND
        assertThat(events.stream(TriggerSuccessEvent.class).count()).isZero();
        assertThat(events.stream(TriggerFailedEvent.class).count()).isOne();
    }
    
    @Test
    void testCancelRunningTrigger() {
        // GIVEN
        TaskId<String> taskId = taskService.replace("foo-cancel", c -> {
            throw new CancelTaskException(c);
        });
        var key1 = subject.queue(taskId.newTrigger().build()).getKey();

        // WHEN
        assertThat(persistentTaskTestService.runNextTrigger()).isPresent();
        assertThat(persistentTaskTestService.runNextTrigger()).isEmpty();

        // THEN
        assertThat(historyService.findLastKnownStatus(key1).get().status()).isEqualTo(TriggerStatus.CANCELED);
        
        // AND
        assertThat(events.stream(TriggerCanceledEvent.class)).hasSize(1);
        assertThat(events.stream(TriggerFailedEvent.class)).hasSize(0);
        assertThat(events.stream(TriggerSuccessEvent.class)).hasSize(0);
    }
    
    @Test
    void testFailRunningTriggerNoRetry() {
        // GIVEN
        TaskId<String> taskId = taskService.replace("foo-fail", c -> {
            throw new FailTaskNoRetryException(c);
        });
        var key1 = subject.queue(taskId.newTrigger().build()).getKey();

        // WHEN
        assertThat(persistentTaskTestService.runNextTrigger()).isPresent();
        assertThat(persistentTaskTestService.runNextTrigger()).isEmpty();

        // THEN
        assertThat(historyService.findLastKnownStatus(key1).get().status()).isEqualTo(TriggerStatus.FAILED);
        
        // AND
        assertThat(events.stream(TriggerFailedEvent.class).count()).isOne();
        assertThat(events.stream(TriggerCanceledEvent.class).count()).isZero();
        assertThat(events.stream(TriggerSuccessEvent.class).count()).isZero();
    }
    
    @Test
    void testAddTriggerAndWaitForSignal() {
        // GIVEN
        TaskId<String> taskId = taskService.replace("foo", c -> asserts.info("foo"));
        
        // WHEN
        var triggerKey = subject.queue(taskId.newTrigger()
                .waitForSignal(OffsetDateTime.now().plusDays(1))
                .build()).getKey();
        
        // THEN
        assertThat(persistentTaskTestService.runNextTrigger()).isEmpty();
        var t = subject.get(triggerKey).get();
        assertThat(t.getData().getStatus()).isEqualTo(TriggerStatus.AWAITING_SIGNAL);
    }
    
    @Test
    void testResumeWaitingTriggerForSignal() {
        // GIVEN
        TaskId<String> taskId = taskService.replace("foo", asserts::info);
        subject.queue(taskId.newTrigger()
                .waitForSignal(OffsetDateTime.now().plusDays(1))
                .state("foo bar")
                .build());
        var triggerKey = subject.queue(taskId.newTrigger()
                .waitForSignal(OffsetDateTime.now().plusDays(1))
                .state("old state")
                .build()).getKey();
        assertThat(persistentTaskTestService.runNextTrigger()).isEmpty();
        
        // WHEN
        subject.resume(TriggerBuilder.newTrigger(triggerKey, "new state").build());
        
        // THEN
        var t = subject.get(triggerKey).get();
        assertThat(t.getData().getStatus()).isEqualTo(TriggerStatus.WAITING);
        assertThat(t.getData().getState()).isEqualTo(subject.getStateSerializer().serialize("new state"));
        
        // WHEN
        assertThat(persistentTaskTestService.runNextTrigger()).isPresent();
        
        // THEN
        asserts.awaitValueOnce("new state");
        asserts.assertMissing("old state");
        asserts.assertMissing("foo bar");
        assertThat(events.stream(TriggerResumedEvent.class).count()).isOne();
        // AND
        assertThat(persistentTaskTestService.runNextTrigger()).isEmpty();
    }
    
    @Test
    void testResumeWaitingTriggerWithFunction() {
        // GIVEN
        TaskId<String> taskId = taskService.replace("foo", asserts::info);
        var waitingOne = subject.queue(taskId.newTrigger()
                .waitForSignal(OffsetDateTime.now().plusDays(1))
                .state("foo bar")
                .tag("aaa")
                .build())
                .getKey();
        var triggerKey = subject.queue(taskId.newTrigger()
                .waitForSignal(OffsetDateTime.now().plusDays(1))
                .state("old state")
                .tag("aaa")
                .build())
                .getKey();
        assertThat(persistentTaskTestService.runNextTrigger()).isEmpty();
        
        // WHEN
        var search = new TriggerSearch();
        search.setTag("aaa");
        search.setKeyId(triggerKey.getId());
        subject.resumeOne(search, s -> {
            return "Cool new State";
        });
        
        // THEN
        var t = subject.get(triggerKey).get();
        assertThat(t.getData().getStatus()).isEqualTo(TriggerStatus.WAITING);
        assertThat(t.getData().getState()).isEqualTo(subject.getStateSerializer().serialize("Cool new State"));
        
        // WHEN
        assertThat(persistentTaskTestService.runNextTrigger()).isPresent();
        
        // THEN
        asserts.awaitValueOnce("Cool new State");
        asserts.assertMissing("old state");
        asserts.assertMissing("foo bar");
        assertThat(events.stream(TriggerResumedEvent.class).count()).isOne();
        // AND
        assertThat(persistentTaskTestService.runNextTrigger()).isEmpty();
        assertThat(historyService.findLastKnownStatus(triggerKey).get().status()).isEqualTo(TriggerStatus.SUCCESS);
        assertThat(triggerService.get(waitingOne).get().status()).isEqualTo(TriggerStatus.AWAITING_SIGNAL);
    }
    
    @Test
    void testAwaitForSignalTriggersInTimeoutWillNotRun() {
        // GIVEN
        TaskId<String> taskId = taskService.replace("foo", asserts::info);
        subject.queue(taskId.newTrigger()
                .waitForSignal(OffsetDateTime.now().minusSeconds(1))
                .state("old state")
                .build()).getKey();

        // WHEN & THEN
        assertThat(persistentTaskTestService.runNextTrigger()).isEmpty();
        // AND
        asserts.assertMissing("old state");
    }
    
    @Test
    void testExpireTimeoutTriggers() {
        // GIVEN
        TaskId<String> taskId = taskService.replace("foo", asserts::info);
        subject.queue(taskId.newTrigger()
                .waitForSignal(OffsetDateTime.now().plusMinutes(1))
                .state("old state")
                .build());
        var trigger = subject.queue(taskId.newTrigger()
                .waitForSignal(OffsetDateTime.now().minusSeconds(1))
                .state("foobar")
                .build());
        
        // WHEN
        var expired = subject.expireTimeoutTriggers();
        
        // WHEN
        assertThat(expired).hasSize(1);
        assertThat(trigger).isEqualTo(expired.get(0));
        // AND
        assertThat(events.stream(TriggerExpiredEvent.class).count()).isOne();
    }
}
