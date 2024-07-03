package org.sterl.spring.task;

import java.io.Serializable;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sterl.spring.task.api.TaskId;
import org.sterl.spring.task.model.TriggerEntity;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${persistent-tasks.web.base-path:api/persistent-tasks}")
public class TaskSchedulerResource {

    private final TaskSchedulerService schedulerService;
    
    @GetMapping("/tasks")
    public Set<TaskId<? extends Serializable>> listTasks() {
        return schedulerService.findAllTaskIds();
    }
    
    @GetMapping("/trigger")
    @Transactional(readOnly = true)
    public Page<TriggerEntity> listTriggers(
             @PageableDefault(size = 20) Pageable pageable) {
        return schedulerService.findAllTriggers(pageable);
    }
}
