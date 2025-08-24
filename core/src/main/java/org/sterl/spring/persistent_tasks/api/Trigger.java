package org.sterl.spring.persistent_tasks.api;

import java.time.OffsetDateTime;

import org.springframework.lang.Nullable;

import lombok.Data;

@Data
public class Trigger {
    
    /** just a unique id of this trigger */
    private Long id;
    
    private Long instanceId;
    
    /** the business key which is unique it is combination for triggers but not the history! */
    private TriggerKey key;
    
    @Nullable
    private String tag;
    
    @Nullable
    private String correlationId;
    
    @Nullable
    private String runningOn;

    private OffsetDateTime createdTime;

    private OffsetDateTime runAt;

    @Nullable
    private OffsetDateTime lastPing;

    @Nullable
    private OffsetDateTime start;

    @Nullable
    private OffsetDateTime end;

    private int executionCount = 0;

    /** priority, the higher a more priority it will get */
    private int priority = 4;

    private TriggerStatus status = TriggerStatus.WAITING;
    
    @Nullable
    private Long runningDurationInMs;

    @Nullable
    private Object state;

    @Nullable
    private String exceptionName;
    @Nullable
    private String lastException;
}
