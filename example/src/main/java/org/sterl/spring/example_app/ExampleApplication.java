package org.sterl.spring.example_app;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;
import org.sterl.spring.persistent_tasks.EnablePersistentTasks;
import org.sterl.spring.persistent_tasks_ui.EnablePersistentTasksUi;

@SpringBootApplication
@EnablePersistentTasks
@EnablePersistentTasksUi
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
              .pathsToMatch("/spring-tasks/**")
              .build();
    }

}
