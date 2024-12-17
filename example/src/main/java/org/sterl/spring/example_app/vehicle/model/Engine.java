package org.sterl.spring.example_app.vehicle.model;

import org.sterl.spring.example_app.shared.model.AbstractEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Engine extends AbstractEntity<Long> {

    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Id
    private Long id;
    
    @Size(min = 2, max = 50)
    private String type;

    private Integer power;
}
