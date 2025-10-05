package org.sterl.spring.persistent_tasks.config;

import java.io.Serializable;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.sterl.spring.persistent_tasks.EnableSpringPersistentTasks;
import org.sterl.spring.persistent_tasks.api.task.StateSerializer;
import org.sterl.spring.persistent_tasks.trigger.component.DefaultStateSerializer;

import lombok.extern.slf4j.Slf4j;

@EnableScheduling
@EnableAsync
@AutoConfigurationPackage(basePackageClasses = EnableSpringPersistentTasks.class)
@ComponentScan(basePackageClasses = EnableSpringPersistentTasks.class)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Configuration
@Slf4j
public class SpringPersistentTasksConfig {

    @Bean
    @ConditionalOnMissingBean(DefaultStateSerializer.class)
    StateSerializer<Serializable> defaultStateSerializer() {
        log.info("Using java serialization for task states.", 
                DefaultStateSerializer.class.getSimpleName());
        return new DefaultStateSerializer();
    }
}
