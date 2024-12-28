package org.sterl.spring.persistent_tasks.task.api;

import java.util.Arrays;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.task.TaskService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${spring.persistent-tasks.web.base-path:spring-tasks-api}")
public class TaskResource {

    private final TaskService taskService;
    
    @GetMapping("tasks")
    public String[] get() {
        final var taskNames = taskService.findAllTaskIds()
                .stream()
                .map(TaskId::name)
                .toArray(size -> new String[size]);

        Arrays.sort(taskNames);

        return taskNames;
    }
}
