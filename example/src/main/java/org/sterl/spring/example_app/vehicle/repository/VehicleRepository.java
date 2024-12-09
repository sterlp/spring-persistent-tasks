package org.sterl.spring.example_app.vehicle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sterl.spring.example_app.vehicle.model.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

}
