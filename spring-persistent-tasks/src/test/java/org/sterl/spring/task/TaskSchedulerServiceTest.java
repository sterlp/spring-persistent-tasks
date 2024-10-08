package org.sterl.spring.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.sterl.spring.task.api.RetryStrategy;
import org.sterl.spring.task.api.SpringBeanTask;
import org.sterl.spring.task.api.TaskId;
import org.sterl.spring.task.api.TriggerId;
import org.sterl.spring.task.model.TaskSchedulerEntity.TaskSchedulerStatus;
import org.sterl.spring.task.model.TriggerStatus;
import org.sterl.spring.task.repository.TaskRepository;

class TaskSchedulerServiceTest extends AbstractSpringTest {

    @Autowired
    private TaskSchedulerService subject;
    @Autowired
    private TaskRepository taskRepository;
    
    @BeforeEach
    void before() throws Exception {
        subject.deleteAllTriggers();
        subject.pingRegistry();
        taskRepository.clear();
    }

    @Test
    void schedulerShouldBeOnlineTest() {
        // GIVEN

        // WHEN spring started

        // THEN
        assertThat(taskSchedulerRepository.count()).isOne();
        assertThat(taskSchedulerRepository.findAll().get(0).getStatus()).isEqualTo(TaskSchedulerStatus.ONLINE);
    }
    
    @Test
    void canCreateAnTrigger() {
        // GIVEN
        TaskId<String> taskId = subject.register("foo", c -> asserts.info("foo"));
        subject.<String>register("bar", c -> asserts.info("bar"));

        // WHEN
        subject.trigger(taskId);
        subject.trigger(taskId);

        // THEN
        assertThat(subject.countTriggers(taskId)).isEqualTo(2);
    }

    @Test
    void runSimpleTaskTest() throws Exception {
        // GIVEN
        TaskId<String> taskId = subject.register("foo", c -> asserts.info("foo"));
        subject.<String>register("bar", c -> asserts.info("bar"));
        TriggerId triggerId = subject.trigger(taskId);

        // WHEN
        subject.triggerNextTask().get();

        // THEN
        assertThat(subject.countTriggers(TriggerStatus.SUCCESS)).isOne();
        assertThat(subject.get(triggerId).get().getData().getExecutionCount()).isEqualTo(1);
        asserts.assertValue("foo");
        asserts.assertMissing("bar");
    }

    @Test
    void runSimpleTaskWithStateTest() throws Exception {
        // GIVEN
        TaskId<String> task = subject.register("foo", c -> asserts.info(c));
        subject.trigger(task, "Hello");

        // WHEN
        subject.triggerNextTask().get();

        // THEN
        asserts.assertValue("Hello");
        assertThat(subject.countTriggers(TriggerStatus.SUCCESS)).isOne();
        assertThat(subject.hasTriggers()).isFalse();
    }

    @Test
    void runSimpleTaskMultipleTimesTest() throws Exception {
        // GIVEN
        TaskId<String> task = subject.register("foo", c -> asserts.info(c));
        for (int i = 1; i < 5; ++i) subject.trigger(task, i + " state");

        // WHEN
        for (int i = 1; i < 5; ++i) subject.triggerNextTask().get();

        // THEN
        for (int i = 1; i < 5; ++i) asserts.assertValue(i + " state");

        assertThat(subject.countTriggers(TriggerStatus.SUCCESS)).isEqualTo(4);
        assertThat(subject.hasTriggers()).isFalse();
    }

    @Test
    void failedTasksAreFailedTest() throws Exception {
        // GIVEN
        TaskId<String> task = subject.<String>register("foo", c -> {
            throw new RuntimeException("Nope!");
        });
        final var triggerId = subject.trigger(task);

        // WHEN
        subject.triggerNextTask().get();
        subject.triggerNextTask(OffsetDateTime.now().plusDays(1)).get();
        subject.triggerNextTask(OffsetDateTime.now().plusDays(1)).get();

        // THEN
        assertThat(subject.countTriggers(TriggerStatus.FAILED)).isOne();
        assertThat(subject.get(triggerId).get().getData().getExecutionCount()).isEqualTo(3);
    }

    @Test
    void failedSavingExceptionTest() throws Exception {
        // GIVEN
        TaskId<String> task = subject.<String>register("foo", c -> {
            throw new IllegalArgumentException("Nope! " + c);
        });

        // WHEN
        final var triggerId = subject.trigger(task.newTrigger().state("Hallo :-)").build());
        subject.triggerNextTask().get();

        // THEN
        final var trigger = subject.get(triggerId).get();
        assertThat(trigger.getData().getExceptionName()).isEqualTo(IllegalArgumentException.class.getName());
        assertThat(trigger.getData().getLastException()).contains("Nope! Hallo :-)");
    }

