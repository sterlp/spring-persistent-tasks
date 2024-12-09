package org.sterl.spring.persistent_tasks.trigger.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${spring.persistent-tasks.web.base-path:spring-tasks}")
public class TriggerResource {


    private final TriggerService triggerService;
    
    @GetMapping("/triggers/count")
    public long count() {
        return triggerService.countTriggers();
    }
    
    @GetMapping("/triggers")
    public Page<TriggerEntity> listTasks(
            @PageableDefault(size = 50, direction = Direction.ASC, sort = "data.createdTime") Pageable pageable) {
        return triggerService.findAllTriggers(pageable);
    }
}
