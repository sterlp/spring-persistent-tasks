package org.sterl.spring.persistent_tasks.trigger.interceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.sterl.spring.persistent_tasks.api.TriggerStatus;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerCanceledEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerFailedEvent;
import org.sterl.spring.persistent_tasks.trigger.event.TriggerSuccessEvent;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Component
@Slf4j
public class TriggerMetricInterceptor {

    private final MeterRegistry meterRegistry;
    private final Map<String, Timer> cache = new ConcurrentHashMap<>(); 

    @EventListener
    public void onFailed(TriggerFailedEvent data) {
        recordTime(data.key().getTaskName(), 
                data.status(), 
                data.getData().getRunningDurationInMs());
    }
    @EventListener
    public void onSuccess(TriggerSuccessEvent data) {
        recordTime(data.key().getTaskName(), 
                data.status(), 
                data.getData().getRunningDurationInMs());

    }
    @EventListener
    public void onSuccess(TriggerCanceledEvent data) {
        recordTime(data.key().getTaskName(), 
                data.status(), 
                data.getData().getRunningDurationInMs());
    }
    
    private void recordTime(String name, TriggerStatus status, Long timeMs) {
        if (timeMs == null) return;
        final var key = name  + status;

        try {
            var timer = cache.get(key);
            if (timer == null) {
                timer = Timer.builder("persistent_tasks.task." + name)
                             .tags(Tags.of("status", status.name()))
                             .register(meterRegistry);
                cache.put(key, timer);
            }
            timer.record(timeMs, TimeUnit.MICROSECONDS);
        } catch (Exception e) {
            log.error("Failed to update timer for {}", name, e);
        }
    }
}