    @Test
    void failedTasksAreRetriedTest() throws Exception {
        // GIVEN
        TaskId<String> task = subject.register("foo",
                new SpringBeanTask<String>() {
                    @Override
                    public void accept(String state) {
                        asserts.info(state);
                        throw new RuntimeException("NOPE!");
                    }

                    public RetryStrategy retryStrategy() {
                        return RetryStrategy.TRY_THREE_TIMES_IMMEDIATELY;
                    };
                });
        var id = subject.trigger(task.newTrigger().state("hallo").build());

        // WHEN
        Awaitility.await().until(() -> {
            subject.triggerNextTask().get();
            return asserts.getCount("hallo") >= 3;
        });

        // THEN
        assertThat(asserts.getCount("hallo")).isEqualTo(3);
        assertThat(subject.countTriggers(TriggerStatus.FAILED)).isOne();
        assertThat(subject.get(id).get().getData().getExecutionCount()).isEqualTo(3);
    }

    @Test
    void taskPriorityTest() throws Exception {
        // GIVEN
        TaskId<String> task = subject.register("aha", s -> asserts.info(s));
        List<TriggerId> triggers = subject.triggerAll(Arrays.asList(
                task.newTrigger().state("mid").priority(5).build(),
                task.newTrigger().state("low").priority(4).build(),
                task.newTrigger().state("high").priority(6).build()));
        // WHEN
        while (subject.hasTriggers())
            subject.triggerNextTask().get();

        // THEN
        assertThat(subject.get(triggers.get(0)).get().getData().getPriority()).isEqualTo(5);
        assertThat(subject.get(triggers.get(1)).get().getData().getPriority()).isEqualTo(4);
        assertThat(subject.get(triggers.get(2)).get().getData().getPriority()).isEqualTo(6);
        asserts.awaitOrdered("high", "mid", "low");
        assertThat(subject.countTriggers(TriggerStatus.SUCCESS)).isEqualTo(3);
    }

    @Test
    void creationJoinTransactionTest() throws Exception {
        // GIVEN
        final var taskId = subject.register("aha", s -> asserts.info("should not trigger"));

        // WHEN
        try {
            trx.executeWithoutResult(t -> {
                subject.trigger(taskId);
                subject.trigger(taskId);
                throw new RuntimeException("we are doomed!");
            });
        } catch (Exception idc) {
        }

        subject.triggerNextTask().get();

        // THEN
        asserts.assertMissing("should not trigger");
        assertThat(triggerRepository.count()).isZero();
    }

    @Test
    void overrideTriggerUsingSameTest() throws Exception {
        // GIVEN
        final TaskId<String> taskId = subject.register("send_email", s -> asserts.info(s));

        // WHEN
        subject.trigger(taskId.newTrigger()
                .id("paul@sterl.org")
                .state("pau@sterl.org") // bad state
                .build());
        subject.trigger(taskId.newTrigger()
                .id("paul@sterl.org")
                .state("paul@sterl.org") // fixed state
                .build());

        subject.triggerNextTask().get();
        subject.triggerNextTask().get();

        // THEN
        asserts.awaitValueOnce("paul@sterl.org");
        assertThat(subject.countTriggers(TriggerStatus.SUCCESS)).isEqualTo(1);
    }

    @Test
    void multithreadingTest() throws Exception {
        // GIVEN
        try (final var executor = Executors.newFixedThreadPool(100)) {
            final TaskId<String> taskId = subject.register("multi-threading", s -> asserts.info(s));
            for (int i = 1; i <= 100; ++i) {
                subject.trigger(taskId, "t" + i);
            }
            
            final List<Callable<Object>> tasks = new ArrayList<>(100);
            for (int i = 1; i <= 100; ++i) {
                tasks.add(() -> subject.triggerNextTask().get());
            }
            
            // WHEN
            executor.invokeAll(tasks);
            while (subject.hasTriggers()) subject.triggerNextTask();
            
            // THEN
            for (int i = 1; i <= 100; ++i) {
                asserts.awaitValueOnce("t" + i);
            }
            assertThat(subject.countTriggers(TriggerStatus.SUCCESS)).isEqualTo(100);
        }
        
    }
}
