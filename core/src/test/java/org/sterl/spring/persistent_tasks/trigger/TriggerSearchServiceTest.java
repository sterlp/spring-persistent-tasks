package org.sterl.spring.persistent_tasks.trigger;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TriggerSearch;

import com.github.f4b6a3.uuid.UuidCreator;

class TriggerSearchServiceTest extends AbstractSpringTest {

    @Autowired
    private TriggerService subject;

    @Test
    void testSearchByCorrelationId() {
        // GIVEN
        TaskId<String> t1 = taskService.replace("foo1", asserts::info);
        TaskId<String> t2 = taskService.replace("foo2", asserts::info);
        
        var cId1 = UuidCreator.getTimeOrderedEpochFast().toString();
        var cId2 = UuidCreator.getTimeOrderedEpochFast().toString();
        var key1 = subject.queue(t1.newTrigger().correlationId(cId1).build()).getKey();
        subject.queue(t1.newTrigger().correlationId(cId2).build()).getKey();
        subject.queue(t2.newTrigger().build()).getKey();

        // WHEN
        final var result = subject.searchTriggers(TriggerSearch.byCorrelationId(cId1), Pageable.ofSize(10));

        // THEN
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getKey()).isEqualTo(key1);
    }
}
