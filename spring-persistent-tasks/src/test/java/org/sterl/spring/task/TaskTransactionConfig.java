package org.sterl.spring.task;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.annotation.Bean;
import org.sterl.spring.sample_app.person.PersonBE;
import org.sterl.spring.sample_app.person.PersonRepository;
import org.sterl.spring.task.api.AbstractTask;
import org.sterl.spring.task.api.RetryStrategy;
import org.sterl.spring.task.api.Task;
import org.sterl.spring.task.api.TaskResult;

public class TaskTransactionConfig {
    @Bean
    AtomicBoolean sendError() {
        return new AtomicBoolean(false);
    }
    @Bean
    Task<String> savePerson(PersonRepository personRepository, AtomicBoolean sendError) {
        return new AbstractTask<String>("savePerson") {
            @Override
            public TaskResult execute(String name) {
                personRepository.save(new PersonBE(name));
                if (sendError.get()) throw new RuntimeException("Error requested for " + name);
                return TaskResult.DONE;
            }
            public RetryStrategy retryStrategy() {
                return RetryStrategy.TRY_THREE_TIMES_IMMEDIATELY;
            }
        };
    }
}
