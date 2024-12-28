package org.sterl.spring.example_app.vehicle.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.example_app.vehicle.model.Vehicle;
import org.sterl.spring.example_app.vehicle.repository.VehicleRepository;
import org.sterl.spring.persistent_tasks.api.SpringBeanTask;
import org.sterl.spring.persistent_tasks.api.TaskId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component(BuildVehicleTask.NAME)
@RequiredArgsConstructor
@Slf4j
public class BuildVehicleTask implements SpringBeanTask<Vehicle> {

    static final String NAME = "buildVehicleTask";
    public static final TaskId<Vehicle> ID = new TaskId<>(NAME);

    private final VehicleRepository vehicleRepository;

    @Transactional(timeout = 5)
    @Override
    public void accept(Vehicle vehicle) {
        vehicleRepository.save(vehicle);
        log.info("Create vehicle ={}", vehicle);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }
}
