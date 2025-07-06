package org.sterl.spring.persistent_tasks.trigger.component;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.TaskId;

class ReadTriggerComponentTest extends AbstractSpringTest {

    @Autowired
    ReadTriggerComponent subject;

    @Test
    void test() {
        TaskId<String> taskId = taskService.replace("foo", c -> asserts.info("foo"));
        triggerService.queue(taskId.newTrigger().id("1").correlationId("a1").build());
        triggerService.queue(taskId.newTrigger().id("2").correlationId("a1").build());
        triggerService.queue(taskId.newTrigger().id("3").correlationId("b1").build());

        // WHEN
        var result = trx.execute(t -> subject.searchGroupedTriggers(null, PageRequest.of(0, 10)));
        // THEN
        assertThat(result.getTotalElements()).isEqualTo(2);
        // AND
        assertThat(result.getContent().get(0).count()).isEqualTo(2);
        assertThat(result.getContent().get(0).groupByValue()).isEqualTo("a1");
        // AND
        assertThat(result.getContent().get(1).count()).isEqualTo(1);
        assertThat(result.getContent().get(1).groupByValue()).isEqualTo("b1");
        
        result = trx.execute(t -> subject.searchGroupedTriggers(null, PageRequest.of(0, 1)));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }
}
