package org.sterl.spring.persistent_tasks.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.AbstractSpringTest.TaskConfig.Task3;
import org.sterl.spring.persistent_tasks.PersistentTaskService;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerRequest;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.history.component.TriggerHistoryComponent;
import org.sterl.spring.persistent_tasks.shared.model.TriggerEntity;

import com.github.f4b6a3.uuid.UuidCreator;

class HistoryServiceTest extends AbstractSpringTest {

    Random random = new Random();
    @Autowired 
    private HistoryService subject;
    @Autowired
    private PersistentTaskService persistentTaskService;
    
    @Autowired
    private TriggerHistoryComponent triggerHistory;

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
        final var trigger = triggerService.queue(Task3.ID.newUniqueTrigger("Hallo"));
        persistentTaskTestService.runNextTrigger();
        // WHEN
        var triggers = subject.findAllDetailsForInstance(trigger.getId(), 
                PageRequest.of(0, 100)).getContent();
        
        // AND
        assertThat(triggers).hasSize(3);
        assertThat(triggers.get(0).getStatus()).isEqualTo(TriggerStatus.SUCCESS);
        assertThat(triggers.get(1).getStatus()).isEqualTo(TriggerStatus.RUNNING);
        assertThat(triggers.get(2).getStatus()).isEqualTo(TriggerStatus.WAITING);
    }
    
    @RepeatedTest(3)
    void testTriggerHistoryTrx() {
        // GIVEN
        final var trigger = persistentTaskService.queue(Task3.ID.newUniqueTrigger("Hallo"));
        // WHEN
        hibernateAsserts.reset();
        schedulerService.triggerNextTasks().forEach(t -> {
            try {t.get();} catch (Exception ex) {throw new RuntimeException(ex);}
        });
        
        // THEN
        // 2 to get the work done
        // 1 for the success history
        hibernateAsserts.assertTrxCount(3);
        assertThat(subject.countTriggers(trigger)).isEqualTo(1);
    }
    
    @Test
    void testTriggerDelete() {
        // GIVEN
        // WHEN
        subject.deleteAll();
    }
    
    @Test
    void testDeleteByDate() {
        // GIVEN
        var now = OffsetDateTime.now();
        var task = UuidCreator.getTimeOrderedEpochFast().toString();
        var instanceId = random.nextLong(0, 99999) + now.toEpochSecond();
        
        trx.executeWithoutResult(t -> {
            triggerHistory.execute(instanceId, TriggerEntity.builder()
                    .key(new TriggerKey("123", task))
                    .createdTime(now)
                    .build(), true);
            
            // this one we keep
            triggerHistory.execute(instanceId, TriggerEntity.builder()
                    .key(new TriggerKey("123", task))
                    .createdTime(now.plusMinutes(1))
                    .build(), true);
        });

        // WHEN
        var count = subject.deleteAllOlderThan(now.minusSeconds(1));
        
        // THEN
        assertThat(count).isZero();
        
        // WHEN
        count = subject.deleteAllOlderThan(now.plusSeconds(30));
        // THEN
        assertThat(count).isEqualTo(3);
    }
}
