package org.sterl.spring.example_app.vehicle;

import java.time.OffsetDateTime;
import java.util.Random;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.example_app.vehicle.task.BuildVehicleTask;
import org.sterl.spring.example_app.vehicle.task.FailingBuildVehicleTask;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.event.TriggerTaskCommand;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(timeout = 10)
@RequiredArgsConstructor
public class VehicleService {

    private final ApplicationEventPublisher eventPublisher;
    private static final Random RANDOM = new Random();

    @Scheduled(fixedDelay = 30_000, initialDelay = 1_000)
    void triggerBuildVehicle() {
        buildVehicle("Car " + OffsetDateTime.now().toString());
        
        eventPublisher.publishEvent(
                TriggerTaskCommand.of(
                        TaskId.TaskTriggerBuilder
                              .newTrigger(BuildVehicleTask.NAME)
                              .runAt(OffsetDateTime.now().plusHours(RANDOM.nextInt(10_000)))
                              .build())
                );
        
        eventPublisher.publishEvent(
                TriggerTaskCommand.of(FailingBuildVehicleTask.NAME, 
                        "Car " + OffsetDateTime.now().toString()));
    }
    
    public void buildVehicle(String type) {
        eventPublisher.publishEvent(
                TriggerTaskCommand.of(BuildVehicleTask.NAME, type));
    }
}
