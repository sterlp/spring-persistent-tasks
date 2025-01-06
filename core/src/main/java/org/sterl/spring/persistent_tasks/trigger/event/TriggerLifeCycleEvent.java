package org.sterl.spring.persistent_tasks.trigger.event;

import java.io.Serializable;

import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

/**
 * Tag any events which are fired in case something changes on a trigger
 */
public interface TriggerLifeCycleEvent {
    TriggerEntity trigger();
    Serializable state();
}
