package org.sterl.spring.persistent_tasks.trigger.event;

import java.io.Serializable;

import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

public record TriggerCanceledEvent(TriggerEntity trigger, Serializable state) implements TriggerLifeCycleEvent {
}
