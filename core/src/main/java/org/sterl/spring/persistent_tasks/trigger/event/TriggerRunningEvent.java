package org.sterl.spring.persistent_tasks.trigger.event;

import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

public record TriggerRunningEvent(TriggerEntity trigger) implements TriggerLifeCycleEvent {

}
