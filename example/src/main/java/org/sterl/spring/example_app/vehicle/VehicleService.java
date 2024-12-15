package org.sterl.spring.example_app.vehicle;

import java.time.OffsetDateTime;
import java.util.Random;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sterl.spring.example_app.vehicle.model.Vehicle;
import org.sterl.spring.example_app.vehicle.task.BuildVehicleTask;
import org.sterl.spring.example_app.vehicle.task.FailingBuildVehicleTask;
import org.sterl.spring.persistent_tasks.api.event.TriggerTaskCommand;

import lombok.RequiredArgsConstructor;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@Service
@Transactional(timeout = 10)
@RequiredArgsConstructor
public class VehicleService {

    private final ApplicationEventPublisher eventPublisher;
    private static final Random RANDOM = new Random();
    private static final PodamFactory PODAM = new PodamFactoryImpl();


    @Scheduled(fixedDelay = 30_000, initialDelay = 1_000)
    void triggerBuildVehicle() {
        final Vehicle v = PODAM.manufacturePojoWithFullData(Vehicle.class);
        v.setId(null);
        v.getEngine().setId(null);
        
        eventPublisher.publishEvent(
                TriggerTaskCommand.of(BuildVehicleTask.ID.newUniqueTrigger(v)));
        
        eventPublisher.publishEvent(
                TriggerTaskCommand.of(BuildVehicleTask.ID
                        .newTrigger()
                        .state(v)
                        .runAt(OffsetDateTime.now().plusMinutes(RANDOM.nextInt(10_000)))
                        .build()));
        
        eventPublisher.publishEvent(
                TriggerTaskCommand.of(FailingBuildVehicleTask.ID.newUniqueTrigger(v)));
    }
    
    public void buildVehicle(String type) {
        final Vehicle v = PODAM.manufacturePojo(Vehicle.class);
        v.setId(null);
        v.getEngine().setId(null);
        v.setType(type);

        eventPublisher.publishEvent(
                TriggerTaskCommand.of(BuildVehicleTask.ID.newUniqueTrigger(v)));
    }
}
