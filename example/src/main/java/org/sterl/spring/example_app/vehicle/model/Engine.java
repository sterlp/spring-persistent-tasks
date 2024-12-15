package org.sterl.spring.example_app.vehicle.model;

import java.io.Serializable;

import org.hibernate.validator.constraints.Length;
import org.sterl.spring.example_app.shared.model.AbstractEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Engine extends AbstractEntity<Long> implements Serializable {

    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Id
    private Long id;
    
    @Length(max = 50)
    private String type;

    private Integer power;
}
