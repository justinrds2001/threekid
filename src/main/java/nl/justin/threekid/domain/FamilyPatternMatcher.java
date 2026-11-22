package nl.justin.threekid.domain;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Function;

public class FamilyPatternMatcher {

	private static final int ADULT_AGE = 18;

	public boolean hasFamilyShape(Person candidate, Function<PersonId, Optional<Person>> lookup) {
		var partner = candidate.partner();
		if (partner == null || candidate.children().size() != 3) {
			return false;
		}
		return candidate.children().stream()
				.map(lookup)
				.allMatch(child -> child.filter(c -> c.parents().contains(partner)).isPresent());
	}

	public boolean hasUnderAgeChild(Person candidate, Function<PersonId, Optional<Person>> lookup, LocalDate today) {
		return candidate.children().stream()
				.map(lookup)
				.flatMap(Optional::stream)
				.anyMatch(child -> isUnderAge(child, today));
	}

	private static boolean isUnderAge(Person child, LocalDate today) {
		return child.birthDate() != null && today.isBefore(child.birthDate().plusYears(ADULT_AGE));
	}
}
