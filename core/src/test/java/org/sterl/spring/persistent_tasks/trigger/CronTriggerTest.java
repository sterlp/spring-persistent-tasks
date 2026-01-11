package org.sterl.spring.persistent_tasks.trigger;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.domain.Pageable;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.AbstractSpringTest.TaskConfig.Task3;
import org.sterl.spring.persistent_tasks.api.TaskId;

class CronTriggerTest extends AbstractSpringTest {

    @Autowired
    private TriggerService subject;

    @Autowired
    private TaskId<String> task1Id;
    @Autowired
    private TaskId<String> task2Id;
    @Autowired
    private TaskId<String> task3Id;

    @Test
    void testAddCron() throws Exception {
        // GIVEN
        final var cron = task1Id.newCron()
                .after(Duration.ofHours(1))
                .id("some-id-to-be-refactoring-save")
                .build();

        // WHEN
        final var triggerId = subject.register(cron);

        // THEN
        assertThat(triggerId).isTrue();
        hibernateAsserts.assertTrxCount(2);
        // one for the trigger and one for the history
        hibernateAsserts.assertInsertCount(2);
        // AND
        assertThat(subject.countTriggers(cron.getTaskId())).isOne();
        assertThat(subject.countTriggers(task2Id)).isZero();
    }

    @Test
    void test_cron_triggers_are_not_create_if_exisits() throws Exception {
        // GIVEN
        final var cron1 = task1Id.newCron()
                .after(Duration.ofHours(1))
                .build();
        final var cron2 = task2Id.newCron()
                .after(Duration.ofHours(1))
                .build();

        subject.register(cron1);
        subject.register(cron2);

        // WHEN
        var count = subject.queueCronTrigger();

        // THEN - as by default we should create all which are needed
        assertThat(count).isZero();
    }

    @Test
    void test_replan_missing_cron_triggers() throws Exception {
        // GIVEN
        final var cron1 = task2Id.newCron()
                .after(Duration.ofSeconds(1))
                .build();
        final var cron2 = task3Id.newCron()
                .after(Duration.ofHours(1))
                .build();

        subject.register(cron1);
        subject.register(cron2);
        assertThat(subject.countTriggers()).isEqualTo(2);

        var run = persistentTaskTestService.runAllDueTrigger(OffsetDateTime.now().plusMinutes(1));

        // WHEN
        assertThat(run).hasSize(1);
        assertThat(subject.countTriggers()).isOne();
        var count = subject.queueCronTrigger();

        // THEN - as by default we should create all which are needed
        assertThat(count).isOne();
    }

    @Test
    void test_cron_test_builder() throws Exception {
        // GIVEN
        final var anyState = "my-cool-state-" + UUID.randomUUID();
        final var cron = task3Id.newCron()
                .after(Duration.ofMinutes(1))
                .stateProvider(() -> anyState)
                .build();
        assertThat(subject.countTriggers(task3Id)).isZero();

        // WHEN
        var task = subject.register(cron);
        assertThat(task).isTrue();

        // THEN
        var triggers = subject.findAllTriggers(task3Id, Pageable.ofSize(10)).getContent();
        assertThat(triggers).hasSize(1);
        assertThat(triggers.get(0).getData().getRunAt()).isBetween(
                OffsetDateTime.now(), OffsetDateTime.now().plusMinutes(1));
        // AND
        persistentTaskTestService.runAllDueTrigger(OffsetDateTime.now().plusHours(1));
        asserts.awaitValueOnce(Task3.NAME + "::" + anyState);
    }

    @Test
    void test_register_annoation() throws Exception {
        // GIVEN
        var cronAnnotation = AnnotationUtils.findAnnotation(Task3.class, CronTrigger.class);

        // WHEN
        boolean res = subject.register(cronAnnotation, task3Id);

        // THEN
        assertThat(res).isTrue();
    }
}
