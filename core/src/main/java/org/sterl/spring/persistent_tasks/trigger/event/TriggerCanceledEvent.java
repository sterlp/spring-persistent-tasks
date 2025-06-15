package org.sterl.spring.persistent_tasks.trigger.event;

import java.io.Serializable;

import org.sterl.spring.persistent_tasks.shared.model.TriggerEntity;

/**
 * Fired if a trigger could be canceled before it is running.
 * <p>
 * Inside a transaction, it is save to join or listen for the <code>AFTER_COMMIT</code>
 * </p>
 */
public record TriggerCanceledEvent(long id, TriggerEntity data, Serializable state) implements TriggerLifeCycleEvent {

    @Override
    public boolean isDone() {
        return true;
    }
}
