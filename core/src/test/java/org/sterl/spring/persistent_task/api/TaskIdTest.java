package org.sterl.spring.persistent_task.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.sterl.spring.persistent_tasks.api.TaskId.TriggerBuilder;

class TaskIdTest {

    @Test
    void test() {
        assertThat(TriggerBuilder.newTrigger("foo").build().key().getId()).isNull();
        assertThat(TriggerBuilder.newTrigger("foo").build().key().getTaskName()).isEqualTo("foo");
    }
}
