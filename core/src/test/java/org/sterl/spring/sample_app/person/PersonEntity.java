package org.sterl.spring.sample_app.person;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "person")
@Entity
@Data @NoArgsConstructor
public class PersonEntity{

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    public PersonEntity(String name) {
        super();
        this.name = name;
    }
}
