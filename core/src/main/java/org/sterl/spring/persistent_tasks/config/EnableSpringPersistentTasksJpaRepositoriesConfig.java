package org.sterl.spring.persistent_tasks.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.sterl.spring.persistent_tasks.EnableSpringPersistentTasksJpaRepositories;

@EnableJpaRepositories(basePackageClasses = EnableSpringPersistentTasksJpaRepositories.class)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class EnableSpringPersistentTasksJpaRepositoriesConfig {

}
