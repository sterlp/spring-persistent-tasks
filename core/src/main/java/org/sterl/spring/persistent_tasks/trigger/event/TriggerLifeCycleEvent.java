package org.sterl.spring.persistent_tasks.trigger.event;

import java.io.Serializable;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

/**
 * Tag any events which are fired in case something changes on a trigger
 */
public interface TriggerLifeCycleEvent {
    default TriggerKey key() {
        return trigger().getKey();
    }
    @NonNull
    TriggerEntity trigger();
    @Nullable
    Serializable state();
}
