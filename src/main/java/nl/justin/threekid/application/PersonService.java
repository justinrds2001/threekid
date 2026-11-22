package nl.justin.threekid.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import nl.justin.threekid.domain.FamilyLinker;
import nl.justin.threekid.domain.FamilyPatternMatcher;
import nl.justin.threekid.domain.Person;

@Service
public class PersonService {

	private final FamilyLinker linker = new FamilyLinker();
	private final FamilyPatternMatcher matcher = new FamilyPatternMatcher();
	private final PersonRepository repository;
	private final Clock clock;

	public PersonService(PersonRepository repository, Clock clock) {
		this.repository = repository;
		this.clock = clock;
	}

	public List<Person> save(Person person) {
		linker.link(person, repository::findById).forEach(repository::save);
		return findMatches();
	}

	private List<Person> findMatches() {
		var today = LocalDate.now(clock);
		return repository.findAll().stream()
				.filter(candidate -> matcher.hasFamilyShape(candidate, repository::findById))
				.filter(candidate -> matcher.hasUnderAgeChild(candidate, repository::findById, today))
				.toList();
	}
}
