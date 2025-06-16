package org.sterl.spring.persistent_tasks;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Optional;

import org.awaitility.Awaitility;
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
import org.springframework.lang.Nullable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.event.TriggerTaskCommand;
import org.sterl.spring.persistent_tasks.api.task.PersistentTask;
import org.sterl.spring.persistent_tasks.history.HistoryService;
import org.sterl.spring.persistent_tasks.scheduler.SchedulerService;
import org.sterl.spring.persistent_tasks.scheduler.component.EditSchedulerStatusComponent;
import org.sterl.spring.persistent_tasks.scheduler.config.SchedulerConfig;
import org.sterl.spring.persistent_tasks.scheduler.config.SchedulerThreadFactory;
import org.sterl.spring.persistent_tasks.task.TaskService;
import org.sterl.spring.persistent_tasks.test.AsyncAsserts;
import org.sterl.spring.persistent_tasks.test.PersistentTaskTestService;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;
import org.sterl.spring.persistent_tasks.trigger.model.RunningTriggerEntity;
import org.sterl.spring.sample_app.SampleApp;
import org.sterl.test.hibernate_asserts.HibernateAsserts;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@ActiveProfiles({"virtual-thread"}) // postgres mssql mariadb mysql
@SpringBootTest(classes = SampleApp.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@RecordApplicationEvents
public class AbstractSpringTest {

    @Autowired
    protected PersistentTaskTestService persistentTaskTestService;

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
    private ThreadPoolTaskExecutor triggerHistoryExecutor;

    @Autowired
    protected TransactionTemplate trx;
    @Autowired
    protected AsyncAsserts asserts;
    @Autowired
    protected HibernateAsserts hibernateAsserts;

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
        SchedulerService schedulerA(
                TriggerService triggerService,
                MeterRegistry meterRegistry,
                EditSchedulerStatusComponent editSchedulerStatus,
                SchedulerThreadFactory threadFactory,
                TransactionTemplate trx) throws UnknownHostException {

            final var name = "schedulerA";
            return SchedulerConfig.newSchedulerService(name, meterRegistry, triggerService, editSchedulerStatus, threadFactory, 10, Duration.ZERO, trx);
        }

        @Bean
        SchedulerService schedulerB(TriggerService triggerService,
                MeterRegistry meterRegistry,
                EditSchedulerStatusComponent editSchedulerStatus,
                SchedulerThreadFactory threadFactory,
                TransactionTemplate trx) throws UnknownHostException {

            final var name = "schedulerB";
            return SchedulerConfig.newSchedulerService(name, meterRegistry, triggerService, editSchedulerStatus, threadFactory, 20, Duration.ZERO, trx);
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
            public void accept(@Nullable String state) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
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
    
    protected void awaitHistoryThreads() {
        Awaitility.await().until(() -> triggerHistoryExecutor.getActiveCount() == 0);
    }

    @Deprecated
    protected Optional<RunningTriggerEntity> runNextTrigger() {
        return persistentTaskTestService.runNextTrigger();
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
