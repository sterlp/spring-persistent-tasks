package org.sterl.spring.persistent_tasks.trigger;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.TaskId;

class CronTriggerTest extends AbstractSpringTest {

    @Autowired
    private TriggerService subject;
    
    @Autowired
    private TaskId<String> task1Id;
    @Autowired
    private TaskId<String> task2Id;

    @Test
    void testAddCron() throws Exception {
        // GIVEN
        final var cron = task1Id.newCron()
                .every(Duration.ofHours(1))
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
                .every(Duration.ofHours(1))
                .build();
        final var cron2 = task2Id.newCron()
                .every(Duration.ofHours(1))
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
        final var cron1 = task1Id.newCron()
                .every(Duration.ofSeconds(1))
                .build();
        final var cron2 = task2Id.newCron()
                .every(Duration.ofHours(1))
                .build();

        subject.register(cron1);
        subject.register(cron2);
        
        persistentTaskTestService.runAllDueTrigger(OffsetDateTime.now().plusMinutes(1));

        // WHEN
        assertThat(subject.countTriggers()).isOne();
        var count = subject.queueCronTrigger();

        // THEN - as by default we should create all which are needed
        assertThat(count).isOne();
    }
}
