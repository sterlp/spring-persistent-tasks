package org.sterl.spring.persistent_tasks.scheduler.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@FunctionalInterface
public interface SchedulerThreadFactory {
    ExecutorService newExecutorService(int maxThreads);
    
    enum Type {
        DEFAULT,
        VIRTUAL
    }
    
    SchedulerThreadFactory DEFAULT_THREAD_POOL_FACTORY = (maxThreads) -> {
        return new ThreadPoolExecutor(
                1, maxThreads,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
    };
    
    SchedulerThreadFactory VIRTUAL_THREAD_POOL_FACTORY = (maxThreads) -> {
        return Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("v-spt-", 0) .factory());
    };
}
