package org.sterl.spring.persistent_tasks.trigger;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.test.context.event.ApplicationEvents;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.api.task.PersistentTask;
import org.sterl.spring.persistent_tasks.task.exception.TriggerSignalExpiredException;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerExpiredEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerFailedEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerRunningEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerSuccessEvent;

class TriggerLifeCycleTest extends AbstractSpringTest {

    @Autowired
    private TriggerService subject;

    @Autowired
    private ApplicationEvents events;

    @Test
    void testFailedTrigger() throws Exception {
        // GIVEN
        final AtomicInteger afterTriggerFailedCalled = new AtomicInteger(0);
        final AtomicReference<Exception> exRef = new AtomicReference<>();
        TaskId<String> task = taskService.<String>replace("foo", new PersistentTask<>() {
            @Override
            public void accept(@Nullable String c) {
                throw new IllegalArgumentException("Nope! " + c);
            }

            public void afterTriggerFailed(String c, Exception e) {
                afterTriggerFailedCalled.incrementAndGet();
                exRef.set(e);
            }
        });

        // WHEN
        subject.queue(task.newTrigger().state("Hallo :-)").build());
        persistentTaskTestService.runAllDueTrigger(OffsetDateTime.now().plusDays(10));

        // THEN
        assertThat(events.stream(TriggerSuccessEvent.class).count()).isZero();
        assertThat(events.stream(TriggerRunningEvent.class).count()).isEqualTo(4);
        assertThat(events.stream(TriggerFailedEvent.class).count()).isEqualTo(4);
        // AND
        assertThat(events.stream(TriggerFailedEvent.class).filter(e -> e.isDone()).count()).isOne();
        // AND
        assertThat(afterTriggerFailedCalled.get()).isOne();
        assertThat(exRef.get().getClass()).isEqualTo(IllegalArgumentException.class);
        assertThat(exRef.get().getMessage()).isEqualTo("Nope! Hallo :-)");
    }

    @Test
    void testExpireTimeoutTriggers() {
        // GIVEN
        final AtomicInteger afterTriggerFailedCalled = new AtomicInteger(0);
        final AtomicReference<Exception> exRef = new AtomicReference<>();
        TaskId<String> taskId = taskService.replace("foo", new PersistentTask<>() {
            @Override
            public void accept(@Nullable String state) {
                asserts.info(state);
            }

            public void afterTriggerFailed(String state, Exception e) {
                afterTriggerFailedCalled.incrementAndGet();
                exRef.set(e);
            }
        });
        subject.queue(
                taskId.newTrigger().waitForSignal(OffsetDateTime.now().plusMinutes(1)).state("old state").build());
        var trigger = subject
                .queue(taskId.newTrigger().waitForSignal(OffsetDateTime.now().minusSeconds(1)).state("foobar").build());

        // WHEN
        var expired = subject.expireTimeoutTriggers();
        awaitHistoryThreads();

        // THEN
        assertThat(expired).hasSize(1);
        assertThat(trigger).isEqualTo(expired.get(0));
        // AND the expired signal is now a terminal, done end state
        assertThat(events.stream(TriggerExpiredEvent.class).count()).isOne();
        assertThat(events.stream(TriggerExpiredEvent.class).allMatch(TriggerExpiredEvent::isDone)).isTrue();
        assertThat(events.stream(TriggerFailedEvent.class).count()).isZero();
        // AND it is removed from the active table (only the still waiting trigger remains)
        assertThat(triggerService.get(trigger.getKey())).isEmpty();
        assertThat(triggerService.countTriggers()).isOne();
        // AND it is historized with its own status
        assertThat(historyService.findLastKnownStatus(trigger.getKey()).get().status())
                .isEqualTo(TriggerStatus.EXPIRED_SIGNAL);
        // AND the owning task is notified once, terminally, with a distinguishable exception
        assertThat(afterTriggerFailedCalled.get()).isOne();
        assertThat(exRef.get()).isInstanceOf(TriggerSignalExpiredException.class);
    }

    @Test
    void testAbandonedTriggerCallFailed() {
        // GIVEN
        final AtomicInteger afterTriggerFailedCalled = new AtomicInteger(0);
        final AtomicReference<Exception> exRef = new AtomicReference<>();
        TaskId<String> taskId = taskService.replace("foo", new PersistentTask<>() {
            @Override
            public void accept(@Nullable String state) {
            }

            public void afterTriggerFailed(String state, Exception e) {
                afterTriggerFailedCalled.incrementAndGet();
                exRef.set(e);
            }
        });

        trx.execute(trx -> {
            var t = subject.queue(taskId.newTrigger().waitForSignal(OffsetDateTime.now().minusSeconds(1)).build());
            t.runOn("foo-bar-gone");
            t.setLastPing(OffsetDateTime.now().minusDays(1));
            t.getData().setExecutionCount(99);
            return t;
        });

        // WHEN
        var result = subject.rescheduleAbandoned(OffsetDateTime.now());

        // THEN
        assertThat(result).hasSize(1);
        assertThat(afterTriggerFailedCalled.get()).isOne();
        assertThat(exRef.get()).isNotNull();
    }
}
