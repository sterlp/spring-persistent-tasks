package org.sterl.spring.example_app.vehicle.task;

import java.util.Random;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.sterl.spring.example_app.vehicle.model.Vehicle;
import org.sterl.spring.example_app.vehicle.repository.VehicleRepository;
import org.sterl.spring.persistent_tasks.api.TaskId;
import org.sterl.spring.persistent_tasks.api.task.TransactionalTask;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component(FailingBuildVehicleTask.NAME)
@RequiredArgsConstructor
@Slf4j
public class FailingBuildVehicleTask implements TransactionalTask<Vehicle> {

    static final String NAME = "failingBuildVehicleTask";
    public static final TaskId<Vehicle> ID = new TaskId<>(NAME);
    private final Random random = new Random();
    private final VehicleRepository vehicleRepository;

    @Override
    public void accept(@Nullable Vehicle vehicle) {
        vehicleRepository.save(vehicle);
        log.info("Create vehicle with {} - which will fail", vehicle);
        try {
            Thread.sleep(random.nextInt(3501));
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
        if (random.nextInt(11) % 2 == 0)
            throw new RuntimeException("This persistentTask will always fail!");
    }
}
