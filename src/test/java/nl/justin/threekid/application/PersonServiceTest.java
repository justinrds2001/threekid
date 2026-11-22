package nl.justin.threekid.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

import nl.justin.threekid.domain.Person;
import nl.justin.threekid.domain.PersonId;
import nl.justin.threekid.infrastructure.InMemoryPersonRepository;

class PersonServiceTest {

	private static final PersonId ADA = new PersonId(1);

	private final PersonRepository repository = new InMemoryPersonRepository();
	private final PersonService service = new PersonService(repository);

	@Test
	void storesANewPerson() {
		var ada = person(ADA, "Ada");

		service.save(ada);

		assertThat(repository.findById(ADA)).contains(ada);
	}

	@Test
	void replacesThePersonWithAnExistingId() {
		service.save(person(ADA, "Ada"));
		var renamed = person(ADA, "Ada Lovelace");

		service.save(renamed);

		assertThat(repository.findById(ADA)).contains(renamed);
	}

	private static Person person(PersonId id, String name) {
		return new Person(id, name, null, Set.of(), null, Set.of());
	}
}
