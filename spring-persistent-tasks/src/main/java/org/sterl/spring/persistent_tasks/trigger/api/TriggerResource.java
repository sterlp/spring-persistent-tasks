package org.sterl.spring.persistent_tasks.trigger.api;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.Trigger;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;
import org.sterl.spring.persistent_tasks.trigger.api.TriggerConverter.FromTriggerEntity;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${spring.persistent-tasks.web.base-path:spring-tasks-api}")
public class TriggerResource {

    private final TriggerService triggerService;
    
    @GetMapping("triggers/count")
    public long count() {
        return triggerService.countTriggers();
    }
    
    @GetMapping("triggers")
    public PagedModel<Trigger> list(
            @RequestParam(name = "taskId", required = false) String taskId,
            @PageableDefault(size = 100, direction = Direction.ASC, sort = "data.runAt") 
            Pageable pageable) {
        return FromTriggerEntity.INSTANCE.toPage(
                triggerService.findAllTriggers(TaskId.of(taskId), pageable));
    }
}
