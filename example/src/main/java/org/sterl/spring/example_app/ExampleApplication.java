package org.sterl.spring.example_app;

import java.net.UnknownHostException;
import java.time.Duration;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.EnableSpringPersistentTasks;
import org.sterl.spring.persistent_tasks.scheduler.SchedulerService;
import org.sterl.spring.persistent_tasks.scheduler.component.EditSchedulerStatusComponent;
import org.sterl.spring.persistent_tasks.scheduler.component.TaskExecutorComponent;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;
import org.sterl.spring.persistent_tasks_ui.EnableSpringPersistentTasksUI;

@SpringBootApplication
@EnableSpringPersistentTasks
@EnableSpringPersistentTasksUI
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }
    
    @Bean
    GroupedOpenApi exampleAppApi() {
      return GroupedOpenApi.builder()
              .group("example-app-api")
              .pathsToMatch("/api/**")
              .build();
    }
    
    @Bean
    GroupedOpenApi springPersistentTasksApi() {
      return GroupedOpenApi.builder()
              .group("spring-persistent-tasks-api")
              .pathsToMatch("/spring-tasks-api/**")
              .build();
    }

    @Bean(name = "schedulerB", initMethod = "start", destroyMethod = "stop")
    @SuppressWarnings("resource")
    SchedulerService schedulerB(
            TriggerService triggerService, 
            EditSchedulerStatusComponent editSchedulerStatus,
            TransactionTemplate trx) throws UnknownHostException {

        return new SchedulerService("schedulerB", triggerService, 
                new TaskExecutorComponent(triggerService, 7), editSchedulerStatus, trx);
    }
}
