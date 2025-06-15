package org.sterl.spring.persistent_tasks.trigger.event;

import java.io.Serializable;
import java.time.OffsetDateTime;

import org.sterl.spring.persistent_tasks.shared.model.TriggerEntity;

/**
 * <p>
 * Inside a transaction, it is save to join or listen for the <code>AFTER_COMMIT</code>
 * </p>
 */
public record TriggerFailedEvent(long id, 
        TriggerEntity data, Serializable state, 
        Exception exception, 
        OffsetDateTime retryAt) implements TriggerLifeCycleEvent {

    @Override
    public boolean isDone() {
        return retryAt == null;
    }
}
