package org.sterl.spring.persistent_tasks.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.AbstractSpringTest.TaskConfig.Task3;
import org.sterl.spring.persistent_tasks.PersistentTaskService;
import org.sterl.spring.persistent_tasks.api.TriggerRequest;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;

class HistoryServiceTest extends AbstractSpringTest {

    @Autowired 
    private HistoryService subject;
    @Autowired
    private PersistentTaskService persistentTaskService;

    @Test
    void testReQueueTrigger() {
        // GIVEN
        final TriggerRequest<String> triggerRequest = Task3.ID.newUniqueTrigger("Hallo");
        var trigger = triggerService.run(triggerRequest, "test").get();
        asserts.assertValue(Task3.NAME + "::Hallo");
        
        // WHEN
        asserts.clear();
        final Optional<TriggerKey> newKey = subject.reQueue(trigger.getId(), OffsetDateTime.now());
        
        // THEN
        assertThat(newKey).isPresent();
        assertThat(newKey.get()).isEqualTo(trigger.key());
        // AND
        asserts.awaitValue(persistentTaskTestService::awaitRunningTriggers, Task3.NAME + "::Hallo");
        // AND
        assertThat(subject.countTriggers(trigger.getKey())).isEqualTo(2);
    }
    
    @Test
    void testTriggerHistory() throws TimeoutException, InterruptedException {
        // GIVEN
        final var trigger = Task3.ID.newUniqueTrigger("Hallo");
        triggerService.queue(trigger);
        persistentTaskTestService.runNextTrigger();
        // WHEN
        var triggers = subject.findAllDetailsForKey(trigger.key(), PageRequest.of(0, 100)).getContent();
        
        // AND
        assertThat(triggers).hasSize(3);
        assertThat(triggers.get(0).getData().getStatus()).isEqualTo(TriggerStatus.SUCCESS);
        assertThat(triggers.get(1).getData().getStatus()).isEqualTo(TriggerStatus.RUNNING);
        assertThat(triggers.get(2).getData().getStatus()).isEqualTo(TriggerStatus.WAITING);
    }
    
    @RepeatedTest(3)
    void testTriggerHistoryTrx() {
        // GIVEN
        final var trigger = Task3.ID.newUniqueTrigger("Hallo");
        persistentTaskService.queue(trigger);
        // WHEN
        hibernateAsserts.reset();
        schedulerService.triggerNextTasks().forEach(t -> {
            try {t.get();} catch (Exception ex) {throw new RuntimeException(ex);}
        });
        
        // THEN
        // 2 to get the work done
        // 1 for the success history
        hibernateAsserts.assertTrxCount(3);
        assertThat(subject.countTriggers(trigger.key())).isEqualTo(1);
        assertThat(subject.findAllDetailsForKey(trigger.key()).getTotalElements()).isEqualTo(3);
    }
}
