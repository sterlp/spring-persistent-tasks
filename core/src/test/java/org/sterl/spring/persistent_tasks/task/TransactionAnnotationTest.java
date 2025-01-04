package org.sterl.spring.persistent_tasks.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.ReflectionUtils;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.PersistentTask;
import org.sterl.spring.sample_app.person.PersonBE;
import org.sterl.spring.sample_app.person.PersonRepository;

import lombok.RequiredArgsConstructor;

class TransactionAnnotationTest extends AbstractSpringTest {
    @Component("transactionalClass")
    @Transactional(timeout = 5, propagation = Propagation.MANDATORY)
    @RequiredArgsConstructor
    static class TransactionalClass implements PersistentTask<PersonBE> {
        private final PersonRepository personRepository;
        @Override
        public void accept(PersonBE state) {
            personRepository.save(state);
        }
    }
    @Component("transactionalMethod")
    @RequiredArgsConstructor
    static class TransactionalMethod implements PersistentTask<PersonBE> {
        private final PersonRepository personRepository;
        @Transactional(timeout = 6, propagation = Propagation.MANDATORY)
        @Override
        public void accept(PersonBE state) {
            personRepository.save(state);
        }
    }
    
    /**
     * A closure cannot be annotated, so we use a anonymous class
     */
    @Configuration
    static class Config {
        @Bean("transactionalClosure")
        PersistentTask<PersonBE> transactionalClosure(PersonRepository personRepository) {
            return new PersistentTask<PersonBE>() {
                @Transactional(timeout = 7, propagation = Propagation.MANDATORY)
                @Override
                public void accept(PersonBE state) {
                    personRepository.save(state);
                } 
            };
        }
    }
    
    @Autowired @Qualifier("transactionalClass")
    PersistentTask<PersonBE> transactionalClass;
    @Autowired @Qualifier("transactionalMethod")
    PersistentTask<PersonBE> transactionalMethod;
    @Autowired @Qualifier("transactionalMethod")
    PersistentTask<PersonBE> transactionalClosure;

    @Test
    void testFindTransactionAnnotation() throws Exception {
        var a = getTrxAnnotation(transactionalClass.getClass(), Transactional.class);
        assertThat(a).isNotNull();
        assertThat(a.timeout()).isEqualTo(5);
        
        a = getTrxAnnotation(transactionalMethod.getClass(), Transactional.class);
        assertThat(a).isNotNull();
        assertThat(a.timeout()).isEqualTo(6);
        
        a = getTrxAnnotation(transactionalClosure.getClass(), Transactional.class);
        assertThat(a).isNotNull();
        assertThat(a.timeout()).isEqualTo(6);
    }
    
    @Test
    void testA() {
        // Resolve the actual target class
        var targetClass = AopProxyUtils.ultimateTargetClass(transactionalMethod); // ;

        // Retrieve the method and its @Transactional annotation
        //var targetMethod = targetClass.getMethod("accept", Serializable.class);
        var targetMethod = ReflectionUtils.findMethod(targetClass, "accept", Serializable.class);
        var transactionalMethodAnnotation = AnnotationUtils.findAnnotation(targetMethod, Transactional.class);
        System.out.println("Method-level Transactional Annotation: " + transactionalMethodAnnotation);

        // Retrieve the class-level @Transactional annotation
        var transactionalClassAnnotation = AnnotationUtils.findAnnotation(targetClass, Transactional.class);
        System.out.println("Class-level Transactional Annotation: " + transactionalClassAnnotation);
        
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
    }
    
    public <A extends Annotation> A getTrxAnnotation(Class<?> inTask, Class<A> searchFor) {
        var task = AopProxyUtils.ultimateTargetClass(inTask);
        A result = AnnotationUtils.findAnnotation(task, searchFor);
        if (result != null) return result;

        var targetMethod = ReflectionUtils.findMethod(task, "accept", Serializable.class);
        if (targetMethod == null) return null;

        result = AnnotationUtils.findAnnotation(targetMethod, searchFor);
        return result;
    }

    public static DefaultTransactionDefinition convertTransactionalToDefinition(Transactional transactional) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();

        // Map Transactional attributes to DefaultTransactionDefinition
        def.setIsolationLevel(transactional.isolation().value());
        def.setPropagationBehavior(transactional.propagation().value());
        def.setTimeout(transactional.timeout());
        def.setReadOnly(transactional.readOnly());
        // No direct mapping for 'rollbackFor' or 'noRollbackFor'
        // Set a name if desired (e.g., based on transactional class/method)
        def.setName("TransactionalDefinition");

        return def;
    }

}
