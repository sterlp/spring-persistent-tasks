package org.sterl.spring.persistent_tasks.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.AbstractSpringTest.TaskConfig.Task3;
import org.sterl.spring.persistent_tasks.PersistentTaskService;
import org.sterl.spring.persistent_tasks.api.TriggerRequest;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TaskId.TriggerBuilder;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.scheduler.entity.SchedulerEntity;

class SchedulerServiceTest extends AbstractSpringTest {

    private SchedulerService subject;
    @Autowired
    private PersistentTaskService persistentTaskService;

    @BeforeEach
    public void beforeEach() throws Exception {
        super.beforeEach();
        subject = schedulerService;
    }

    @Test
    void schedulerShouldBeOnlineTest() {
        // GIVEN
        final var time = OffsetDateTime.now(); 
        // WHEN spring started
        final SchedulerEntity status = subject.getStatus();

        // THEN
        assertThat(status.getLastPing()).isBeforeOrEqualTo(time);
    }
    
    @RepeatedTest(3)
    void testFastTaskFreeThreadChound() throws Exception {
        // GIVEN
        var t = taskService.<String>replace("fastTask", s -> {});
        for (int i = 0; i < 50; ++i) triggerService.queue(t.newUniqueTrigger(null));
        // WHEN
        List<Future<TriggerKey>> trigger;
        while ( (trigger = subject.triggerNextTasks()).size() > 0) {
            trigger.forEach(t1 -> {
                try {
                    t1.get();
                } catch (Exception e) {
                    throw new RuntimeException(e.getCause());
                }
            });
        }
        // THEN
        assertThat(subject.getRunning().size()).isZero();
        assertThat(subject.hasRunningTriggers()).isFalse();
        // AND
        final SchedulerEntity scheduler = subject.getScheduler();
        assertThat(scheduler.getRunningTasks()).isZero();
    }
    
    @Test
    void testWillTriggerOnlyFreeThreadSize() throws Exception {
        // GIVEN
        for (int i = 0; i < 15; i++) {
            triggerService.queue(TriggerBuilder
                    .newTrigger("slowTask")
                    .state(200L)
                    .build()
                );
        }

        // WHEN
        subject.triggerNextTasks();

        // THEN
        assertThat(triggerService.countTriggers(TriggerStatus.RUNNING)).isEqualTo(10);
        assertThat(triggerService.countTriggers(TriggerStatus.WAITING)).isEqualTo(5);

        // AND
        final SchedulerEntity scheduler = subject.getScheduler();
        assertThat(scheduler.getRunningTasks()).isEqualTo(10);
    }
    
    @Test
    void verifyRunningStatusTest() throws Exception {
        // GIVEN
        final TriggerKey triggerKey = triggerService.queue(TriggerBuilder
                .newTrigger("slowTask")
                .state(50L)
                .build()
            ).getKey();

        // WHEN
        final Future<TriggerKey> running = subject.triggerNextTasks().get(0);

        // THEN
        Thread.sleep(40);
        var runningTrigger = triggerService.get(triggerKey).get();
        assertThat(runningTrigger.getData().getStatus()).isEqualTo(TriggerStatus.RUNNING);
        // AND
        running.get();
        assertThat(triggerService.get(triggerKey)).isEmpty();
        // AND
        var history = historyService.findLastKnownStatus(triggerKey).get();
        assertThat(history.getData().getStatus()).isEqualTo(TriggerStatus.SUCCESS);
    }
    
    @Test
    void testRunOrQueue() throws Exception {
        // GIVEN
        final TriggerRequest<String> triggerRequest = Task3.ID
                .newTrigger("Hallo")
                .build();
        
        // WHEN
        var ref = subject.runOrQueue(triggerRequest);

        // THEN
        asserts.awaitValue(Task3.NAME + "::Hallo");
        persistentTaskTestService.awaitRunningTriggers();

        assertThat(persistentTaskService.getLastTriggerData(ref).get().getStatus())
            .isEqualTo(TriggerStatus.SUCCESS);
    }

    @Test
    void testQueuedInFuture() {
        // GIVEN
        final TriggerRequest<String> triggerRequest = Task3.ID
                .newTrigger("Hallo")
                .runAfter(Duration.ofMinutes(5))
                .build();
        subject.runOrQueue(triggerRequest);
        
        // WHEN
        persistentTaskTestService.scheduleNextTriggersAndWait();
        
        // THEN
        asserts.assertMissing(Task3.NAME + "::Hallo");
        assertThat(triggerService.countTriggers(TriggerStatus.WAITING)).isOne();
    }

    @Test
    void runSimpleTaskMultipleTimesTest() throws Exception {
        // GIVEN
        TaskId<String> taskId = taskService.replace("foo", c -> asserts.info(c));
        for (int i = 1; i < 21; ++i) {
            triggerService.queue(taskId.newTrigger(i + " state").build());
        }

        // WHEN
        persistentTaskTestService.runAllDueTrigger(OffsetDateTime.now());

        // THEN
        for (int i = 1; i < 21; ++i) {
            asserts.awaitValue(i + " state");
        }

        assertThat(historyService.countTriggers(TriggerStatus.SUCCESS)).isEqualTo(20);
        assertThat(triggerService.hasPendingTriggers()).isFalse();
    }
}
