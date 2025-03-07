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

    public static final String TASK_RUNNING_ON = "taskRunningOn";
    public static final String TASK_START = "taskStart";
    public static final String TASK_NAME = "taskName";
    public static final String TASK_KEY = "taskKey";
    public static final String TASK_ID = "taskId";

    @EventListener
    public void beforeRun(TriggerRunningEvent data) {
        MDC.put(TASK_RUNNING_ON, data.runningOn());
        MDC.put(TASK_START, data.getData().getStart() + "");
        MDC.put(TASK_NAME, data.key().getTaskName());
        MDC.put(TASK_KEY, data.key().getId());
        MDC.put(TASK_ID, data.id() + "");
    }
    @EventListener
    public void onFailed(TriggerFailedEvent data) {
        clearMdc();
    }
    @EventListener
    public void onSuccess(TriggerSuccessEvent data) {
        clearMdc();
    }
    private void clearMdc() {
        MDC.remove(TASK_RUNNING_ON);
        MDC.remove(TASK_START);
        MDC.remove(TASK_NAME);
        MDC.remove(TASK_KEY);
        MDC.remove(TASK_ID);
    }
}
