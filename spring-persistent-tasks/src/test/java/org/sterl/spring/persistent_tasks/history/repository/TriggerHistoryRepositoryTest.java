package org.sterl.spring.persistent_tasks.history.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.HistoryOverview;
import org.sterl.spring.persistent_tasks.history.model.TriggerHistoryEntity;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;

class TriggerHistoryRepositoryTest extends AbstractSpringTest {

    @Autowired TriggerHistoryRepository subject;
    
    @Test
    void testGrouping() {
        // GIVEN
        var history1 = newHistoryEntry(TriggerStatus.RUNNING, OffsetDateTime.now());
        subject.save(history1);
        var history2 = newHistoryEntry(TriggerStatus.RUNNING, OffsetDateTime.now());
        history2.setInstanceId(history1.getInstanceId());
        history2.getData().setKey(history1.getKey());
        subject.save(history2);
        // AND
        subject.save(newHistoryEntry(TriggerStatus.RUNNING, OffsetDateTime.now()));

        // WHEN
        var result = subject.listHistoryOverview(
                PageRequest.of(0, 10).withSort(Direction.DESC, "data.end"));
        
        // THEN
        assertThat(result.getTotalElements()).isEqualTo(2L);
    }

    @Test
    void testStatus() {
        // GIVEN
        var history1 = newHistoryEntry(TriggerStatus.RUNNING, OffsetDateTime.now().minusMinutes(1));
        subject.save(history1);
        var history2 = newHistoryEntry(TriggerStatus.FAILED, OffsetDateTime.now());
        history2.setInstanceId(history1.getInstanceId());
        subject.save(history2);

        var result = subject.listHistoryOverview(
                PageRequest.of(0, 10));
        
        // THEN
        assertThat(result.getContent().get(0).status()).isEqualTo(TriggerStatus.FAILED);
    }

    private TriggerHistoryEntity newHistoryEntry(TriggerStatus s, OffsetDateTime created) {
        var history = pm.manufacturePojo(TriggerHistoryEntity.class);
        history.setId(null);
        history.setCreatedTime(created);
        history.getData().setStatus(s);
        return history;
    }

}
