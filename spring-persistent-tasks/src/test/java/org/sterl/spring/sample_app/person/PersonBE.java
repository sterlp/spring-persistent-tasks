package org.sterl.spring.sample_app.person;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table
@Entity
@Data @NoArgsConstructor
public class PersonBE {

    @Id
    @GeneratedValue
    private Long id;
    
    private String name;

    public PersonBE(String name) {
        super();
        this.name = name;
    }
}
