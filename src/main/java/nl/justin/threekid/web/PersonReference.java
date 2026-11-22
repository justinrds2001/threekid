package nl.justin.threekid.web;

import jakarta.validation.constraints.NotNull;

import nl.justin.threekid.domain.PersonId;

public record PersonReference(@NotNull Long id) {

	static PersonReference from(PersonId id) {
		return id == null ? null : new PersonReference(id.value());
	}

	PersonId toDomain() {
		return new PersonId(id);
	}
}
