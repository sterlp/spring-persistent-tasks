package org.sterl.spring.persistent_tasks.trigger.event;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;

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
    
    Long id();
    
    default Long getId() {
        return id();
    }
    
    TriggerEntity data();
    
    @Nullable
    Serializable state();
    
    /**
     * If a trigger is done, it finished it's execution either successfully or with an final error without retries left.
     * 
     * @return <code>true</code> if the trigger was completed, either with success, error or canceled.
     */
    boolean isDone();
    
    default long timePassedMs() {
        var result = data().getRunningDurationInMs();
        if (result == null && data().getStart() != null) {
            result = Duration.between(data().getStart(), OffsetDateTime.now()).toMillis();
        }
        return result == null ? 0 : result.longValue();
    }
}
