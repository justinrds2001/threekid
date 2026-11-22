package nl.justin.threekid.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import nl.justin.threekid.domain.Person;
import nl.justin.threekid.domain.PersonId;
import nl.justin.threekid.domain.TooManyParentsException;
import nl.justin.threekid.infrastructure.InMemoryPersonRepository;

class PersonServiceTest {

	private static final PersonId ADA = new PersonId(1);
	private static final PersonId BOB = new PersonId(2);

	private final MutableClock clock = new MutableClock(Instant.parse("2026-11-22T12:00:00Z"));
	private final PersonRepository repository = new InMemoryPersonRepository();
	private final PersonService service = new PersonService(repository, clock);

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
	void storesNothingWhenAThirdParentIsRejected() {
		var kid = new PersonId(10);
		var parents = Set.of(new PersonId(2), new PersonId(3));
		service.save(new Person(kid, "Kid", null, parents, null, Set.of()));

		assertThatThrownBy(() -> service.save(new Person(ADA, "Ada", null, Set.of(), null, Set.of(kid))))
				.isInstanceOf(TooManyParentsException.class);
		assertThat(repository.findById(ADA)).isEmpty();
		assertThat(repository.findById(kid)).map(Person::parents).contains(parents);
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
	void matchesAPlaceholderThroughLinksFromOtherRecords() {
		var bob = new PersonId(2);
		service.save(new Person(ADA, "Ada", null, Set.of(), bob, Set.of()));
		service.save(child(new PersonId(10), LocalDate.of(2015, 1, 1), bob));
		service.save(child(new PersonId(11), LocalDate.of(1990, 1, 1), bob));

		var matches = service.save(child(new PersonId(12), LocalDate.of(1990, 1, 1), bob));

		assertThat(matches).filteredOn(person -> person.id().equals(bob))
				.singleElement()
				.satisfies(placeholder -> assertThat(placeholder.name()).isNull());
	}

	@Test
	void returnsNobodyWhenNoOneMatches() {
		assertThat(service.save(person(ADA, "Ada"))).isEmpty();
	}

	@Test
	void dropsAMatchWhenAChildSwitchesToAnotherParent() {
		saveFamily(LocalDate.of(2015, 1, 1));

		var matches = service.save(child(new PersonId(12), LocalDate.of(1990, 1, 1), new PersonId(3)));

		assertThat(matches).isEmpty();
	}

	@Test
	void matchesWhenOnlyAChildRecordCompletesTheFamily() {
		service.save(new Person(ADA, "Ada", null, Set.of(), BOB, Set.of()));
		service.save(child(new PersonId(10), LocalDate.of(2015, 1, 1), BOB));
		service.save(child(new PersonId(11), LocalDate.of(1990, 1, 1), BOB));
		service.save(child(new PersonId(12), LocalDate.of(1990, 1, 1), new PersonId(3)));

		var matches = service.save(child(new PersonId(12), LocalDate.of(1990, 1, 1), BOB));

		assertThat(matches).extracting(Person::id).containsExactlyInAnyOrder(ADA, BOB);
	}

	@Test
	void stopsMatchingOnceTheYoungestChildTurns18() {
		var matches = saveFamily(LocalDate.of(2008, 11, 23));
		assertThat(matches).isNotEmpty();

		clock.advanceOneDay();

		assertThat(service.save(person(new PersonId(99), "Unrelated"))).isEmpty();
	}

	@Test
	void keepsEveryLinkWhenChildrenAreSavedAtTheSameTime() throws InterruptedException {
		var children = 100;
		var start = new CountDownLatch(1);
		try (var executor = Executors.newFixedThreadPool(8)) {
			IntStream.range(0, children).forEach(i -> executor.submit(() -> {
				start.await();
				return service.save(new Person(new PersonId(100 + i), null, null, Set.of(ADA), null, Set.of()));
			}));
			start.countDown();
		}

		assertThat(repository.findById(ADA)).map(person -> person.children().size()).contains(children);
	}

	private List<Person> saveFamily(LocalDate youngestBirthDate) {
		service.save(new Person(ADA, "Ada", null, Set.of(), BOB, Set.of()));
		service.save(child(new PersonId(10), youngestBirthDate, BOB));
		service.save(child(new PersonId(11), LocalDate.of(1990, 1, 1), BOB));
		return service.save(child(new PersonId(12), LocalDate.of(1990, 1, 1), BOB));
	}

	private static Person child(PersonId id, LocalDate birthDate, PersonId otherParent) {
		return new Person(id, null, birthDate, Set.of(ADA, otherParent), null, Set.of());
	}

	private static Person person(PersonId id, String name) {
		return new Person(id, name, null, Set.of(), null, Set.of());
	}

	private static final class MutableClock extends Clock {

		private Instant instant;

		MutableClock(Instant instant) {
			this.instant = instant;
		}

		void advanceOneDay() {
			instant = instant.plus(1, ChronoUnit.DAYS);
		}

		@Override
		public Instant instant() {
			return instant;
		}

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			throw new UnsupportedOperationException();
		}
	}
}
