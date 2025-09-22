package org.sterl.spring.persistent_tasks.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Role;
import org.sterl.spring.persistent_tasks.EnableSpringPersistentTasksEntityScan;

@EntityScan(basePackageClasses = EnableSpringPersistentTasksEntityScan.class)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class EnableSpringPersistentTasksEntityScanConfig {

}
