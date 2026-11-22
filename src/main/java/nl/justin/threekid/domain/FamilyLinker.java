package nl.justin.threekid.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class FamilyLinker {

	public List<Person> link(Person incoming, Function<PersonId, Optional<Person>> lookup) {
		var changed = new LinkedHashMap<PersonId, Person>();
		changed.put(incoming.id(), incoming);
		referencedIds(incoming)
				.filter(id -> !changed.containsKey(id) && lookup.apply(id).isEmpty())
				.forEach(id -> changed.put(id, Person.placeholder(id)));
		return List.copyOf(changed.values());
	}

	private static Stream<PersonId> referencedIds(Person person) {
		return Stream.of(person.parents().stream(), Stream.ofNullable(person.partner()), person.children().stream())
				.flatMap(ids -> ids);
	}
}
