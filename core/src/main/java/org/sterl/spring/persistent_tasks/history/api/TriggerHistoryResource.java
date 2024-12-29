package org.sterl.spring.persistent_tasks.history.api;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sterl.spring.persistent_tasks.api.Trigger;
import org.sterl.spring.persistent_tasks.history.HistoryService;
import org.sterl.spring.persistent_tasks.history.api.HistoryConverter.FromLastTriggerStateEntity;
import org.sterl.spring.persistent_tasks.history.api.HistoryConverter.FromTriggerStateDetailEntity;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${spring.persistent-tasks.web.base-path:spring-tasks-api}")
public class TriggerHistoryResource {

    private final HistoryService historyService;

    @GetMapping("history/instance/{instanceId}")
    public List<Trigger> listInstances(@PathVariable("instanceId") long instanceId) {
        return FromTriggerStateDetailEntity.INSTANCE.convert( //
                historyService.findAllForInstance(instanceId));
    }

    @GetMapping("history")
    public PagedModel<Trigger> list(
            @PageableDefault(size = 100, direction = Direction.ASC, sort = "data.runAt") Pageable pageable) {

        return FromLastTriggerStateEntity.INSTANCE.toPage( //
                historyService.findTriggerState(null, pageable));
    }
}
