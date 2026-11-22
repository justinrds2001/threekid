package nl.justin.threekid.domain;

import java.util.Optional;

@FunctionalInterface
public interface PersonLookup {

	Optional<Person> find(PersonId id);
}
