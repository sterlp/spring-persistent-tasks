package org.sterl.spring.persistent_tasks.task.util;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.sterl.spring.persistent_tasks.api.task.PersistentTaskBase;

public abstract class ReflectionUtil {

    public static <A extends Annotation> A getAnnotation(PersistentTaskBase<? extends Serializable> inTask, Class<A> searchFor) {
        var task = AopProxyUtils.ultimateTargetClass(inTask);
        var targetMethod = ReflectionUtils.findMethod(task, "accept", Serializable.class);
        
        A result = null;
        // check the method first
        if (targetMethod != null) result = AnnotationUtils.findAnnotation(targetMethod, searchFor);
        // check the class if no method annotation was found
        if (result == null) result = AnnotationUtils.findAnnotation(task, searchFor);

        return result;
    }
}
