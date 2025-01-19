package org.sterl.spring.persistent_tasks.trigger.model;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.api.PersistentTask;
import org.sterl.spring.persistent_tasks.shared.model.HasTriggerData;
import org.sterl.spring.persistent_tasks.shared.model.TriggerData;
import org.sterl.spring.persistent_tasks.trigger.component.EditTriggerComponent;

public record RunTaskWithStateCommand (
        PersistentTask<Serializable> task,
        Optional<TransactionTemplate> trx,
        Serializable state,
        TriggerEntity trigger) implements HasTriggerData {

    public Optional<TriggerEntity> execute(EditTriggerComponent editTrigger) {
        if (trx.isPresent()) {
            System.err.println("IN TRX");
            return trx.get().execute(t -> runTask(editTrigger));
        } else {
            System.err.println("NO TRX");
            return runTask(editTrigger);
        }
    }

    private Optional<TriggerEntity> runTask(EditTriggerComponent editTrigger) {
        editTrigger.triggerIsNowRunning(trigger, state);

        task.accept(state);

        var result = editTrigger.completeTaskWithSuccess(trigger.getKey(), state);
        editTrigger.deleteTrigger(trigger);

        return result;
    }

    @Override
    public TriggerData getData() {
        return trigger.getData();
    }
}
