package nl.justin.threekid.domain;

import java.time.LocalDate;
import java.util.Map;

public class FamilyPatternMatcher {

	public boolean matches(Person candidate, Map<PersonId, Person> people, LocalDate today) {
		var partner = candidate.partner();
		return partner != null
				&& candidate.children().size() == 3
				&& candidate.children().stream().allMatch(child -> hasParent(people.get(child), partner));
	}

	private static boolean hasParent(Person child, PersonId parent) {
		return child != null && child.parents().contains(parent);
	}
}
