package org.sterl.spring.persistent_tasks.task;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.AbstractSpringTest;
import org.sterl.spring.persistent_tasks.api.TaskId.TriggerBuilder;
import org.sterl.spring.persistent_tasks.api.task.PersistentTask;
import org.sterl.spring.persistent_tasks.api.task.TransactionalTask;
import org.sterl.spring.persistent_tasks.task.util.ReflectionUtil;
import org.sterl.spring.sample_app.person.PersonEntity;
import org.sterl.spring.sample_app.person.PersonRepository;

import lombok.RequiredArgsConstructor;

class TaskTransactionTest extends AbstractSpringTest {

    @Component("transactionalClass")
    @Transactional(timeout = 5, propagation = Propagation.MANDATORY)
    @RequiredArgsConstructor
    static class TransactionalClass implements PersistentTask<String> {
        private final PersonRepository personRepository;
        @Override
        public void accept(String name) {
            personRepository.save(new PersonEntity(name));
            personRepository.save(new PersonEntity(name));
        }
    }

    @Component("transactionalMethod")
    @Transactional(timeout = 76, propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
    @RequiredArgsConstructor
    static class TransactionalMethod implements PersistentTask<String> {
        private final PersonRepository personRepository;
        @Transactional(timeout = 6, propagation = Propagation.MANDATORY, isolation = Isolation.REPEATABLE_READ)
        @Override
        public void accept(String name) {
            personRepository.save(new PersonEntity(name));
            personRepository.save(new PersonEntity(name));
        }
    }

    /**
     * A closure cannot be annotated, so we use a anonymous class
     */
    @Configuration
    static class Config {
        @Bean("transactionalAnonymous")
        PersistentTask<String> transactionalAnonymous(PersonRepository personRepository) {
            return new PersistentTask<String>() {
                @Transactional(timeout = 7, propagation = Propagation.REQUIRES_NEW)
                @Override
                public void accept(String name) {
                    personRepository.save(new PersonEntity(name));
                } 
            };
        }
        @Bean("transactionalClosure")
        TransactionalTask<String> transactionalClosure(PersonRepository personRepository) {
            return name -> {
                personRepository.save(new PersonEntity(name));
                personRepository.save(new PersonEntity(name));
            };
        }
    }
    
    @Autowired TaskService subject;
    @Autowired PersonRepository personRepository;

    @Autowired @Qualifier("transactionalClass")
    PersistentTask<String> transactionalClass;
    @Autowired @Qualifier("transactionalMethod")
    PersistentTask<String> transactionalMethod;
    @Autowired @Qualifier("transactionalAnonymous")
    PersistentTask<String> transactionalAnonymous;

    @Test
    void testFindTransactionAnnotation() {
        var a = ReflectionUtil.getAnnotation(transactionalClass, Transactional.class);
        assertThat(a).isNotNull();
        assertThat(a.timeout()).isEqualTo(5);
        
        a = ReflectionUtil.getAnnotation(transactionalMethod, Transactional.class);
        assertThat(a).isNotNull();
        assertThat(a.timeout()).isEqualTo(6);
        
        a = ReflectionUtil.getAnnotation(transactionalAnonymous, Transactional.class);
        assertThat(a).isNotNull();
        assertThat(a.timeout()).isEqualTo(7);
    }

    @Test
    void testGetTransactionTemplate() {
        var a = subject.getTransactionTemplate(transactionalClass);
        assertThat(a).isPresent();
        assertThat(a.get().getTimeout()).isEqualTo(5);
        assertThat(a.get().getPropagationBehavior()).isEqualTo(Propagation.REQUIRED.value());
        
        a = subject.getTransactionTemplate(transactionalMethod);
        assertThat(a).isPresent();
        assertThat(a.get().getTimeout()).isEqualTo(6);
        assertThat(a.get().getPropagationBehavior()).isEqualTo(Propagation.REQUIRED.value());
        assertThat(a.get().getIsolationLevel()).isEqualTo(Isolation.REPEATABLE_READ.value());
        
        a = subject.getTransactionTemplate(transactionalAnonymous);
        assertThat(a).isEmpty();
    }
    
    @Test
    void testRequiresNewHasOwnTransaction() {
        // GIVEN
        var t = triggerService.queue(TriggerBuilder
                .newTrigger("transactionalAnonymous", "test").build());
        
        // WHEN
        personRepository.deleteAllInBatch();
        hibernateAsserts.reset();
        triggerService.run(t).get();
        
        // THEN
        hibernateAsserts.assertTrxCount(4);
        assertThat(personRepository.count()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"transactionalClass", "transactionalMethod", "transactionalClosure"})
    void testTransactionalTask(String task) throws InterruptedException {
        // GIVEN
        var t = triggerService.queue(TriggerBuilder
                .newTrigger(task, "test").build());
        
        // WHEN
        personRepository.deleteAllInBatch();
        hibernateAsserts.reset();
        triggerService.run(t).get();
        Thread.sleep(50); // TODO wait for the history running event
        // THEN
        hibernateAsserts.assertTrxCount(2);
        assertThat(personRepository.count()).isEqualTo(2);
    }
}
