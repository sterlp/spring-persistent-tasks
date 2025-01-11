package org.sterl.spring.persistent_tasks.api;

import java.util.EnumSet;
import java.util.Set;

public enum TriggerStatus {
    WAITING,
    RUNNING,
    SUCCESS,
    FAILED,
    CANCELED
    ;
    public static final Set<TriggerStatus> ACTIVE_STATES = EnumSet.of(WAITING, RUNNING);
    public static final Set<TriggerStatus> END_STATES = EnumSet.of(SUCCESS, FAILED, CANCELED);
}
