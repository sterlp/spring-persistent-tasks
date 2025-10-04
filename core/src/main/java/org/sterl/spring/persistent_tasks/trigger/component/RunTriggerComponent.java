package org.sterl.spring.persistent_tasks.trigger.component;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.api.task.RunningTriggerContextHolder;
import org.sterl.spring.persistent_tasks.task.TaskService;
import org.sterl.spring.persistent_tasks.trigger.model.RunTaskWithStateCommand;
import org.sterl.spring.persistent_tasks.trigger.model.RunningTriggerEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RunTriggerComponent {

    private final TaskService taskService;
    private final FailTriggerComponent failTrigger;
    private final EditTriggerComponent editTrigger;
    private final StateSerializationComponent stateSerialization;

    /**
     * Will execute the given {@link RunningTriggerEntity} and handle any errors
     * etc.
     */
    @Transactional(propagation = Propagation.NEVER)
    public Optional<RunningTriggerEntity> execute(RunningTriggerEntity trigger) {
        if (trigger == null) {
            return Optional.empty();
        }

        final var runTaskWithState = buildTaskWithStateFor(trigger);
        // something went really wrong this trigger is crap
        if (runTaskWithState == null)
            return Optional.of(trigger);

        try {
            if (OffsetDateTime.now().isAfter(trigger.getData().getRunAt())) {
                log.debug("Running {} for {} time.", trigger.key(), trigger.executionCount());
            } else {
                log.info("Running to early {} start should be {} for {} time.",
                        trigger.key(), trigger.getData().getRunAt(), trigger.executionCount());
            }

            RunningTriggerContextHolder.setContext(runTaskWithState.runningTrigger());
            return runTaskWithState.execute(editTrigger);
        } catch (Exception e) {
            return failTrigger.execute(runTaskWithState, e);
        } finally {
            RunningTriggerContextHolder.clearContext();
        }
    }

    @Nullable
    private RunTaskWithStateCommand buildTaskWithStateFor(RunningTriggerEntity trigger) {
        try {
            final var task = taskService.assertIsKnown(trigger.newTaskId());
            final var trx = taskService.getTransactionTemplateIfJoinable(task);
            final var state = stateSerialization.deserialize(trigger.getData());
            return new RunTaskWithStateCommand(task, trx, state, trigger);
        } catch (Exception e) {
            failTrigger.execute(trigger, e);
            return null;
        }
    }
}
