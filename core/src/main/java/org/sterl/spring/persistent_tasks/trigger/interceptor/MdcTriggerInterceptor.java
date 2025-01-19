package org.sterl.spring.persistent_tasks.trigger.interceptor;

import org.slf4j.MDC;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerFailedEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerRunningEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerSuccessEvent;

/**
 * Adds task name and id to the {@link MDC} context.
 */
@Component
public class MdcTriggerInterceptor {

    public static final String TASK_NAME = "taskName";
    public static final String TASK_ID = "taskId";

    @EventListener
    public void beforeRun(TriggerRunningEvent data) {
        MDC.put(TASK_NAME, data.key().getTaskName());
        MDC.put(TASK_ID, data.key().getId());
    }
    @EventListener
    public void onFailed(TriggerFailedEvent data) {
        MDC.remove(TASK_NAME);
        MDC.remove(TASK_ID);
    }
    @EventListener
    public void onSuccess(TriggerSuccessEvent data) {
        MDC.remove(TASK_NAME);
        MDC.remove(TASK_ID);
    }
}
