package org.sterl.spring.example_app.vehicle;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.example_app.vehicle.task.BuildVehicleTask;
import org.sterl.spring.persistent_tasks.api.event.TriggerTaskCommand;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(timeout = 10)
@RequiredArgsConstructor
public class VehicleService {

    private final ApplicationEventPublisher eventPublisher;

    public void buildVehicle(String type) {
        eventPublisher.publishEvent(
                TriggerTaskCommand.of(BuildVehicleTask.NAME, type));
    }
}
