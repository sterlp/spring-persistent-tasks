package org.sterl.spring.persistent_tasks.trigger.event;

import java.io.Serializable;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.sterl.spring.persistent_tasks.shared.model.HasTriggerData;
import org.sterl.spring.persistent_tasks.shared.model.TriggerData;

/**
 * Tag any events which are fired in case something changes on a trigger.
 * The attached data is already a copy, any modification to this data will have no effect.
 */
public interface TriggerLifeCycleEvent extends HasTriggerData {
    default TriggerData getData() {
        return data();
    }
    long id();
    @NonNull
    TriggerData data();
    @Nullable
    Serializable state();
    /**
     * @return <code>true</code> if the trigger was completed, either with success, error or canceled.
     */
    boolean isDone();
}
