package org.sterl.spring.persistent_tasks.trigger.model;

import java.io.Serializable;
import java.util.Collection;
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
        TriggerEntity trigger,
        RunningTrigger<Serializable> runningTrigger) implements HasTriggerData {
    
    public RunTaskWithStateCommand(ApplicationEventPublisher eventPublisher,
        PersistentTaskBase<Serializable> task,
        Optional<TransactionTemplate> trx,
        Serializable state,
        TriggerEntity trigger) {
        
        this(eventPublisher, task, trx, state, trigger,
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

        Collection<AddTriggerRequest<Serializable>> nextTriggers = null;
        if (task instanceof ComplexPersistentTask<Serializable, Serializable> complexTask) {
            nextTriggers = complexTask.accept(runningTrigger);
        } else if (task instanceof PersistentTask<Serializable> simpleTask) {
            simpleTask.accept(state); // Direct state handling
        } else {
            throw new IllegalStateException("Unsupported task type: " + task.getClass());
        }

        var result = editTrigger.completeTaskWithSuccess(trigger.getKey(), state);
        editTrigger.deleteTrigger(trigger);

        if (hasValues(nextTriggers)) eventPublisher.publishEvent(TriggerTaskCommand.of(nextTriggers));

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
