package org.sterl.spring.persistent_tasks.scheduler.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.scheduler.SchedulerService;
import org.sterl.spring.persistent_tasks.scheduler.component.EditSchedulerStatusComponent;
import org.sterl.spring.persistent_tasks.scheduler.component.TaskExecutorComponent;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;

import lombok.extern.slf4j.Slf4j;

/**
 * Adds and {@link SchedulerService} if not already added or disabled.
 */
@Configuration
@Slf4j
public class SchedulerConfig {

    @ConditionalSchedulerServiceByProperty
    @Primary
    @DependsOnDatabaseInitialization
    @Bean(name = "schedulerService", initMethod = "start", destroyMethod = "stop")
    SchedulerService schedulerService(
            TriggerService triggerService,
            @Value("${spring.persistent-tasks.max-threads:10}") int maxThreads,
            EditSchedulerStatusComponent editSchedulerStatus,
            TransactionTemplate trx) throws UnknownHostException {

        final var ip = InetAddress.getLocalHost();
        String name = ip.getHostName();

        if (name == null) {
            name = ip.toString();
        }
        return new SchedulerService(name, triggerService, 
                new TaskExecutorComponent(triggerService, maxThreads), 
                editSchedulerStatus, trx);
    }
}
