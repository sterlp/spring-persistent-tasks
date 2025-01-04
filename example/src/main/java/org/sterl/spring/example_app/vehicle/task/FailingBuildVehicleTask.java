package org.sterl.spring.example_app.vehicle.task;

import org.springframework.stereotype.Component;
import org.sterl.spring.example_app.vehicle.model.Vehicle;
import org.sterl.spring.example_app.vehicle.repository.VehicleRepository;
import org.sterl.spring.persistent_tasks.api.SpringBeanTask;
import org.sterl.spring.persistent_tasks.api.TaskId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component(FailingBuildVehicleTask.NAME)
@RequiredArgsConstructor
@Slf4j
public class FailingBuildVehicleTask implements SpringBeanTask<Vehicle> {

    static final String NAME = "failingBuildVehicleTask";
    public static final TaskId<Vehicle> ID = new TaskId<>(NAME);

    private final VehicleRepository vehicleRepository;

    @Override
    public void accept(Vehicle vehicle) {
        vehicleRepository.save(vehicle);
        log.info("Create vehicle with {} - which will fail", vehicle);
        try {
            Thread.sleep(3500);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
        throw new RuntimeException("This persistentTask will always fail!");
    }
}
