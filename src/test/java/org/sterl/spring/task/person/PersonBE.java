package org.sterl.spring.task.person;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
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
