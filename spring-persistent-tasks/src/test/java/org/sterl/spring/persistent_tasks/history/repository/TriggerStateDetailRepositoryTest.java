package org.sterl.spring.persistent_tasks.history.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.history.model.TriggerStateHistoryEntity;
import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;

class TriggerStateDetailRepositoryTest extends AbstractSpringTest {

    @Autowired private TriggerStateDetailRepository subject;

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
        var result = subject.listHistoryOverview(PageRequest.of(0, 10));
        
        // THEN
        assertThat(result.getTotalElements()).isEqualTo(2L);
    }

    private TriggerStateHistoryEntity newHistoryEntry(TriggerStatus s, OffsetDateTime created) {
        var history = pm.manufacturePojo(TriggerStateHistoryEntity.class);
        history.setId(null);
        history.setCreatedTime(created);
        history.getData().setStatus(s);
        return history;
    }

}
