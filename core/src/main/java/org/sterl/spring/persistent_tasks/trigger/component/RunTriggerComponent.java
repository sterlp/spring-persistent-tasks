package org.sterl.spring.persistent_tasks.trigger.component;

import java.util.Optional;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.api.task.RunningTriggerContextHolder;
import org.sterl.spring.persistent_tasks.task.TaskService;
import org.sterl.spring.persistent_tasks.trigger.model.RunTaskWithStateCommand;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RunTriggerComponent {

    private final TaskService taskService;
    private final FailTriggerComponent failTrigger;
    private final EditTriggerComponent editTrigger;
    private final StateSerializer serializer = new StateSerializer();

    /**
     * Will execute the given {@link TriggerEntity} and handle any errors etc.
     */
    @Transactional(propagation = Propagation.NEVER)
    public Optional<TriggerEntity> execute(TriggerEntity trigger) {
        if (trigger == null) {
            return Optional.empty();
        }

        final var runTaskWithState = buildTaskWithStateFor(trigger);
        // something went really wrong this trigger is crap
        if (runTaskWithState == null) return Optional.of(trigger);

        try {
            RunningTriggerContextHolder.setContext(runTaskWithState.runningTrigger());
            return runTaskWithState.execute(editTrigger);
        } catch (Exception e) {
            return failTrigger.execute(runTaskWithState, e);
        } finally {
            RunningTriggerContextHolder.clearContext();
        }
    }

    @Nullable
    private RunTaskWithStateCommand buildTaskWithStateFor(TriggerEntity trigger) {
        try {
            final var task = taskService.assertIsKnown(trigger.newTaskId());
            final var trx = taskService.getTransactionTemplate(task);
            final var state = serializer.deserialize(trigger.getData().getState());
            return new RunTaskWithStateCommand(task, trx, state, trigger);
        } catch (Exception e) {
            failTrigger.execute(trigger, e);
            return null;
        }
    }
}
