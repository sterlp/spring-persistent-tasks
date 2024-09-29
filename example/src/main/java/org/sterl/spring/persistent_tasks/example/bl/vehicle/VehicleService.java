package org.sterl.spring.persistent_tasks.example.bl.vehicle;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.persistent_tasks.example.bl.vehicle.task.BuildVehicleTask;
import org.sterl.spring.task.api.event.TriggerTaskEvent;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(timeout = 10)
@RequiredArgsConstructor
public class VehicleService {

    private final ApplicationEventPublisher eventPublisher;

    public void buildVehicle(String type) {
        eventPublisher.publishEvent(
                TriggerTaskEvent.of(BuildVehicleTask.NAME, type));
    }
}
