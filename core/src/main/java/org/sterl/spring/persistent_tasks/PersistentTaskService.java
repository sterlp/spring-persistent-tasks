package org.sterl.spring.persistent_tasks;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.sterl.spring.persistent_tasks.api.AddTriggerRequest;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.event.TriggerTaskCommand;
import org.sterl.spring.persistent_tasks.scheduler.SchedulerService;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;

/**
 * Abstraction to {@link SchedulerService} or {@link TriggerService}
 * depends on if the {@link SchedulerService} is available.
 */
@Service
public class PersistentTaskService {

    @Autowired(required = false)
    private SchedulerService schedulerService;
    @Autowired
    private TriggerService triggerService;
    
    @EventListener
    void queue(TriggerTaskCommand<? extends Serializable> event) {
        if (event.triggers().size() == 1) {
            runOrQueue(event.triggers().iterator().next());
        } else {
            triggerService.queueAll(event.triggers());
        }
    }

    /**
     * Runs the given trigger if a free threads are available
     * and the runAt time is not in the future.
     * @return the reference to the {@link TriggerKey} 
     */
    public <T extends Serializable> TriggerKey runOrQueue(
            AddTriggerRequest<T> triggerRequest) {
        if (schedulerService == null) {
            schedulerService.runOrQueue(triggerRequest);
        } else {
            triggerService.queue(triggerRequest);
        }
        return triggerRequest.key();
    }
}
