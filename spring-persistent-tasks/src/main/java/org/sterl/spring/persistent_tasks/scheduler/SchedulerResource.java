package org.sterl.spring.persistent_tasks.scheduler;

import java.io.Serializable;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import lombok.RequiredArgsConstructor;

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
