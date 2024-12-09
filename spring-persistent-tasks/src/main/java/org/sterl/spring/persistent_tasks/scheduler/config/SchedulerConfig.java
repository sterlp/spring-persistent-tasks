package org.sterl.spring.persistent_tasks.scheduler.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.EnablePersistentTasks;
import org.sterl.spring.persistent_tasks.scheduler.SchedulerService;
import org.sterl.spring.persistent_tasks.scheduler.component.EditSchedulerStatusComponent;
import org.sterl.spring.persistent_tasks.scheduler.component.TaskExecutorComponent;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class SchedulerConfig {

    @ConditionalSchedulerServiceByProperty
    @Primary
    @DependsOnDatabaseInitialization
    @Bean
    SchedulerService schedulerService(
            TriggerService triggerService,
            TaskExecutorComponent taskExecutor,
            EditSchedulerStatusComponent editSchedulerStatus,
            TransactionTemplate trx) throws UnknownHostException {

        String name = null;
        if (name == null) {
            final var ip = InetAddress.getLocalHost();
            final var hostname = ip.getHostName();

            if (hostname == null) {
                name = ip.toString();
            } else {
                name = hostname;
            }
        }
        return new SchedulerService(name, triggerService, taskExecutor, editSchedulerStatus, trx);
    }
}
