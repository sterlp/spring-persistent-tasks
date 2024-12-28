package org.sterl.spring.persistent_tasks.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.AbstractSpringTest.TaskConfig.Task3;
import org.sterl.spring.persistent_tasks.api.AddTriggerRequest;
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
        runTriggersAndWait();
        asserts.assertValue(Task3.NAME + "::Hallo");
        // AND
        assertThat(subject.countTriggers(trigger.getKey())).isEqualTo(2);
    }
}
