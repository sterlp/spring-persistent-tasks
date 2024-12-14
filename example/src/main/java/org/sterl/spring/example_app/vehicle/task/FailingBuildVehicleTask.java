package org.sterl.spring.example_app.vehicle.task;

import org.springframework.stereotype.Component;
import org.sterl.spring.example_app.vehicle.model.Vehicle;
import org.sterl.spring.example_app.vehicle.repository.VehicleRepository;
import org.sterl.spring.persistent_tasks.api.SpringBeanTask;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component(FailingBuildVehicleTask.NAME)
@RequiredArgsConstructor
@Slf4j
public class FailingBuildVehicleTask implements SpringBeanTask<String> {

    public static final String NAME = "failingBuildVehicleTask";

    private final VehicleRepository vehicleRepository;

    @Override
    public void accept(String type) {
        vehicleRepository.save(new Vehicle(type));
        log.info("Create vehicle with type={} - which will fail", type);
        try {
            Thread.sleep(3500);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
        throw new RuntimeException("This task will always fail!");
    }
}
