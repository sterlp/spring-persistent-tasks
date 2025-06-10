package org.sterl.spring.persistent_tasks.shared;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(name = "spring.persistent-tasks.timers-enabled", havingValue = "true", matchIfMissing = true)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
public @interface TimersEnabled {

}
