package nl.justin.threekid.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class FamilyPatternMatcherTest {

	private static final LocalDate TODAY = LocalDate.of(2026, 11, 22);
	private static final LocalDate YOUNG = TODAY.minusYears(10);
	private static final LocalDate ADULT = TODAY.minusYears(30);

	private static final PersonId CANDIDATE = new PersonId(1);
	private static final PersonId PARTNER = new PersonId(2);

	private final FamilyPatternMatcher matcher = new FamilyPatternMatcher();

	@Test
	void matchesPersonWithPartnerAndThreeSharedChildrenOneUnder18() {
		assertThat(matches(PARTNER, child(10, YOUNG), child(11, ADULT), child(12, ADULT))).isTrue();
	}

	private boolean matches(PersonId partner, Person... children) {
		var childIds = Arrays.stream(children).map(Person::id).collect(Collectors.toSet());
		var candidate = new Person(CANDIDATE, "Candidate", ADULT, Set.of(), partner, childIds);
		var people = Arrays.stream(children).collect(Collectors.toMap(Person::id, Function.identity()));
		people.put(CANDIDATE, candidate);
		return matcher.matches(candidate, Map.copyOf(people), TODAY);
	}

	private static Person child(long id, LocalDate birthDate) {
		return child(id, birthDate, PARTNER);
	}

	private static Person child(long id, LocalDate birthDate, PersonId otherParent) {
		return new Person(new PersonId(id), null, birthDate, Set.of(CANDIDATE, otherParent), null, Set.of());
	}
}
