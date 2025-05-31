package org.sterl.spring.persistent_tasks.scheduler.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.scheduler.SchedulerService;
import org.sterl.spring.persistent_tasks.scheduler.component.EditSchedulerStatusComponent;
import org.sterl.spring.persistent_tasks.scheduler.component.RunOrQueueComponent;
import org.sterl.spring.persistent_tasks.scheduler.component.TaskExecutorComponent;
import org.sterl.spring.persistent_tasks.scheduler.config.SchedulerThreadFactory.Type;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

/**
 * Adds and {@link SchedulerService} if not already added or disabled.
 */
@Configuration
@Slf4j
public class SchedulerConfig {

    public interface SchedulerCustomizer {
        default String name() {
            try {
                final var ip = InetAddress.getLocalHost();
                String name = ip.getHostName();
                
                if (name == null) {
                    name = ip.toString();
                }
                return name;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    @ConditionalOnMissingBean
    @Bean
    SchedulerThreadFactory schedulerThreadFactory(@Value("${spring.persistent-tasks.thread-factory:DEFAULT}") 
        SchedulerThreadFactory.Type type) {
        log.info("Using {} thread factory.", type);
        if (type == Type.VIRTUAL) return SchedulerThreadFactory.VIRTUAL_THREAD_POOL_FACTORY;
        return SchedulerThreadFactory.DEFAULT_THREAD_POOL_FACTORY;
    }
    

    @ConditionalSchedulerServiceByProperty
    @Primary
    @DependsOnDatabaseInitialization
    @Bean(name = "schedulerService", initMethod = "start", destroyMethod = "stop")
    SchedulerService schedulerService(
            TriggerService triggerService,
            MeterRegistry meterRegistry,
            SchedulerThreadFactory schedulerThreadFactory,
            @Value("${spring.persistent-tasks.max-threads:10}") int maxThreads,
            EditSchedulerStatusComponent editSchedulerStatus,
            Optional<SchedulerCustomizer> customizer,
            TransactionTemplate trx) throws UnknownHostException {
        
        customizer = customizer.isEmpty() ? Optional.of(new SchedulerCustomizer() {}) : customizer;
        final var name = customizer.get().name();
        final var maxShutdownWaitTime = Duration.ofSeconds(10);

        return newSchedulerService(name, meterRegistry, triggerService, editSchedulerStatus, 
                schedulerThreadFactory, maxThreads, 
                maxShutdownWaitTime, trx);
    }

    public static SchedulerService newSchedulerService(
            String name,
            MeterRegistry meterRegistry,
            TriggerService triggerService,
            EditSchedulerStatusComponent editSchedulerStatus, 
            int maxThreads, 
            Duration maxShutdownWaitTime,
            TransactionTemplate trx) {
        
        return newSchedulerService(name, meterRegistry, triggerService, editSchedulerStatus,
                SchedulerThreadFactory.DEFAULT_THREAD_POOL_FACTORY,
                maxThreads, 
                maxShutdownWaitTime, trx);
    }
    
    public static SchedulerService newSchedulerService(
            String name,
            MeterRegistry meterRegistry,
            TriggerService triggerService,
            EditSchedulerStatusComponent editSchedulerStatus,
            SchedulerThreadFactory schedulerThreadFactory,
            int maxThreads, 
            Duration maxShutdownWaitTime,
            TransactionTemplate trx) {
        
        final var taskExecutor = new TaskExecutorComponent(name, triggerService, schedulerThreadFactory, maxThreads);
        if (maxShutdownWaitTime != null) taskExecutor.setMaxShutdownWaitTime(maxShutdownWaitTime);

        final var runOrQueue = new RunOrQueueComponent(name, triggerService, taskExecutor);

        return new SchedulerService(name, 
                triggerService,
                taskExecutor, 
                editSchedulerStatus,
                runOrQueue,
                trx,
                meterRegistry);
    }
}
