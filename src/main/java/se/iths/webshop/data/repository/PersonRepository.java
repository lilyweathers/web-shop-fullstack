package se.iths.webshop.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.iths.webshop.business.entity.Person;

import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {
    Optional<Person>findByName(String name);
    Optional<Person>findByEmail(String email);
    Optional<Person> findByEmailAndPassword(String email, String password);
}
