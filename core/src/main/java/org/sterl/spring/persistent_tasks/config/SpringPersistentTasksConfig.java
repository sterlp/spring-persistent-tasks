package org.sterl.spring.persistent_tasks.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Role;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.sterl.spring.persistent_tasks.EnableSpringPersistentTasks;

@EnableScheduling
@EnableAsync
@AutoConfigurationPackage(basePackageClasses = EnableSpringPersistentTasks.class)
@ComponentScan(basePackageClasses = EnableSpringPersistentTasks.class)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class SpringPersistentTasksConfig {

}
