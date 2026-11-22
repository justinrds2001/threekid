package nl.justin.threekid.web;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import nl.justin.threekid.domain.Person;
import nl.justin.threekid.domain.PersonId;

public record PersonRequest(
		Long id,
		String name,
		LocalDate birthDate,
		PersonReference parent1,
		PersonReference parent2,
		PersonReference partner,
		List<PersonReference> children) {

	Person toDomain() {
		var parents = Stream.of(parent1, parent2).filter(Objects::nonNull).map(PersonReference::toDomain)
				.collect(Collectors.toSet());
		var childIds = children == null ? Set.<PersonId>of()
				: children.stream().map(PersonReference::toDomain).collect(Collectors.toSet());
		var partnerId = partner == null ? null : partner.toDomain();
		return new Person(new PersonId(id), name, birthDate, parents, partnerId, childIds);
	}
}
