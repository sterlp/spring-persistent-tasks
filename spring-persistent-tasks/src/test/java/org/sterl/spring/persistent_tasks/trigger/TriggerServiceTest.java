package org.sterl.spring.persistent_tasks.trigger;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.AbstractSpringTest.TaskConfig.Task3;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TaskId.TaskTriggerBuilder;
import org.sterl.spring.persistent_tasks.api.TriggerId;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;
import org.sterl.spring.persistent_tasks.task.repository.TaskRepository;

class TriggerServiceTest extends AbstractSpringTest {

    @Autowired
    private TriggerService subject;
    @Autowired
    private TaskRepository taskRepository;

    // ensure task in the spring context
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
        final var triggerId = subject.queue(trigger);

        // THEN
        final var e = subject.get(triggerId);
        assertThat(e).isPresent();
        assertThat(e.get().getData().getRunAt().toEpochSecond()).isEqualTo(triggerTime.toEpochSecond());
        assertThat(e.get().getData().getCreatedTime()).isNotNull();
        assertThat(e.get().getData().getStart()).isNull();
        assertThat(e.get().getData().getEnd()).isNull();
        assertThat(e.get().getData().getExecutionCount()).isZero();
    }

    @Test
    void testCanCreateAnTrigger() {
        // GIVEN
        TaskId<String> taskId = taskService.replace("foo", c -> asserts.info("foo"));
        taskService.<String>replace("bar", c -> asserts.info("bar"));

        // WHEN
        subject.queue(taskId.newTrigger().build());
        subject.queue(taskId.newTrigger().build());

        // THEN
        assertThat(subject.countTriggers(taskId)).isEqualTo(2);
    }

    @Test
    void testTriggerSpringSimpleTask() throws Exception {
        // GIVEN
        final var trigger = TaskTriggerBuilder.newTrigger(Task3.NAME).state("trigger3").build();

        // WHEN
        var id = subject.queue(trigger);
        subject.run(subject.get(id).get());

        // THEN
        assertThat(taskRepository.contains(Task3.NAME)).isTrue();
        asserts.awaitValue(Task3.NAME + "::trigger3");
    }

    @Test
    void testRunSimpleTask() throws Exception {
        // GIVEN
        TaskId<String> taskId = taskService.replace("foo", c -> asserts.info("foo"));
        taskService.<String>replace("bar", c -> asserts.info("bar"));
        TriggerId triggerId = subject.queue(taskId.newTrigger().build());

        // WHEN
        subject.run(triggerId);

        // THEN
        assertThat(historyService.countTriggers(TriggerStatus.SUCCESS)).isOne();
        final var historyEntity = historyService.findLastKnownStatus(triggerId).get();
        assertThat(historyEntity.getData().getExecutionCount()).isEqualTo(1);
        assertThat(historyEntity.getData().getEnd()).isAfter(historyEntity.getData().getStart());
        assertThat(historyEntity.getData().getRunningDurationInMs())
            .isEqualTo(Duration.between(
                    historyEntity.getData().getStart(),
                    historyEntity.getData().getEnd()).toMillis());
        assertThat(historyEntity.getData().getExecutionCount()).isEqualTo(1);
        asserts.assertValue("foo");
        asserts.assertMissing("bar");
    }

    @Test
    void testTriggerChainTask() throws Exception {
        // GIVEN
        final var trigger = task1Id.newTrigger().state("aa").build();

        // WHEN
        final var triggerId = subject.queue(trigger);
        subject.run(subject.lockNextTrigger());
        subject.run(subject.lockNextTrigger());

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
    void testFailedSavingException() throws Exception {
        // GIVEN
        TaskId<String> task = taskService.<String>replace("foo", c -> {
            throw new IllegalArgumentException("Nope! " + c);
        });

        // WHEN
        final var triggerId = subject.queue(task.newTrigger().state("Hallo :-)").build());
        subject.run(subject.lockNextTrigger());

        // THEN
        final var trigger = triggerService.get(triggerId).get();
        assertThat(trigger.getData().getExecutionCount()).isEqualTo(1);
        assertThat(trigger.getData().getExceptionName()).isEqualTo(IllegalArgumentException.class.getName());
        assertThat(trigger.getData().getLastException()).contains("Nope! Hallo :-)");
    }
    
    @Test
    void testFailedTriggerHasDuration() throws Exception {
        // GIVEN
        TaskId<String> task = taskService.<String>replace("foo", c -> {
            throw new IllegalArgumentException("Nope! " + c);
        });

        // WHEN
        final var triggerId = subject.queue(task.newTrigger().state("Hallo :-)").build());
        subject.run(subject.lockNextTrigger());

        // THEN
        final var trigger = triggerService.get(triggerId).get();
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
        List<TriggerId> triggers = triggerService.queueAll(Arrays.asList(
                task.newTrigger().state("mid").priority(5).build(), //
                task.newTrigger().state("low").priority(4).build(), //
                task.newTrigger().state("high").priority(6).build()));
        // WHEN
        runNextTrigger();
        runNextTrigger();
        runNextTrigger();

        // THEN
        assertThat(historyService.findLastKnownStatus(triggers.get(0)).get().getData().getPriority()).isEqualTo(5);
        assertThat(historyService.findLastKnownStatus(triggers.get(1)).get().getData().getPriority()).isEqualTo(4);
        assertThat(historyService.findLastKnownStatus(triggers.get(2)).get().getData().getPriority()).isEqualTo(6);
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
        } catch (Exception idc) {
        }

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

        var e1 = runNextTrigger();
        var e2 = runNextTrigger();

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
            ArrayList<Callable<?>> lockInvocations = new ArrayList<>();
            for (int i = 1; i <= 100; ++i) {
                lockInvocations.add(() -> runNextTrigger());
            }
            executor.invokeAll(lockInvocations);

            // THEN
            for (int i = 1; i <= 100; ++i) {
                asserts.awaitValueOnce("t" + i);
            }
            assertThat(historyService.countTriggers(TriggerStatus.SUCCESS)).isEqualTo(100);
        }
    }
}
