package org.sterl.spring.task;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.task.repository.TaskRepository;
import org.sterl.spring.task.repository.TaskSchedulerRepository;
import org.sterl.spring.task.repository.TriggerRepository;
import org.sterl.spring.task.sample_app.SampleApp;
import org.sterl.test.AsyncAsserts;

@SpringBootTest(classes = SampleApp.class)
public class AbstractSpringTest {

    @Autowired protected TaskSchedulerRepository taskSchedulerRepository;
    @Autowired protected TaskRepository taskRepository;
    @Autowired protected TriggerRepository triggerRepository;
    @Autowired protected TransactionTemplate trx;

    @Autowired protected AsyncAsserts asserts;

    @Configuration
    static class TaskConfig {
        @Bean
        AsyncAsserts asserts() {
            return new AsyncAsserts();
        }
    }

    @BeforeEach
    void setup() throws Exception {
        triggerRepository.deleteAllInBatch();
        taskSchedulerRepository.deleteAllInBatch();
        taskRepository.clear();
        asserts.clear();
    }
}
