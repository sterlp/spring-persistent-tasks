package org.sterl.spring.example_app.vehicle.model;

import org.sterl.spring.example_app.shared.model.AbstractEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Vehicle extends AbstractEntity<Long> {
    private static final long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Id
    private Long id;
    
    @Size(min = 2, max = 50)
    @NotNull
    private String type;
    
    @Size(min = 3, max = 50)
    @NotNull
    private String name;
    
    @ManyToOne(cascade = CascadeType.ALL)
    private Engine engine;

    public Vehicle(String type) {
        super();
        this.type = type;
    }
}
