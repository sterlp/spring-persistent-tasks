package org.sterl.spring.persistent_tasks.trigger.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.api.task.PersistentTask;
import org.sterl.spring.persistent_tasks.api.task.RunningTrigger;
import org.sterl.spring.persistent_tasks.shared.model.HasTriggerData;
import org.sterl.spring.persistent_tasks.shared.model.TriggerData;
import org.sterl.spring.persistent_tasks.trigger.component.EditTriggerComponent;

public record RunTaskWithStateCommand (
        PersistentTask<Serializable> task,
        Optional<TransactionTemplate> trx,
        Serializable state,
        TriggerEntity trigger,
        RunningTrigger<Serializable> runningTrigger) implements HasTriggerData {
    
    public RunTaskWithStateCommand(
        PersistentTask<Serializable> task,
        Optional<TransactionTemplate> trx,
        Serializable state,
        TriggerEntity trigger) {
        
        this(task, trx, state, trigger,
            new RunningTrigger<>(
                    trigger.getKey(),
                    trigger.getData().getCorrelationId(),
                    trigger.getData().getExecutionCount(),
                    state
                ));
    }

    public Optional<TriggerEntity> execute(EditTriggerComponent editTrigger) {
        if (trx.isPresent()) {
            return trx.get().execute(t -> runTask(editTrigger));
        } else {
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
    
    boolean hasValues(Collection<?> elements) {
        return elements != null && !elements.isEmpty();
    }

    @Override
    public TriggerData getData() {
        return trigger.getData();
    }
}
