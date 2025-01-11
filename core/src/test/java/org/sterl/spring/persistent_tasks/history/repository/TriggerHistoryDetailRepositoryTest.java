package org.sterl.spring.persistent_tasks.history.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryDetailEntity;

class TriggerHistoryDetailRepositoryTest extends AbstractSpringTest {

    @Autowired private TriggerHistoryDetailRepository subject;

    @Test
    void testGrouping() {
        // GIVEN
        var history1 = newHistoryEntry(TriggerStatus.FAILED, OffsetDateTime.now());
        subject.save(history1);
        var history2 = newHistoryEntry(TriggerStatus.SUCCESS, OffsetDateTime.now());
        history2.setInstanceId(history1.getInstanceId());
        history2.getData().setKey(history1.getKey());
        subject.save(history2);
        // AND
        subject.save(newHistoryEntry(TriggerStatus.RUNNING, OffsetDateTime.now()));

        // WHEN
        var result = subject.listTaskHistoryOverview();
        
        // THEN
        assertThat(result.size()).isEqualTo(2L);
    }

    private TriggerHistoryDetailEntity newHistoryEntry(TriggerStatus s, OffsetDateTime created) {
        var history = pm.manufacturePojo(TriggerHistoryDetailEntity.class);
        history.setId(null);
        history.setCreatedTime(created);
        history.getData().setStatus(s);
        history.getData().setStart(OffsetDateTime.now());
        history.getData().setEnd(OffsetDateTime.now().plusSeconds(10));
        return history;
    }

}
