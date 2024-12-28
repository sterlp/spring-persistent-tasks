package org.sterl.spring.persistent_tasks.scheduler.api;

import java.util.Collection;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sterl.spring.persistent_tasks.scheduler.SchedulerService;
import org.sterl.spring.persistent_tasks.scheduler.config.ConditionalSchedulerServiceByProperty;
import org.sterl.spring.persistent_tasks.scheduler.entity.SchedulerEntity;

import lombok.RequiredArgsConstructor;

@ConditionalSchedulerServiceByProperty
@RestController
@RequiredArgsConstructor
@RequestMapping("${spring.persistent-tasks.web.base-path:spring-tasks-api}")
public class SchedulerResource {

    private final SchedulerService anyService;
    private final Collection<SchedulerService> schedulerServices;
    
    @GetMapping("/schedulers")
    public List<String> list() {
        return schedulerServices.stream().map(SchedulerService::getName).toList();
    }
    
    @GetMapping("/schedulers/{name}")
    public ResponseEntity<SchedulerEntity> get(@PathVariable("name") String name) {
        return ResponseEntity.of(anyService.findStatus(name));
    }
}
