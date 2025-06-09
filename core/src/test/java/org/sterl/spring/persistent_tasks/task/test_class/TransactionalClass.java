package org.sterl.spring.persistent_tasks.task.test_class;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.api.task.PersistentTask;
import org.sterl.spring.persistent_tasks.test.AsyncAsserts;
import org.sterl.spring.sample_app.person.PersonEntity;
import org.sterl.spring.sample_app.person.PersonRepository;

import lombok.RequiredArgsConstructor;

@Component
@Transactional(timeout = 5, propagation = Propagation.MANDATORY)
@RequiredArgsConstructor
public class TransactionalClass implements PersistentTask<String> {
    private final PersonRepository personRepository;
    private final AsyncAsserts asserts;

    @Override
    public void accept(String name) {
        personRepository.save(new PersonEntity(name));
        asserts.info(name);
    }
}
