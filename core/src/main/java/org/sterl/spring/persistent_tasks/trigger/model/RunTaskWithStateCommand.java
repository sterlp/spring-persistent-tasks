package org.sterl.spring.persistent_tasks.trigger.model;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.api.AddTriggerRequest;
import org.sterl.spring.persistent_tasks.api.event.TriggerTaskCommand;
import org.sterl.spring.persistent_tasks.api.task.ComplexPersistentTask;
import org.sterl.spring.persistent_tasks.api.task.PersistentTask;
import org.sterl.spring.persistent_tasks.api.task.PersistentTaskBase;
import org.sterl.spring.persistent_tasks.api.task.RunningTrigger;
import org.sterl.spring.persistent_tasks.shared.model.HasTriggerData;
import org.sterl.spring.persistent_tasks.shared.model.TriggerData;
import org.sterl.spring.persistent_tasks.trigger.component.EditTriggerComponent;

public record RunTaskWithStateCommand (
        ApplicationEventPublisher eventPublisher,
        PersistentTaskBase<Serializable> task,
        Optional<TransactionTemplate> trx,
        Serializable state,
        TriggerEntity trigger) implements HasTriggerData {

    public Optional<TriggerEntity> execute(EditTriggerComponent editTrigger) {
        if (trx.isPresent()) {
            return trx.get().execute(t -> runTask(editTrigger));
        } else {
            return runTask(editTrigger);
        }
    }

    private Optional<TriggerEntity> runTask(EditTriggerComponent editTrigger) {
        editTrigger.triggerIsNowRunning(trigger, state);

        AddTriggerRequest<Serializable> nextTrigger = null;
        if (task instanceof ComplexPersistentTask<Serializable> complexTask) {
            final var runningTrigger = new RunningTrigger<>(
                key(),
                executionCount(),
                state
            );
            nextTrigger = complexTask.accept(runningTrigger);
        } else if (task instanceof PersistentTask<Serializable> simpleTask) {
            simpleTask.accept(state); // Direct state handling
        } else {
            throw new IllegalStateException("Unsupported task type: " + task.getClass());
        }

        var result = editTrigger.completeTaskWithSuccess(trigger.getKey(), state);
        editTrigger.deleteTrigger(trigger);

        if (nextTrigger != null) eventPublisher.publishEvent(TriggerTaskCommand.of(nextTrigger));

        return result;
    }

    @Override
    public TriggerData getData() {
        return trigger.getData();
    }
}
