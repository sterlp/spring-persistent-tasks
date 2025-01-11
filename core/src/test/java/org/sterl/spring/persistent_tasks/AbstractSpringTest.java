package org.sterl.spring.persistent_tasks;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.api.PersistentTask;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.api.event.TriggerTaskCommand;
import org.sterl.spring.persistent_tasks.history.HistoryService;
import org.sterl.spring.persistent_tasks.scheduler.SchedulerService;
import org.sterl.spring.persistent_tasks.scheduler.component.EditSchedulerStatusComponent;
import org.sterl.spring.persistent_tasks.scheduler.component.TaskExecutorComponent;
import org.sterl.spring.persistent_tasks.task.TaskService;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;
import org.sterl.spring.sample_app.SampleApp;
import org.sterl.test.AsyncAsserts;
import org.sterl.test.HibernateAsserts;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

// @ActiveProfiles("mssql") // postgres mssql mariadb mysql
@SpringBootTest(classes = SampleApp.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class AbstractSpringTest {

    @Autowired
    protected PersistentTaskService persistentTaskService;

    @Autowired
    @Qualifier("schedulerA")
    protected SchedulerService schedulerService;
    @Autowired
    @Qualifier("schedulerA")
    protected SchedulerService schedulerA;
    @Autowired
    @Qualifier("schedulerB")
    protected SchedulerService schedulerB;

    @Autowired
    protected TriggerService triggerService;
    @Autowired
    protected TaskService taskService;

    @Autowired
    protected HistoryService historyService;

    @Autowired
    protected TransactionTemplate trx;
    @Autowired
    protected AsyncAsserts asserts;
    @Autowired
    protected HibernateAsserts hibernateAsserts;

    protected final PodamFactory pm = new PodamFactoryImpl();

    @Configuration
    public static class TaskConfig {
        @Bean
        AsyncAsserts asserts() {
            return new AsyncAsserts();
        }
        
        @Bean
        TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
            TransactionTemplate template = new TransactionTemplate(transactionManager);
            template.setTimeout(10);
            return template;
        }
        
        @Bean
        HibernateAsserts hibernateAsserts(EntityManager entityManager) {
            return new HibernateAsserts(entityManager);
        }

        @Primary
        @Bean("schedulerA")
        @SuppressWarnings("resource")
        SchedulerService schedulerA(TriggerService triggerService, EditSchedulerStatusComponent editSchedulerStatus,
                TransactionTemplate trx) throws UnknownHostException {

            final var taskExecutor = new TaskExecutorComponent(triggerService, 10);
            taskExecutor.setMaxShutdownWaitTime(Duration.ofSeconds(0));
            return new SchedulerService("schedulerA", triggerService, taskExecutor, editSchedulerStatus, trx);
        }

        @Bean
        @SuppressWarnings("resource")
        SchedulerService schedulerB(TriggerService triggerService, EditSchedulerStatusComponent editSchedulerStatus,
                TransactionTemplate trx) throws UnknownHostException {

            final var taskExecutor = new TaskExecutorComponent(triggerService, 20);
            taskExecutor.setMaxShutdownWaitTime(Duration.ofSeconds(0));
            return new SchedulerService("schedulerB", triggerService, taskExecutor, editSchedulerStatus, trx);
        }

        /**
         * This persistentTask will trigger task2
         */
        @Bean
        PersistentTask<String> task1(ApplicationEventPublisher publisher, AsyncAsserts asserts) {
            return (String state) -> {
                asserts.info("task1::" + state);
                publisher.publishEvent(TriggerTaskCommand.of("task2", "task1::" + state));
            };
        }

        @Bean
        PersistentTask<String> task2(AsyncAsserts asserts) {
            return state -> asserts.info("task2::" + state);
        }

        @Component(Task3.NAME)
        @RequiredArgsConstructor
        public static class Task3 implements PersistentTask<String> {
            public static final String NAME = "task3";
            public static final TaskId<String> ID = new TaskId<>(NAME);

            private final AsyncAsserts asserts;

            @Override
            public void accept(String state) {
                asserts.info(NAME + "::" + state);
            }
        }

        @Bean
        PersistentTask<Long> slowTask(AsyncAsserts asserts) {
            return sleepTime -> {
                try {
                    if (sleepTime == null) {
                        sleepTime = 1L;
                    }
                    Thread.sleep(sleepTime.longValue());
                    asserts.info("slowTask complete after=" + sleepTime + "ms");
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    throw new RuntimeException("OH NO!", e);
                }
            };
        }
    }

    protected Optional<TriggerEntity> runNextTrigger() {
        return triggerService.run(triggerService.lockNextTrigger("test"));
    }
    
    protected void awaitRunningTasks() throws TimeoutException, InterruptedException {
        final long start = System.currentTimeMillis();
        while (triggerService.countTriggers(TriggerStatus.RUNNING) > 0) {
            if (System.currentTimeMillis() - start > 2000) {
                throw new TimeoutException("Still running after 2s");
            }
            Thread.sleep(50);
        }
    }

    @BeforeEach
    public void beforeEach() throws Exception {
        triggerService.deleteAll();
        historyService.deleteAll();
        asserts.clear();
        schedulerA.setMaxThreads(10);
        schedulerB.setMaxThreads(20);
        schedulerA.start();
        schedulerB.start();
        hibernateAsserts.reset();
    }

    @AfterEach
    public void afterEach() throws Exception {
        schedulerA.shutdownNow();
        schedulerB.shutdownNow();
        triggerService.deleteAll();
        historyService.deleteAll();
    }
}
