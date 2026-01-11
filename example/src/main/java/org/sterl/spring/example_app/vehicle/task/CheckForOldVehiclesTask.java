package org.sterl.spring.example_app.vehicle.task;

import java.util.concurrent.TimeUnit;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.sterl.spring.persistent_tasks.api.task.PersistentTask;
import org.sterl.spring.persistent_tasks.trigger.CronTrigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@CronTrigger(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
@Component
@RequiredArgsConstructor
@Slf4j
public class CheckForOldVehiclesTask implements PersistentTask<String> {

    @Override
    public void accept(@Nullable String nothing) {
        log.info("doing vehicle cron job search ...");
    }
}
