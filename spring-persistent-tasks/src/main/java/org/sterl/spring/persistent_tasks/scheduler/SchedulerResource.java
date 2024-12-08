package org.sterl.spring.persistent_tasks.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@ConditionalOnProperty(name = "persistent-tasks.disable-scheduler", matchIfMissing = true)
@RestController
@RequiredArgsConstructor
@RequestMapping("${persistent-tasks.web.base-path:api/persistent-tasks}")
public class SchedulerResource {

    private final SchedulerService schedulerService;
    /*
    @GetMapping("/tasks")
    public Set<TaskId<? extends Serializable>> listTasks() {
        return schedulerService.findAllTaskIds();
    }
    
    @GetMapping("/triggers")
    public Page<TriggerEntity> listTriggers(
             @PageableDefault(size = 20) Pageable pageable) {
        return schedulerService.findAllTriggers(pageable);
    }
    */
}
