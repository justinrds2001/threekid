package nl.justin.threekid.domain;

import java.time.LocalDate;
import java.util.Map;

public class FamilyPatternMatcher {

	private static final int ADULT_AGE = 18;

	public boolean matches(Person candidate, Map<PersonId, Person> people, LocalDate today) {
		var partner = candidate.partner();
		if (partner == null || candidate.children().size() != 3) {
			return false;
		}
		var children = candidate.children().stream().map(people::get).toList();
		return children.stream().allMatch(child -> hasParent(child, partner))
				&& children.stream().anyMatch(child -> isUnderAge(child, today));
	}

	private static boolean hasParent(Person child, PersonId parent) {
		return child != null && child.parents().contains(parent);
	}

	private static boolean isUnderAge(Person child, LocalDate today) {
		return child.birthDate() != null && today.isBefore(child.birthDate().plusYears(ADULT_AGE));
	}
}
