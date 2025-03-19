package org.sterl.spring.persistent_tasks.trigger.api;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sterl.spring.persistent_tasks.api.Trigger;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
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
            @RequestParam(name = "id", required = false) String id,
            @RequestParam(name = "taskName", required = false) String taskName,
            @RequestParam(name = "status", required = false) TriggerStatus status,
            @PageableDefault(size = 100, direction = Direction.ASC, sort = "data.runAt")
            Pageable pageable) {
        var key = new TriggerKey(StringUtils.trimToNull(id), StringUtils.trimToNull(taskName));
        return FromTriggerEntity.INSTANCE.toPage(
                triggerService.findAllTriggers(key, status, pageable));
    }
    
    @PostMapping("triggers/{taskName}/{id}/run-at")
    public Optional<Trigger> setRunAt(
            @PathVariable("taskName") String taskName,
            @PathVariable("id") String id,
            @RequestBody OffsetDateTime runAt) {
        
        var result = triggerService.updateRunAt(new TriggerKey(id, taskName), runAt);
        return FromTriggerEntity.INSTANCE.convert(result);
    }
    
    @DeleteMapping("triggers/{taskName}/{id}")
    public Optional<Trigger> cancelTrigger(
            @PathVariable("taskName") String taskName,
            @PathVariable("id") String id) {

        return FromTriggerEntity.INSTANCE
                .convert(triggerService.cancel(new TriggerKey(id, taskName)));
    }
}
