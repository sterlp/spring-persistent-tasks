package org.sterl.spring.persistent_tasks;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.event.TriggerTaskCommand;
import org.sterl.spring.persistent_tasks.api.task.RunningTriggerContextHolder;

class PersistentTaskServiceTest extends AbstractSpringTest {
    @Autowired
    private PersistentTaskService subject;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    
    @Test
    void testChainedTasks() throws Exception {
        // GIVEN
        final AtomicReference<String> correlationFound = new AtomicReference<>();

        final TaskId<Integer> task1 = taskService.replace("chainTask1", s -> {
            var state = RunningTriggerContextHolder.getContext();
            asserts.info(state.getData() + "::chainTask1");
            eventPublisher.publishEvent(
                    TriggerTaskCommand.of("chainTask2", state.getData() + "::chainTask1",
                    UUID.randomUUID().toString())); // should be ignored!
        });

        taskService.replace("chainTask2", s -> {
            var state = RunningTriggerContextHolder.getContext();
            correlationFound.set(state.getCorrelationId());
            asserts.info("chainTask1::" + state.getData());
            assertThat(state.getCorrelationId()).isEqualTo(RunningTriggerContextHolder.getCorrelationId());
        });
        final var correlationId = UUID.randomUUID().toString();

        // WHEN
        subject.runOrQueue(task1.newTrigger(234).correlationId(correlationId).build());

        // THEN
        asserts.awaitOrdered("234::chainTask1", "chainTask1::234::chainTask1");
        assertThat(correlationId).isEqualTo(correlationFound.get());
        // AND
        var trigger= subject.findAllTriggerByCorrelationId(correlationId);
        assertThat(trigger).hasSize(2);
        assertThat(trigger.get(0).getCorrelationId()).isEqualTo(correlationId);
        assertThat(trigger.get(1).getCorrelationId()).isEqualTo(correlationId);
    }
}
