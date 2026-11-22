package nl.justin.threekid.web;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import nl.justin.threekid.domain.Person;
import nl.justin.threekid.domain.PersonId;

public record PersonRequest(
		@NotNull Long id,
		String name,
		LocalDate birthDate,
		@Valid PersonReference parent1,
		@Valid PersonReference parent2,
		@Valid PersonReference partner,
		List<@NotNull @Valid PersonReference> children) {

	@AssertTrue(message = "children must be unique")
	public boolean isChildrenUnique() {
		if (children == null) {
			return true;
		}
		var distinctIds = children.stream().map(PersonReference::id).distinct().count();
		return distinctIds == children.size();
	}

	@AssertTrue(message = "parent1 and parent2 must differ")
	public boolean isParentsDistinct() {
		return parent1 == null || parent2 == null || !Objects.equals(parent1.id(), parent2.id());
	}

	Person toDomain() {
		var parents = idsOf(Stream.of(parent1, parent2));
		Set<PersonId> childIds = children == null ? Set.of() : idsOf(children.stream());
		return new Person(new PersonId(id), name, birthDate, parents, idOf(partner), childIds);
	}

	private static Set<PersonId> idsOf(Stream<PersonReference> references) {
		return references
				.filter(Objects::nonNull)
				.map(PersonReference::toDomain)
				.collect(Collectors.toSet());
	}

	private static PersonId idOf(PersonReference reference) {
		return reference == null ? null : reference.toDomain();
	}
}
