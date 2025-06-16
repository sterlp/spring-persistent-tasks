package org.sterl.spring.persistent_tasks.task;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.TaskId.TriggerBuilder;
import org.sterl.spring.persistent_tasks.api.task.PersistentTask;
import org.sterl.spring.persistent_tasks.api.task.TransactionalTask;
import org.sterl.spring.persistent_tasks.task.util.ReflectionUtil;
import org.sterl.spring.persistent_tasks.test.AsyncAsserts;
import org.sterl.spring.sample_app.person.PersonEntity;
import org.sterl.spring.sample_app.person.PersonRepository;

class TaskTransactionTest extends AbstractSpringTest {
    /**
     * A closure cannot be annotated, so we use a anonymous class
     */
    @Configuration
    @ComponentScan("org.sterl.spring.persistent_tasks.task.test_class")
    static class Config {
        @Bean("persistentTaskAnnotated")
        PersistentTask<String> persistentTaskAnnotated(PersonRepository personRepository, AsyncAsserts asserts) {
            return new PersistentTask<String>() {
                // this will not work!
                @Transactional(timeout = 8, propagation = Propagation.REQUIRES_NEW)
                @Override
                public void accept(@Nullable String name) {
                    personRepository.save(new PersonEntity(name));
                    asserts.info(name);
                } 
            };
        }
        @Bean("transactionalTaskAnnotated")
        PersistentTask<String> transactionalTaskAnnotated(PersonRepository personRepository, AsyncAsserts asserts) {
            return new TransactionalTask<String>() {
                // this will not work!
                @Transactional(timeout = 9, propagation = Propagation.REQUIRES_NEW)
                @Override
                public void accept(@Nullable String name) {
                    personRepository.save(new PersonEntity(name));
                    asserts.info(name);
                } 
            };
        }
        @Bean("transactionalClosure")
        TransactionalTask<String> transactionalClosure(PersonRepository personRepository, AsyncAsserts asserts) {
            return name -> {
                personRepository.save(new PersonEntity(name));
                asserts.info(name);
            };
        }
    }
    
    @Autowired AsyncAsserts asserts;
    @Autowired TaskService subject;
    @Autowired PersonRepository personRepository;

    @Autowired
    PersistentTask<String> transactionalClass;
    @Autowired
    PersistentTask<String> transactionalClassAndMethod;
    @Autowired
    PersistentTask<String> requiresNewMethod;

    @Autowired
    PersistentTask<String> persistentTaskAnnotated;
    @Autowired
    PersistentTask<String> transactionalTaskAnnotated;
    @Autowired
    PersistentTask<String> transactionalClosure;
    

    @Test
    void testFindTransactionAnnotation() {
        var a = ReflectionUtil.getAnnotation(transactionalClass, Transactional.class);
        assertThat(a).isNotNull();
        assertThat(a.timeout()).isEqualTo(5);
        assertThat(a.propagation()).isEqualTo(Propagation.MANDATORY);
        
        a = ReflectionUtil.getAnnotation(transactionalClassAndMethod, Transactional.class);
        assertThat(a).isNotNull();
        assertThat(a.timeout()).isEqualTo(6);
        assertThat(a.propagation()).isEqualTo(Propagation.MANDATORY);
        assertThat(a.isolation()).isEqualTo(Isolation.REPEATABLE_READ);
        
        a = ReflectionUtil.getAnnotation(requiresNewMethod, Transactional.class);
        assertThat(a).isNotNull();
        assertThat(a.timeout()).isEqualTo(7);
        assertThat(a.propagation()).isEqualTo(Propagation.REQUIRES_NEW);
        
        
        a = ReflectionUtil.getAnnotation(persistentTaskAnnotated, Transactional.class);
        assertThat(a).isNotNull();
        assertThat(a.timeout()).isEqualTo(8);
        assertThat(a.propagation()).isEqualTo(Propagation.REQUIRES_NEW);
        
        a = ReflectionUtil.getAnnotation(transactionalTaskAnnotated, Transactional.class);
        assertThat(a).isNotNull();
        assertThat(a.timeout()).isEqualTo(9);
        assertThat(a.propagation()).isEqualTo(Propagation.REQUIRES_NEW);

        a = ReflectionUtil.getAnnotation(transactionalClosure, Transactional.class);
        assertThat(a).isNull();
    }

    @Test
    void testGetTransactionTemplate() {
        var a = subject.getTransactionTemplateIfJoinable(transactionalClass).orElse(null);
        assertThat(a).isNotNull();
        assertThat(a.getTimeout()).isEqualTo(5);
        assertThat(a.getPropagationBehavior()).isEqualTo(Propagation.REQUIRED.value());
        
        a = subject.getTransactionTemplateIfJoinable(transactionalClassAndMethod).orElse(null);
        assertThat(a).isNotNull();
        assertThat(a.getTimeout()).isEqualTo(6);
        assertThat(a.getPropagationBehavior()).isEqualTo(Propagation.REQUIRED.value());
        assertThat(a.getIsolationLevel()).isEqualTo(Isolation.REPEATABLE_READ.value());
        
        a = subject.getTransactionTemplateIfJoinable(requiresNewMethod).orElse(null);
        assertThat(a).isNull(); // cannot join requires new
        
        
        a = subject.getTransactionTemplateIfJoinable(persistentTaskAnnotated).orElse(null);
        assertThat(a).isNull(); // cannot join requires new
        
        a = subject.getTransactionTemplateIfJoinable(transactionalTaskAnnotated).orElse(null);
        assertThat(a).isNull(); // cannot join requires new

        a = subject.getTransactionTemplateIfJoinable(transactionalClosure).orElse(null);
        assertThat(a).isNotNull(); // the default one
        assertThat(a.getPropagationBehavior()).isEqualTo(Propagation.REQUIRED.value());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"transactionalTaskAnnotated", "persistentTaskAnnotated", "requiresNewMethod"})
    void testRequiresNewHasOwnTransaction(String task) {
        // GIVEN
        var t = triggerService.queue(TriggerBuilder
                .newTrigger(task, task + "test").build());
        
        // WHEN
        personRepository.deleteAllInBatch();
        hibernateAsserts.reset();
        triggerService.run(t).get();
        
        // THEN
        asserts.awaitValue(task + "test");
        hibernateAsserts.assertTrxCount(3);
        assertThat(personRepository.count()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"transactionalClass", "transactionalClassAndMethod", "transactionalClosure"})
    void testTransactionalTask(String task) throws InterruptedException {
        // GIVEN
        personRepository.deleteAllInBatch();
        var t = triggerService.queue(TriggerBuilder
                .newTrigger(task, task).build());

        // WHEN
        hibernateAsserts.reset();
        triggerService.run(t).get();

        // THEN
        asserts.awaitValue(task);
        awaidHistoryThreads();
        hibernateAsserts
            // running trigger
            //.assertDeletedCount(1)
            // 1 running trigger, 3 history, 1 trigger completed
            .assertInsertCount(4)
            .assertTrxCount(2);
        assertThat(personRepository.count()).isEqualTo(1);
    }
}
