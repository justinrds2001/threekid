package nl.justin.threekid.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nl.justin.threekid.domain.Person;
import nl.justin.threekid.domain.PersonId;
import nl.justin.threekid.infrastructure.InMemoryPersonRepository;

class PersonServiceTest {

	private static final PersonId ADA = new PersonId(1);
	private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-22T12:00:00Z"), ZoneOffset.UTC);

	private final PersonRepository repository = new InMemoryPersonRepository();
	private final PersonService service = new PersonService(repository, CLOCK);

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

		assertThat(repository.findById(parent1)).isPresent();
		assertThat(repository.findById(parent2)).isPresent();
		assertThat(repository.findById(partner)).isPresent();
		assertThat(repository.findById(child)).isPresent();
	}

	@Test
	void doesNotReplaceAKnownPersonWithAPlaceholder() {
		var bob = person(new PersonId(2), "Bob");
		service.save(bob);

		service.save(new Person(ADA, "Ada", null, Set.of(), bob.id(), Set.of()));

		assertThat(repository.findById(bob.id())).map(Person::name).contains("Bob");
	}

	@Test
	void returnsThePeopleWhoMatchThePatternAfterSaving() {
		var bob = new PersonId(2);
		var children = Set.of(new PersonId(10), new PersonId(11), new PersonId(12));
		var ada = new Person(ADA, "Ada", null, Set.of(), bob, children);
		service.save(ada);
		service.save(child(new PersonId(10), LocalDate.of(2015, 1, 1), bob));
		service.save(child(new PersonId(11), LocalDate.of(1990, 1, 1), bob));

		var matches = service.save(child(new PersonId(12), LocalDate.of(1990, 1, 1), bob));

		assertThat(matches).extracting(Person::id).containsExactlyInAnyOrder(ADA, bob);
	}

	@Test
	void matchesAParentWhoseChildrenAreKnownOnlyFromTheChildRecords() {
		var bob = new PersonId(2);
		service.save(new Person(ADA, "Ada", null, Set.of(), bob, Set.of()));
		service.save(child(new PersonId(10), LocalDate.of(2015, 1, 1), bob));
		service.save(child(new PersonId(11), LocalDate.of(1990, 1, 1), bob));

		var matches = service.save(child(new PersonId(12), LocalDate.of(1990, 1, 1), bob));

		assertThat(matches).extracting(Person::id).contains(ADA);
	}

	@Test
	void returnsNobodyWhenNoOneMatches() {
		assertThat(service.save(person(ADA, "Ada"))).isEmpty();
	}

	private static Person child(PersonId id, LocalDate birthDate, PersonId otherParent) {
		return new Person(id, null, birthDate, Set.of(ADA, otherParent), null, Set.of());
	}

	private static Person person(PersonId id, String name) {
		return new Person(id, name, null, Set.of(), null, Set.of());
	}
}
