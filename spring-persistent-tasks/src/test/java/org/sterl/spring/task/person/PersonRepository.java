package org.sterl.spring.task.person;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<PersonBE, Long>{

}
