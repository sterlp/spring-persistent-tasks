package org.sterl.spring.persistent_tasks.trigger;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import org.springframework.aot.hint.annotation.Reflective;

@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Reflective
public @interface CronTrigger {
    /**
     * A unique id for this trigger which is optional, by default the cron
     * expression or the delay is used.
     */
    String id() default "";

    /**
     * A cron-like expression, extending the usual UN*X definition to include
     * triggers
     * on the second, minute, hour, day of month, month, and day of week.
     * <p>
     * For example, {@code "0 * * * * MON-FRI"} means once per minute on weekdays
     * (at the top of the minute - the 0th second).
     * </p>
     * The fields read from left to right are interpreted as follows.
     * <ul>
     * <li>second</li>
     * <li>minute</li>
     * <li>hour</li>
     * <li>day of month</li>
     * <li>month</li>
     * <li>day of week</li>
     * </ul>
     * 
     * @return an expression that can be parsed to a cron schedule
     * @see org.springframework.scheduling.support.CronExpression#parse(String)
     */
    String cron() default "";

    /** the delay time between the cron triggers */
    long fixedDelay() default -1;

    /** the unit of the fixed delay */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
