package org.sterl.spring.persistent_tasks;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.sterl.spring.persistent_tasks.api.SpringBeanTask;
import org.sterl.spring.persistent_tasks.api.TriggerId;
import org.sterl.spring.persistent_tasks.api.event.TriggerTaskEvent;
import org.sterl.spring.persistent_tasks.scheduler.SchedulerService;
import org.sterl.spring.persistent_tasks.scheduler.component.EditSchedulerStatusComponent;
import org.sterl.spring.persistent_tasks.scheduler.component.TaskExecutorComponent;
import org.sterl.spring.persistent_tasks.task.TaskService;
import org.sterl.spring.persistent_tasks.trigger.TriggerService;
import org.sterl.spring.persistent_tasks.trigger.model.TriggerEntity;
import org.sterl.spring.sample_app.SampleApp;
import org.sterl.test.AsyncAsserts;

import lombok.RequiredArgsConstructor;

@SpringBootTest(classes = SampleApp.class)
public class AbstractSpringTest {

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
    protected TransactionTemplate trx;
    @Autowired
    protected AsyncAsserts asserts;

    @Configuration
    public static class TaskConfig {
        @Bean
        AsyncAsserts asserts() {
            return new AsyncAsserts();
        }

        @Bean("schedulerA")
        @Primary
        @SuppressWarnings("resource")
        SchedulerService schedulerA(TriggerService triggerService, EditSchedulerStatusComponent editSchedulerStatus,
                TransactionTemplate trx) throws UnknownHostException {

            final var taskExecutor = new TaskExecutorComponent(triggerService);
            taskExecutor.setMaxShutdownWaitTime(Duration.ofSeconds(0));
            return new SchedulerService("schedulerA", triggerService, taskExecutor, editSchedulerStatus, trx);
        }

        @Bean
        @SuppressWarnings("resource")
        SchedulerService schedulerB(TriggerService triggerService, EditSchedulerStatusComponent editSchedulerStatus,
                TransactionTemplate trx) throws UnknownHostException {

            final var taskExecutor = new TaskExecutorComponent(triggerService);
            taskExecutor.setMaxShutdownWaitTime(Duration.ofSeconds(0));
            return new SchedulerService("schedulerB", triggerService, taskExecutor, editSchedulerStatus, trx);
        }

        /**
         * This task will trigger task2
         */
        @Bean
        SpringBeanTask<String> task1(ApplicationEventPublisher publisher, AsyncAsserts asserts) {
            return (String state) -> {
                asserts.info("task1::" + state);
                publisher.publishEvent(TriggerTaskEvent.of("task2", "task1::" + state));
            };
        }

        @Bean
        SpringBeanTask<String> task2(AsyncAsserts asserts) {
            return new SpringBeanTask<String>() {
                @Override
                public void accept(String state) {
                    asserts.info("task2::" + state);
                }
            };
        }

        @Component(Task3.NAME)
        @RequiredArgsConstructor
        public static class Task3 implements SpringBeanTask<String> {
            public static final String NAME = "task3";
            private final AsyncAsserts asserts;

            @Override
            public void accept(String state) {
                asserts.info(NAME + "::" + state);
            }
        }

        @Bean
        SpringBeanTask<Long> slowTask(AsyncAsserts asserts) {
            return sleepTime -> {
                try {
                    if (sleepTime == null)
                        sleepTime = 1L;
                    Thread.sleep(sleepTime.longValue());
                    asserts.info("Complete " + sleepTime);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    throw new RuntimeException("OH NO!", e);
                }
            };
        }
    }
    
    protected Optional<TriggerEntity> runNextTrigger() {
        return triggerService.run(triggerService.lockNextTrigger());
    }

    protected List<TriggerId> runTriggersAndWait() {
        final List<Future<TriggerId>> triggers = schedulerService.triggerNextTasks();
        return triggers.stream().map(t -> {
            try {
                return t.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).toList();
    }

    @BeforeEach
    private void setup() throws Exception {
        triggerService.deleteAll();
        asserts.clear();
        schedulerService.start();
        schedulerB.start();
    }
}
