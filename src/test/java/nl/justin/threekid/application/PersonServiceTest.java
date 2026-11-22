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

	@Test
	void createsPlaceholdersForUnknownReferencedPeople() {
		var parent1 = new PersonId(2);
		var parent2 = new PersonId(3);
		var partner = new PersonId(4);
		var child = new PersonId(5);

		service.save(new Person(ADA, "Ada", null, Set.of(parent1, parent2), partner, Set.of(child)));

		assertThat(repository.findById(parent1)).contains(Person.placeholder(parent1));
		assertThat(repository.findById(parent2)).contains(Person.placeholder(parent2));
		assertThat(repository.findById(partner)).contains(Person.placeholder(partner));
		assertThat(repository.findById(child)).contains(Person.placeholder(child));
	}

	@Test
	void doesNotReplaceAKnownPersonWithAPlaceholder() {
		var bob = person(new PersonId(2), "Bob");
		service.save(bob);

		service.save(new Person(ADA, "Ada", null, Set.of(), bob.id(), Set.of()));

		assertThat(repository.findById(bob.id())).contains(bob);
	}

	private static Person person(PersonId id, String name) {
		return new Person(id, name, null, Set.of(), null, Set.of());
	}
}
