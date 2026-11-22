package nl.justin.threekid.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

public record Person(
		PersonId id,
		String name,
		LocalDate birthDate,
		Set<PersonId> parents,
		PersonId partner,
		Set<PersonId> children) {

	public Person {
		Objects.requireNonNull(id, "id");
		parents = Set.copyOf(parents);
		children = Set.copyOf(children);
	}
}
