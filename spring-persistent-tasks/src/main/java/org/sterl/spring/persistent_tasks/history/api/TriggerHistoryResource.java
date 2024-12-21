package org.sterl.spring.persistent_tasks.history.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sterl.spring.persistent_tasks.api.Trigger;
import org.sterl.spring.persistent_tasks.history.HistoryService;
import org.sterl.spring.persistent_tasks.history.api.HistoryConverter.FromTriggerStateDetailEntity;

import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${spring.persistent-tasks.web.base-path:spring-tasks-api}")
public class TriggerHistoryResource {

    private final HistoryService historyService;

    @GetMapping("history/instance/{instanceId}")
    public List<Trigger> list(
            @PathParam("instanceId") Long instanceId) {
        
        return FromTriggerStateDetailEntity.INSTANCE.convert(
                historyService.findAllForInstance(instanceId));
    }
}
