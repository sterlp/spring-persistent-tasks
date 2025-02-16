package org.sterl.spring.persistent_tasks.api.task;

import java.io.Serializable;

import org.sterl.spring.persistent_tasks.api.TriggerKey;

import lombok.Data;

@Data
public class RunningTrigger<T extends Serializable> {
    private final TriggerKey key;
    private final String correlationId;
    private final int executionCount;
    private final T data;
}
