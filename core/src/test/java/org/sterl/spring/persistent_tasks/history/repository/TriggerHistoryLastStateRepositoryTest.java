package org.sterl.spring.persistent_tasks.history.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryLastStateEntity;
import org.sterl.spring.persistent_tasks.shared.model.TriggerData;

class TriggerHistoryLastStateRepositoryTest extends AbstractSpringTest {

    final AtomicLong idGenerator = new AtomicLong(0);
    @Autowired 
    private TriggerHistoryLastStateRepository subject;
    
    @Test
    void testListTriggerStatus() {
        // GIVEN
        subject.deleteAllInBatch();
        createStatus(new TriggerKey("1", "task1"), TriggerStatus.SUCCESS);
        createStatus(new TriggerKey("2", "task1"), TriggerStatus.SUCCESS);
        createStatus(new TriggerKey("3", "task1"), TriggerStatus.FAILED);
        createStatus(new TriggerKey("4", "task2"), TriggerStatus.SUCCESS);
        createStatus(new TriggerKey("5", "task2"), TriggerStatus.CANCELED);
        assertThat(subject.count()).isEqualTo(5);
        
        // THEN
        var result = subject.listTriggerStatus();
        
        // WHEN
        assertThat(result.size()).isEqualTo(4);
        // AND
        var i = 0;
        assertThat(result.get(i).taskName()).isEqualTo("task1");
        assertThat(result.get(i).status()).isEqualTo(TriggerStatus.FAILED);
        assertThat(result.get(i).executionCount()).isEqualTo(1L);
        // AND
        i = 1;
        assertThat(result.get(i).taskName()).isEqualTo("task1");
        assertThat(result.get(i).status()).isEqualTo(TriggerStatus.SUCCESS);
        assertThat(result.get(i).executionCount()).isEqualTo(2L);
        // AND
        i = 2;
        assertThat(result.get(i).taskName()).isEqualTo("task2");
        assertThat(result.get(i).status()).isEqualTo(TriggerStatus.CANCELED);
        assertThat(result.get(i).executionCount()).isEqualTo(1L);
    }
    
    private TriggerHistoryLastStateEntity createStatus(TriggerKey key, TriggerStatus status) {
        final var now = OffsetDateTime.now();
        final var isCancel = status == TriggerStatus.CANCELED;

        TriggerHistoryLastStateEntity result = new TriggerHistoryLastStateEntity();
        result.setId(idGenerator.incrementAndGet());
        result.setData(TriggerData
                .builder()
                .start(isCancel ? null : now.minusMinutes(1))
                .end(isCancel ? null : now)
                .createdTime(now)
                .key(key)
                .correlationId(UUID.randomUUID().toString())
                .status(status)
                .runningDurationInMs(isCancel ? null : 600L)
                .build()
            );
        
        return subject.save(result);
    }

}
