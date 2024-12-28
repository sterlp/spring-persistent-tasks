package org.sterl.spring.sample_app.person;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<PersonBE, Long>{

}
