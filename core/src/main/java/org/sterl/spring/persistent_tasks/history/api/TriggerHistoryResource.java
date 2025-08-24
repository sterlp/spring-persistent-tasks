package org.sterl.spring.persistent_tasks.history.api;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sterl.spring.persistent_tasks.api.TaskStatusHistoryOverview;
import org.sterl.spring.persistent_tasks.api.Trigger;
import org.sterl.spring.persistent_tasks.api.TriggerGroup;
import org.sterl.spring.persistent_tasks.api.HistoryTrigger;
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerSearch;
import org.sterl.spring.persistent_tasks.history.HistoryService;
import org.sterl.spring.persistent_tasks.history.api.HistoryConverter.FromLastTriggerStateEntity;
import org.sterl.spring.persistent_tasks.history.api.HistoryConverter.ToHistoryTrigger;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${spring.persistent-tasks.web.base-path:spring-tasks-api}")
public class TriggerHistoryResource {

    public static final String PATH_GROUP = "history-grouped";
    private final HistoryService historyService;

    @GetMapping("history/instance/{instanceId}")
    public PagedModel<HistoryTrigger> listInstances(
            @PathVariable("instanceId") long instanceId, 
            @PageableDefault(size = 250) Pageable page) {
        
        return ToHistoryTrigger.INSTANCE.toPage(historyService.findAllDetailsForInstance(instanceId, page));
    }
    @GetMapping("task-status-history")
    public List<TaskStatusHistoryOverview> taskStatusHistory() {
        return historyService.taskStatusHistory();
    }

    @GetMapping(PATH_GROUP)
    public PagedModel<TriggerGroup> listGrouped(
            TriggerSearch search,
            @PageableDefault(size = 100) Pageable page) {
        return new PagedModel<TriggerGroup>(historyService.searchGroupedTriggers(search, page));
    }
    
    @GetMapping("history")
    public PagedModel<Trigger> list(
            TriggerSearch search,
            @PageableDefault(size = 100) Pageable page) {

        return FromLastTriggerStateEntity.INSTANCE.toPage( //
                historyService.searchTriggers(search, page));
    }
    
    @PostMapping("history/{id}/re-run")
    public ResponseEntity<TriggerKey> reRunTrigger(@PathVariable(name = "id", required = true) Long id) {
        var newTrigger = historyService.reQueue(id, OffsetDateTime.now());
        return ResponseEntity.of(newTrigger);
    }
}
