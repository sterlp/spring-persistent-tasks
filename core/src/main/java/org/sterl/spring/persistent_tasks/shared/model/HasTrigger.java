package org.sterl.spring.persistent_tasks.shared.model;

import java.io.Serializable;

import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;

public interface HasTrigger {
    TriggerEntity getData();
    
    default TriggerKey key() {
        return getData().getKey();
    }
    default int executionCount() {
        return getData().getExecutionCount();
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
        if (status() == TriggerStatus.AWAITING_SIGNAL) return true;
        if (getData().getRunAt() == null) return false;
        return getData().getRunAt().toInstant().toEpochMilli() > System.currentTimeMillis();
    }
}
