package nl.justin.threekid.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
		var people = repository.findAll().stream().collect(Collectors.toMap(Person::id, Function.identity()));
		var today = LocalDate.now(clock);
		return people.values().stream()
				.filter(candidate -> matcher.matches(candidate, people, today))
				.toList();
	}
}
