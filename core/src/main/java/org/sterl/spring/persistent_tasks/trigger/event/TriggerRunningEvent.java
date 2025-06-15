package org.sterl.spring.persistent_tasks.trigger.event;

import java.io.Serializable;

import org.sterl.spring.persistent_tasks.shared.model.TriggerEntity;

/**
 * Event fired before a trigger is executed
 * <p>
 * This event is maybe not in a transaction and so a transactional event listener will not work.
 * </p>
 */
public record TriggerRunningEvent(long id, TriggerEntity data, Serializable state, String runningOn) implements TriggerLifeCycleEvent {

    public boolean isRunningOn(String name) {
        return isRunning() && name != null && name.equals(runningOn);
    }

    @Override
    public boolean isDone() {
        return false;
    }
}
