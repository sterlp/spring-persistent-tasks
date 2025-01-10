package org.sterl.spring.persistent_tasks.trigger.event;

import java.io.Serializable;

import org.sterl.spring.persistent_tasks.shared.model.TriggerData;

/**
 * Fired if a trigger could be canceled before it is running.
 * <p>
 * Inside a transaction, it is save to join or listen for the <code>AFTER_COMMIT</code>
 * </p>
 */
public record TriggerCanceledEvent(long id, TriggerData data, Serializable state) implements TriggerLifeCycleEvent {
}
