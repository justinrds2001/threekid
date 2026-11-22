package nl.justin.threekid.web;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import nl.justin.threekid.domain.Person;
import nl.justin.threekid.domain.PersonId;

public record PersonResponse(
		long id,
		String name,
		LocalDate birthDate,
		PersonReference parent1,
		PersonReference parent2,
		PersonReference partner,
		List<PersonReference> children) {

	static PersonResponse from(Person person) {
		var parents = sorted(person.parents());
		return new PersonResponse(
				person.id().value(),
				person.name(),
				person.birthDate(),
				parents.isEmpty() ? null : parents.getFirst(),
				parents.size() > 1 ? parents.get(1) : null,
				PersonReference.from(person.partner()),
				sorted(person.children()));
	}

	private static List<PersonReference> sorted(Set<PersonId> ids) {
		return ids.stream()
				.sorted(Comparator.comparingLong(PersonId::value))
				.map(PersonReference::from)
				.toList();
	}
}
