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
     * If no new correlation id is provided take either the one from any
     * currently running trigger or its id.
     */
    public static String buildOrGetCorrelationId(String newCorrelationId) {
        if (newCorrelationId != null) return newCorrelationId;
        
        var c = getContext();
        if (c != null) {
            if (c.getCorrelationId() != null) return c.getCorrelationId();
            if (c.getKey() != null) return c.getKey().getId();
        }
        return null;
    }
}
