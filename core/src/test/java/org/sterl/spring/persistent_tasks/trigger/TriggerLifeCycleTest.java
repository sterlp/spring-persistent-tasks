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
import org.sterl.spring.persistent_tasks.api.task.PersistentTask;
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
        TaskId<String> taskId = taskService.replace("foo", asserts::info);
        subject.queue(
                taskId.newTrigger().waitForSignal(OffsetDateTime.now().plusMinutes(1)).state("old state").build());
        var trigger = subject
                .queue(taskId.newTrigger().waitForSignal(OffsetDateTime.now().minusSeconds(1)).state("foobar").build());

        // WHEN
        var expired = subject.expireTimeoutTriggers();

        // WHEN
        assertThat(expired).hasSize(1);
        assertThat(trigger).isEqualTo(expired.get(0));
        // AND
        assertThat(events.stream(TriggerExpiredEvent.class).count()).isOne();
        assertThat(events.stream(TriggerFailedEvent.class).count()).isZero();
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
