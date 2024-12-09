package org.sterl.spring.example_app.vehicle.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sterl.spring.example_app.vehicle.VehicleService;
import org.sterl.spring.example_app.vehicle.model.Vehicle;
import org.sterl.spring.example_app.vehicle.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/vehicles")
@RequiredArgsConstructor
public class VehicleResource {

    private final VehicleService vehicleService;
    private final VehicleRepository vehicleRepository;

    @PostMapping
    public void build(@RequestBody String type) {
        vehicleService.buildVehicle(type);
    }
    
    @GetMapping
    public Page<Vehicle> list(
        @PageableDefault(size = 50)
        Pageable pageable) {
        return vehicleRepository.findAll(pageable);
    }
}
