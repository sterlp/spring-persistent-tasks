package org.sterl.spring.persistent_tasks.api.task;

import java.io.Serializable;
import java.util.Objects;

/**
 * The {@link RunningTrigger} state will be provided by this context holder to any thread.
 * Furthermore the correlationId of this context is preferred if a context is found.
 */
public class RunningTriggerContextHolder {

    private static final ThreadLocal<RunningTrigger<? extends Serializable>> contextHolder = new InheritableThreadLocal<>();

    public static void clearContext() {
        contextHolder.remove();
    }

    public static RunningTrigger<? extends Serializable> getContext() {
        return contextHolder.get();
    }

    public static void setContext(RunningTrigger<? extends Serializable> context) {
        Objects.requireNonNull(context, "Only non-null correlationId instances are permitted");
        contextHolder.set(context);
    }
    
    public static String getCorrelationId() {
        return contextHolder.get() == null ? null : contextHolder.get().getCorrelationId();
    }
    
    /**
     * Default method to obtain a new correlation ID taking in account if an ID is set or not.
     * 
     * @param newCorrelationId optional desired correlationId
     * @return either the set correlationId or the desired one or a random build one.
     */
    public static String buildOrGetCorrelationId(String newCorrelationId) {
        var correlationId = getCorrelationId();
        if (correlationId == null) correlationId = newCorrelationId;
        // take over any key from the trigger before ...
        if (correlationId == null) {
            var c = getContext();
            if (c != null) correlationId = c.getKey().getId();
        }
        return correlationId;
    }
}
