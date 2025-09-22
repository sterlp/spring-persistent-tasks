package org.sterl.spring.example_app;

import java.net.UnknownHostException;
import java.time.Duration;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.EnableSpringPersistentTasks;
import org.sterl.spring.persistent_tasks.scheduler.SchedulerService;
import org.sterl.spring.persistent_tasks.scheduler.component.EditSchedulerStatusComponent;
import org.sterl.spring.persistent_tasks.scheduler.config.SchedulerConfig;
import org.sterl.spring.persistent_tasks.scheduler.config.SchedulerConfig.SchedulerCustomizer;
import org.sterl.spring.persistent_tasks.scheduler.config.SchedulerThreadFactory;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;
import org.sterl.spring.persistent_tasks_ui.EnableSpringPersistentTasksUI;

import io.micrometer.core.instrument.MeterRegistry;

@EnableWebSecurity
@SpringBootApplication
@EnableSpringPersistentTasks
@EnableSpringPersistentTasksUI
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }
    
    @Bean
    SchedulerCustomizer SchedulerCustomizer() {
        return new SchedulerCustomizer() {
            @Override
            public String name() {
                return "Test-Scheduler";
            }
        };
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

    // just one more for demonstration
    @Bean(name = "schedulerB", initMethod = "start", destroyMethod = "stop")
    SchedulerService schedulerB(
            MeterRegistry meterRegistry,
            TriggerService triggerService, 
            EditSchedulerStatusComponent editSchedulerStatus,
            TransactionTemplate trx) throws UnknownHostException {

        return SchedulerConfig.newSchedulerService("schedulerB",
                meterRegistry,
                triggerService,
                editSchedulerStatus, 
                SchedulerThreadFactory.VIRTUAL_THREAD_POOL_FACTORY,
                7, 
                Duration.ofSeconds(1), 
                trx);
    }
    
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .httpBasic(org.springframework.security.config.Customizer.withDefaults())
            .csrf(c -> 
                c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                 .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            );
        return http.build();
    }
    
    @Bean
    UserDetailsService users() {
        UserDetails admin = User.builder()
            .username("admin")
            .password("admin")
            .roles("ADMIN")
            .build();
        return new InMemoryUserDetailsManager(admin);
    }
}
