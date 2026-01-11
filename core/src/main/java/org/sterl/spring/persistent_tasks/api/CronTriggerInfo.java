package org.sterl.spring.persistent_tasks.api;

import org.springframework.lang.Nullable;

import lombok.Data;

/**
 * Representing a cron trigger definition registered in-memory.
 */
@Data
public class CronTriggerInfo {

    /** Unique ID of this cron trigger */
    private String id;

    /** The task that this cron trigger executes */
    private String taskName;

    /** The schedule (cron expression or interval description) */
    private String schedule;

    /** Tag for the created triggers */
    @Nullable
    private String tag;

    /** Priority for the created triggers */
    private int priority = 5;

    /** Whether this cron trigger is suspended (not creating new triggers) */
    private boolean suspended = false;

    /** Whether this cron trigger has a state provider configured */
    private boolean hasStateProvider = false;
}
