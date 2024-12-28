package org.sterl.spring.persistent_tasks.shared.model;

import java.util.EnumSet;
import java.util.Set;

public enum TriggerStatus {
    NEW,
    RUNNING,
    SUCCESS,
    FAILED,
    CANCELED
    ;
    public static final Set<TriggerStatus> ACTIVE_STATES = EnumSet.of(NEW, RUNNING);
    public static final Set<TriggerStatus> END_STATES = EnumSet.of(SUCCESS, FAILED, CANCELED);
}
