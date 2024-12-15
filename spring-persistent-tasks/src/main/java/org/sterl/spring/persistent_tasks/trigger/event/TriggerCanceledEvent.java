package org.sterl.spring.persistent_tasks.trigger.event;

import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

public record TriggerCanceledEvent(TriggerEntity trigger) implements TriggerLifeCycleEvent {

}
