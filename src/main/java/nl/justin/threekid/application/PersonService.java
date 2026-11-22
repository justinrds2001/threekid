package nl.justin.threekid.application;

import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import nl.justin.threekid.domain.Person;
import nl.justin.threekid.domain.PersonId;

@Service
public class PersonService {

	private final PersonRepository repository;

	public PersonService(PersonRepository repository) {
		this.repository = repository;
	}

	public void save(Person person) {
		repository.save(person);
		referencedIds(person)
				.filter(id -> repository.findById(id).isEmpty())
				.forEach(id -> repository.save(Person.placeholder(id)));
	}

	private static Stream<PersonId> referencedIds(Person person) {
		return Stream.of(person.parents().stream(), Stream.ofNullable(person.partner()), person.children().stream())
				.flatMap(ids -> ids);
	}
}
