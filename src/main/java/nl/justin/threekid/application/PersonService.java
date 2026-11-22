package nl.justin.threekid.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import nl.justin.threekid.domain.FamilyLinker;
import nl.justin.threekid.domain.FamilyPatternMatcher;
import nl.justin.threekid.domain.Person;
import nl.justin.threekid.domain.PersonId;

@Service
public class PersonService {

	private final FamilyLinker linker = new FamilyLinker();
	private final FamilyPatternMatcher matcher = new FamilyPatternMatcher();
	private final Set<PersonId> familyShaped = new HashSet<>();
	private final PersonRepository repository;
	private final Clock clock;

	public PersonService(PersonRepository repository, Clock clock) {
		this.repository = repository;
		this.clock = clock;
	}

	public List<Person> save(Person person) {
		var changed = linker.link(person, repository::findById);
		changed.forEach(repository::save);
		changed.forEach(this::reassess);
		return currentMatches();
	}

	private void reassess(Person candidate) {
		if (matcher.hasFamilyShape(candidate, repository::findById)) {
			familyShaped.add(candidate.id());
		} else {
			familyShaped.remove(candidate.id());
		}
	}

	private List<Person> currentMatches() {
		var today = LocalDate.now(clock);
		return familyShaped.stream()
				.map(repository::findById)
				.flatMap(Optional::stream)
				.filter(candidate -> matcher.hasUnderAgeChild(candidate, repository::findById, today))
				.toList();
	}
}
