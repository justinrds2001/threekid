package nl.justin.threekid.application;

import java.util.Collection;
import java.util.Optional;

import nl.justin.threekid.domain.Person;
import nl.justin.threekid.domain.PersonId;

public interface PersonRepository {

	Optional<Person> findById(PersonId id);

	Collection<Person> findAll();

	void save(Person person);
}
