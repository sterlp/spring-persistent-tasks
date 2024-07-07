package org.sterl.spring.task;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.annotation.Bean;
import org.sterl.spring.sample_app.person.PersonBE;
import org.sterl.spring.sample_app.person.PersonRepository;
import org.sterl.spring.task.api.RetryStrategy;
import org.sterl.spring.task.api.SpringBeanTask;

public class TaskTransactionConfig {
    @Bean
    AtomicBoolean sendError() {
        return new AtomicBoolean(false);
    }
    @Bean
    SpringBeanTask<String> savePerson(PersonRepository personRepository, AtomicBoolean sendError) {
        return new SpringBeanTask<>() {
            @Override
            public void accept(String name) {
                personRepository.save(new PersonBE(name));
                if (sendError.get()) throw new RuntimeException("Error requested for " + name);
            }
            public RetryStrategy retryStrategy() {
                return RetryStrategy.TRY_THREE_TIMES_IMMEDIATELY;
            }
        };
    }
}
