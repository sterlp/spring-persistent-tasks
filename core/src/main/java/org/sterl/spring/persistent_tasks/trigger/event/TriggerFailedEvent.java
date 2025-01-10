package org.sterl.spring.persistent_tasks.trigger.event;

import java.io.Serializable;

import org.sterl.spring.persistent_tasks.shared.model.TriggerData;

/**
 * <p>
 * Inside a transaction, it is save to join or listen for the <code>AFTER_COMMIT</code>
 * </p>
 */
public record TriggerFailedEvent(long id, TriggerData data, Serializable state, Exception exception) implements TriggerLifeCycleEvent {

}
