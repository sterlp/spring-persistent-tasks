package org.sterl.spring.persistent_tasks.example.bl.vehicle.task;

import org.springframework.stereotype.Component;
import org.sterl.spring.persistent_tasks.example.bl.vehicle.model.Vehicle;
import org.sterl.spring.persistent_tasks.example.bl.vehicle.repository.VehicleRepository;
import org.sterl.spring.task.api.SpringBeanTask;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component(BuildVehicleTask.NAME)
@RequiredArgsConstructor
@Slf4j
public class BuildVehicleTask implements SpringBeanTask<String> {

    public static final String NAME = "buildVehicleTask";

    private final VehicleRepository vehicleRepository;

    @Override
    public void accept(String type) {
        vehicleRepository.save(new Vehicle(type));
        log.info("Create vehicle with type={}", type);
    }
}
