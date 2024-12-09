package org.sterl.spring.persistent_tasks.trigger.api;

import java.util.Collection;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sterl.spring.persistent_tasks.scheduler.SchedulerService;
import org.sterl.spring.persistent_tasks.scheduler.config.ConditionalSchedulerServiceByProperty;

import lombok.RequiredArgsConstructor;

@ConditionalSchedulerServiceByProperty
@RestController
@RequiredArgsConstructor
@RequestMapping("${persistent-tasks.web.base-path:spring-tasks}")
public class TriggerResource {

    private final Collection<SchedulerService> schedulerServices;
    
    @GetMapping("/schedulers")
    public List<String> listTasks() {
        return schedulerServices.stream().map(SchedulerService::getName).toList();
    }
}
