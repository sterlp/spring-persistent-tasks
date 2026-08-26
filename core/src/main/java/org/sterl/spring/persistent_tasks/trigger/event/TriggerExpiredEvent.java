package org.sterl.spring.persistent_tasks.trigger.event;

import java.io.Serializable;

import org.sterl.spring.persistent_tasks.shared.model.TriggerEntity;

/**
 * Fired if a trigger waiting for a signal expired before the signal arrived.
 * <p>
 * This is a terminal end state ({@link org.sterl.spring.persistent_tasks.api.TriggerStatus#EXPIRED_SIGNAL}):
 * the trigger is removed from the active table and historized, and the owning task's
 * {@code afterTriggerFailed} callback is invoked with a
 * {@link org.sterl.spring.persistent_tasks.task.exception.TriggerSignalExpiredException}.
 * </p>
 * <p>
 * Inside a transaction, it is save to join or listen for the <code>AFTER_COMMIT</code>
 * </p>
 */
public record TriggerExpiredEvent(Long id, TriggerEntity data, Serializable state) implements TriggerLifeCycleEvent {

    @Override
    public boolean isDone() {
        return true;
    }
}
