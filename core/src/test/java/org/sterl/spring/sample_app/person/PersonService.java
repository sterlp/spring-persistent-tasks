package org.sterl.spring.sample_app.person;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;
    
    public PersonEntity create(String name) {
        return personRepository.save(new PersonEntity(name));
    }
}
