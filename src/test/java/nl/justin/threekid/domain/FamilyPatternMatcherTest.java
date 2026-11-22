package nl.justin.threekid.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
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
		var family = family(PARTNER, child(10, YOUNG), child(11, ADULT), child(12, ADULT));
		assertThat(matcher.hasFamilyShape(family.candidate(), family.lookup())).isTrue();
		assertThat(matcher.hasUnderAgeChild(family.candidate(), family.lookup(), TODAY)).isTrue();
	}

	@Test
	void doesNotMatchPersonWithoutPartner() {
		assertThat(hasFamilyShape(null, child(10, YOUNG), child(11, ADULT), child(12, ADULT))).isFalse();
	}

	@Test
	void doesNotMatchPersonWithTwoChildren() {
		assertThat(hasFamilyShape(PARTNER, child(10, YOUNG), child(11, ADULT))).isFalse();
	}

	@Test
	void doesNotMatchPersonWithFourChildren() {
		assertThat(hasFamilyShape(PARTNER, child(10, YOUNG), child(11, ADULT), child(12, ADULT), child(13, ADULT)))
				.isFalse();
	}

	@Test
	void doesNotMatchWhenAChildHasAnotherParentThanThePartner() {
		var otherParent = new PersonId(3);
		assertThat(hasFamilyShape(PARTNER, child(10, YOUNG), child(11, ADULT), child(12, ADULT, otherParent))).isFalse();
	}

	@Test
	void doesNotMatchWhenAChildRecordIsUnknown() {
		var known = child(10, YOUNG);
		var candidate = new Person(CANDIDATE, "Candidate", ADULT, Set.of(), PARTNER,
				Set.of(known.id(), new PersonId(11), new PersonId(12)));
		var people = Map.of(CANDIDATE, candidate, known.id(), known);
		assertThat(matcher.hasFamilyShape(candidate, id -> Optional.ofNullable(people.get(id)))).isFalse();
	}

	@Test
	void doesNotMatchWhenAllChildrenAre18OrOlder() {
		assertThat(hasUnderAgeChild(child(10, ADULT), child(11, ADULT), child(12, ADULT))).isFalse();
	}

	@Test
	void doesNotCountAChildWithUnknownBirthDateAsUnder18() {
		assertThat(hasUnderAgeChild(child(10, null), child(11, ADULT), child(12, ADULT))).isFalse();
	}

	@Test
	void matchesWhenAChildTurns18Tomorrow() {
		var turns18Tomorrow = TODAY.minusYears(18).plusDays(1);
		assertThat(hasUnderAgeChild(child(10, turns18Tomorrow), child(11, ADULT), child(12, ADULT))).isTrue();
	}

	@Test
	void doesNotMatchWhenTheYoungestChildTurns18Today() {
		var turns18Today = TODAY.minusYears(18);
		assertThat(hasUnderAgeChild(child(10, turns18Today), child(11, ADULT), child(12, ADULT))).isFalse();
	}

	private boolean hasFamilyShape(PersonId partner, Person... children) {
		var family = family(partner, children);
		return matcher.hasFamilyShape(family.candidate(), family.lookup());
	}

	private boolean hasUnderAgeChild(Person... children) {
		var family = family(PARTNER, children);
		return matcher.hasUnderAgeChild(family.candidate(), family.lookup(), TODAY);
	}

	private static Family family(PersonId partner, Person... children) {
		var childIds = Arrays.stream(children).map(Person::id).collect(Collectors.toSet());
		var candidate = new Person(CANDIDATE, "Candidate", ADULT, Set.of(), partner, childIds);
		var people = Arrays.stream(children).collect(Collectors.toMap(Person::id, Function.identity()));
		people.put(CANDIDATE, candidate);
		return new Family(candidate, id -> Optional.ofNullable(people.get(id)));
	}

	private static Person child(long id, LocalDate birthDate) {
		return child(id, birthDate, PARTNER);
	}

	private static Person child(long id, LocalDate birthDate, PersonId otherParent) {
		return new Person(new PersonId(id), null, birthDate, Set.of(CANDIDATE, otherParent), null, Set.of());
	}

	private record Family(Person candidate, Function<PersonId, Optional<Person>> lookup) {
	}
}
