package nl.justin.threekid.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import nl.justin.threekid.application.PersonRepository;
import nl.justin.threekid.domain.Person;
import nl.justin.threekid.domain.PersonId;

@Repository
public class InMemoryPersonRepository implements PersonRepository {

	private final Map<PersonId, Person> people = new ConcurrentHashMap<>();

	@Override
	public Optional<Person> findById(PersonId id) {
		return Optional.ofNullable(people.get(id));
	}

	@Override
	public Collection<Person> findAll() {
		return List.copyOf(people.values());
	}

	@Override
	public void save(Person person) {
		people.put(person.id(), person);
	}
}
