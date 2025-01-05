package org.sterl.spring.persistent_tasks.task.util;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.sterl.spring.persistent_tasks.api.PersistentTask;

public abstract class ReflectionUtil {

    public static <A extends Annotation> A getAnnotation(PersistentTask<? extends Serializable> inTask, Class<A> searchFor) {
        var task = AopProxyUtils.ultimateTargetClass(inTask);
        A result = AnnotationUtils.findAnnotation(task, searchFor);
        if (result != null) return result;

        var targetMethod = ReflectionUtils.findMethod(task, "accept", Serializable.class);
        if (targetMethod == null) return null;

        result = AnnotationUtils.findAnnotation(targetMethod, searchFor);
        return result;
    }
}
