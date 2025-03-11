package org.sterl.spring.persistent_tasks.api;

import java.time.OffsetDateTime;

import lombok.Data;

@Data
public class Trigger {
    
    /** just a unique id of this trigger */
    private Long id;
    
    private Long instanceId;
    
    /** the business key which is unique it is combination for triggers but not the history! */
    private TriggerKey key;
    
    private String correlationId;
    
    private String runningOn;

    private OffsetDateTime createdTime = OffsetDateTime.now();

    private OffsetDateTime runAt = OffsetDateTime.now();

    private OffsetDateTime start;

    private OffsetDateTime end;

    private int executionCount = 0;

    /** priority, the higher a more priority it will get */
    private int priority = 4;

    private TriggerStatus status = TriggerStatus.WAITING;
    
    private Long runningDurationInMs;

    private Object state;

    private String exceptionName;
    private String lastException;
}
