package org.sterl.spring.persistent_tasks.api;

import java.time.OffsetDateTime;

import org.sterl.spring.persistent_tasks.shared.model.TriggerStatus;

import lombok.Data;

@Data
public class TriggerView {
    
    private String key;
    
    private TriggerId id;
    
    private String runningOn;

    private OffsetDateTime createdTime = OffsetDateTime.now();

    private OffsetDateTime runAt = OffsetDateTime.now();

    private OffsetDateTime start;

    private OffsetDateTime end;

    private int executionCount = 0;

    /** priority, the higher a more priority it will get */
    private int priority = 4;

    private TriggerStatus status = TriggerStatus.NEW;
    
    private Long runningDurationInMs;

    private Object state;

    private String exceptionName;
    private String lastException;
}
