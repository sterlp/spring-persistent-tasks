package org.sterl.spring.persistent_tasks.trigger.event;

import java.io.Serializable;

import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

public record TriggerAddedEvent(TriggerEntity trigger, Serializable state) implements TriggerLifeCycleEvent {

    public boolean isRunningOn(String name) {
        return trigger.isRunning() && name != null && name.equals(trigger.getRunningOn());
    }

}
