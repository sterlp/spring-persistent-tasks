package org.sterl.spring.persistent_tasks.trigger.event;

import java.io.Serializable;

import org.sterl.spring.persistent_tasks.shared.model.TriggerEntity;

/**
 * Fired if a new trigger is added.
 * <p>
 * Inside a transaction, it is save to join or listen for the <code>AFTER_COMMIT</code>
 * </p>
 */
public record TriggerExpiredEvent(long id, TriggerEntity data, Serializable state) implements TriggerLifeCycleEvent {

    @Override
    public boolean isDone() {
        return false;
    }
}
