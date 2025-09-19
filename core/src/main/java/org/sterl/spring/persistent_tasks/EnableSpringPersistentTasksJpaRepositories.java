package org.sterl.spring.persistent_tasks;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.sterl.spring.persistent_tasks.config.EnableSpringPersistentTasksJpaRepositoriesConfig;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EnableSpringPersistentTasksJpaRepositoriesConfig.class)
@EntityScan // not sure if needed
@EnableJpaRepositories // not sure if needed
public @interface EnableSpringPersistentTasksJpaRepositories {

}
