package org.sterl.spring.persistent_tasks.trigger.event;

import java.io.Serializable;

import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

/**
 * Event fired before a trigger is executed
 */
public record TriggerRunningEvent(TriggerEntity trigger, Serializable state) implements TriggerLifeCycleEvent {

}
