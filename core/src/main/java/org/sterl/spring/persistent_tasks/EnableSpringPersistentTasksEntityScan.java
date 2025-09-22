package org.sterl.spring.persistent_tasks;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.sterl.spring.persistent_tasks.config.EnableSpringPersistentTasksEntityScanConfig;

/**
 * Annotation to include spring persistent task entities if <code>@EntityScan</code> annotation is used.
 * 
 * <pre>
 * @SpringBootApplication
 * @EntityScan
 * @EnableSpringPersistentTasksEntityScan
 * @EnableSpringPersistentTasks
 * public class MyApp {
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EnableSpringPersistentTasksEntityScanConfig.class)
public @interface EnableSpringPersistentTasksEntityScan {

}
