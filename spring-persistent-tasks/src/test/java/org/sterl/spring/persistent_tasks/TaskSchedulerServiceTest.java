package org.sterl.spring.persistent_tasks;

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
import org.sterl.spring.persistent_tasks.api.RetryStrategy;
import org.sterl.spring.persistent_tasks.api.SpringBeanTask;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TriggerId;
import org.sterl.spring.persistent_tasks.scheduler.SchedulerService;
import org.sterl.spring.persistent_tasks.scheduler.entity.SchedulerEntity.TaskSchedulerStatus;
import org.sterl.spring.persistent_tasks.task.repository.TaskRepository;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerStatus;

class TaskSchedulerServiceTest extends AbstractSpringTest {

    @Autowired
    private SchedulerService subject;
    @Autowired
    private TaskRepository taskRepository;
    
    @BeforeEach
    void before() throws Exception {
        subject.deleteAll();
        subject.pingRegistry();
        taskRepository.clear();
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
