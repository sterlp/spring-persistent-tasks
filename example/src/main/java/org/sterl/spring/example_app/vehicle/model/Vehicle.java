package org.sterl.spring.example_app.vehicle.model;

import java.io.Serializable;

import org.hibernate.validator.constraints.Length;
import org.sterl.spring.example_app.shared.model.AbstractEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Vehicle extends AbstractEntity<Long> implements Serializable {

    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Id
    private Long id;
    
    @Length(max = 50)
    @NotNull
    private String type;
    
    @Length(max = 50)
    @NotNull
    private String name;
    
    @ManyToOne(cascade = CascadeType.ALL)
    private Engine engine;

    public Vehicle(String type) {
        super();
        this.type = type;
    }
}
