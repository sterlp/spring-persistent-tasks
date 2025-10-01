package org.sterl.spring.persistent_tasks;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.sterl.spring.persistent_tasks.config.SpringPersistentTasksConfig;

/**
 * Enables the spring persistent task services.
 * 
 * <pre>
 * {@code
 * @SpringBootApplication
 * @EnableSpringPersistentTasks
 * public class MyApp {
 * }
 * }
 * </pre>
 * <p>Include corresponding annotation if you use:</p>
 * <ul>
 * <li>@EntityScan -> {@link EnableSpringPersistentTasksEntityScan}</li>
 * <li>@EnableJpaRepositories -> {@link EnableSpringPersistentTasksJpaRepositories}</li>
 * <li>@EnableEnversRepositories -> {@link EnableSpringPersistentTasksJpaRepositories}</li>
 * </ul>
 * 
 * They break the spring auto configuration.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SpringPersistentTasksConfig.class)
public @interface EnableSpringPersistentTasks {

}
