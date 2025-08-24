package org.sterl.spring.persistent_tasks.api;

import java.time.OffsetDateTime;

import org.springframework.lang.Nullable;

import lombok.Data;

@Data
public class HistoryTrigger {
    
    /** just a unique id of this trigger */
    private Long id;
    
    private Long instanceId;
    
    /** the business key which is unique it is combination for triggers but not the history! */
    private TriggerKey key;
    
    private OffsetDateTime createdTime;

    @Nullable
    private OffsetDateTime start;

    private int executionCount = 0;

    private TriggerStatus status;

    @Nullable
    private String message;
}
