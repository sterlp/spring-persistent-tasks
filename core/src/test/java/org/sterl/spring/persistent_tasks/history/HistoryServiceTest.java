package org.sterl.spring.persistent_tasks.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.AbstractSpringTest.TaskConfig.Task3;
import org.sterl.spring.persistent_tasks.api.AddTriggerRequest;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

class HistoryServiceTest extends AbstractSpringTest {

    @Autowired HistoryService subject;

    @Test
    void testReQueueTrigger() {
        // GIVEN
        final AddTriggerRequest<String> triggerRequest = Task3.ID.newUniqueTrigger("Hallo");
        var trigger = triggerService.run(triggerRequest, "test").get();
        asserts.assertValue(Task3.NAME + "::Hallo");
        
        // WHEN
        asserts.clear();
        final Optional<TriggerEntity> t = subject.reQueue(trigger.getId(), OffsetDateTime.now());
        
        // THEN
        assertThat(t).isPresent();
        // AND
        persistentTaskService.executeTriggersAndWait();
        asserts.assertValue(Task3.NAME + "::Hallo");
        // AND
        assertThat(subject.countTriggers(trigger.getKey())).isEqualTo(2);
    }
    
    @Test
    void testTriggerHistory() throws TimeoutException, InterruptedException {
        // GIVEN
        final var trigger = Task3.ID.newUniqueTrigger("Hallo");
        triggerService.queue(trigger);
        persistentTaskService.executeTriggersAndWait();
        // WHEN
        var triggers = subject.findAllDetailsForKey(trigger.key(), PageRequest.of(0, 100)).getContent();
        
        // AND
        assertThat(triggers).hasSize(3);
        assertThat(triggers.get(0).getData().getStatus()).isEqualTo(TriggerStatus.SUCCESS);
        assertThat(triggers.get(1).getData().getStatus()).isEqualTo(TriggerStatus.RUNNING);
        assertThat(triggers.get(2).getData().getStatus()).isEqualTo(TriggerStatus.WAITING);
    }
}
