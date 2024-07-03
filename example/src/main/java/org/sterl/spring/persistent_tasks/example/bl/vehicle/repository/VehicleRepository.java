package org.sterl.spring.persistent_tasks.example.bl.vehicle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sterl.spring.persistent_tasks.example.bl.vehicle.model.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

}
