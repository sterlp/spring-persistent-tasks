package org.sterl.spring.persistent_tasks.trigger.event;

import java.io.Serializable;

import org.springframework.lang.Nullable;
import org.sterl.spring.persistent_tasks.api.event.PersistentTasksEvent;
import org.sterl.spring.persistent_tasks.shared.model.HasTrigger;
import org.sterl.spring.persistent_tasks.shared.model.TriggerEntity;

/**
 * Tag any events which are fired in case something changes on a trigger.
 * The attached data is already a copy, any modification to this data will have no effect.
 */
public interface TriggerLifeCycleEvent extends HasTrigger, PersistentTasksEvent {
    default TriggerEntity getData() {
        return data();
    }
    long id();
    TriggerEntity data();
    @Nullable
    Serializable state();
    /**
     * @return <code>true</code> if the trigger was completed, either with success, error or canceled.
     */
    boolean isDone();
}
