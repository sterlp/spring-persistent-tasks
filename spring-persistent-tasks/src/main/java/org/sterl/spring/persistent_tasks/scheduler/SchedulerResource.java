package org.sterl.spring.persistent_tasks.scheduler;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sterl.spring.persistent_tasks.scheduler.config.ConditionalSchedulerServiceByProperty;

import lombok.RequiredArgsConstructor;

@ConditionalSchedulerServiceByProperty
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
