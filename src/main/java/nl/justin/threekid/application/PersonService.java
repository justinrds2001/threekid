package nl.justin.threekid.application;

import org.springframework.stereotype.Service;

import nl.justin.threekid.domain.Person;

@Service
public class PersonService {

	private final PersonRepository repository;

	public PersonService(PersonRepository repository) {
		this.repository = repository;
	}

	public void save(Person person) {
		repository.save(person);
	}
}
