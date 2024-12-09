package org.sterl.spring.example_app.vehicle.model;

import org.sterl.spring.example_app.shared.model.AbstractEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Vehicle extends AbstractEntity<Long> {

    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Id
    private Long id;
    
    private String type;

    public Vehicle(String type) {
        super();
        this.type = type;
    }
}
