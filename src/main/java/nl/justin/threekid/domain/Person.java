package nl.justin.threekid.domain;

import java.time.LocalDate;
import java.util.HashSet;
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

	public static Person placeholder(PersonId id) {
		return new Person(id, null, null, Set.of(), null, Set.of());
	}

	public Person withParent(PersonId parent) {
		return new Person(id, name, birthDate, plus(parents, parent), partner, children);
	}

	public Person withChild(PersonId child) {
		return new Person(id, name, birthDate, parents, partner, plus(children, child));
	}

	public Person withoutParent(PersonId parent) {
		return new Person(id, name, birthDate, minus(parents, parent), partner, children);
	}

	public Person withoutChild(PersonId child) {
		return new Person(id, name, birthDate, parents, partner, minus(children, child));
	}

	public Person withPartner(PersonId newPartner) {
		return new Person(id, name, birthDate, parents, newPartner, children);
	}

	private static Set<PersonId> plus(Set<PersonId> ids, PersonId id) {
		var copy = new HashSet<>(ids);
		copy.add(id);
		return copy;
	}

	private static Set<PersonId> minus(Set<PersonId> ids, PersonId id) {
		var copy = new HashSet<>(ids);
		copy.remove(id);
		return copy;
	}
}
