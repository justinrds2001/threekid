package nl.justin.threekid.domain;

import java.time.LocalDate;
import java.util.Map;

public class FamilyPatternMatcher {

	public boolean matches(Person candidate, Map<PersonId, Person> people, LocalDate today) {
		return true;
	}
}
