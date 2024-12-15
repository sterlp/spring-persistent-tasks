package org.sterl.spring.persistent_tasks.scheduler.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
    @ConditionalOnMissingBean
    @DependsOnDatabaseInitialization
    @Bean
    SchedulerService schedulerService(
            TriggerService triggerService,
            TaskExecutorComponent taskExecutor,
            EditSchedulerStatusComponent editSchedulerStatus,
            TransactionTemplate trx) throws UnknownHostException {

        final var ip = InetAddress.getLocalHost();
        String name = ip.getHostName();

        if (name == null) {
            name = ip.toString();
        }
        return new SchedulerService(name, triggerService, taskExecutor, editSchedulerStatus, trx);
    }
}
