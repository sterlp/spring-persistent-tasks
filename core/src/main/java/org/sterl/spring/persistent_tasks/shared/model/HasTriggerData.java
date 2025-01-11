package org.sterl.spring.persistent_tasks.shared.model;

import java.io.Serializable;
import java.time.OffsetDateTime;

import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;

public interface HasTriggerData {
    TriggerData getData();
    
    default TriggerKey key() {
        return getData().getKey();
    }
    default TriggerStatus status() {
        return getData().getStatus();
    }
    default boolean isRunning() {
        return getData().getStatus() == TriggerStatus.RUNNING;
    }
    
    default TaskId<Serializable> newTaskId() {
        if (getData() == null || getData().getKey() == null) return null;
        return getData().getKey().toTaskId();
    }
    
    default boolean shouldRunInFuture() {
        return getData().getRunAt().isAfter(OffsetDateTime.now());
    }
}
