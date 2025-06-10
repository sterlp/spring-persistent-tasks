package org.sterl.spring.persistent_tasks.api;

import java.util.EnumSet;
import java.util.Set;

public enum TriggerStatus {
    AWAITING_SIGNAL,
    WAITING,
    RUNNING,
    SUCCESS,
    FAILED,
    CANCELED,
    EXPIRED_SIGNAL
    ;
    public static final Set<TriggerStatus> ACTIVE_STATES = EnumSet.of(AWAITING_SIGNAL, WAITING, RUNNING);
    public static final Set<TriggerStatus> END_STATES = EnumSet.of(SUCCESS, FAILED, CANCELED, EXPIRED_SIGNAL);
}
