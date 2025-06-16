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
import org.sterl.spring.persistent_tasks.api.TriggerKey;
import org.sterl.spring.persistent_tasks.api.TriggerSearch;
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
                historyService.findAllDetailsForInstance(instanceId));
    }
    @GetMapping("task-status-history")
    public List<TaskStatusHistoryOverview> taskStatusHistory() {
        return historyService.taskStatusHistory();
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
