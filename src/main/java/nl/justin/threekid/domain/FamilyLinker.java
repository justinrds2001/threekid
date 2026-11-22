package nl.justin.threekid.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class FamilyLinker {

	public List<Person> link(Person incoming, Function<PersonId, Optional<Person>> lookup) {
		var changes = new Changes(lookup);
		changes.put(incoming);
		incoming.children().forEach(child -> changes.update(child, c -> c.withParent(incoming.id())));
		incoming.parents().forEach(parent -> changes.update(parent, p -> p.withChild(incoming.id())));
		if (incoming.partner() != null) {
			changes.update(incoming.partner(), UnaryOperator.identity());
		}
		return changes.toList();
	}

	private static final class Changes {

		private final Map<PersonId, Person> people = new LinkedHashMap<>();
		private final Function<PersonId, Optional<Person>> lookup;

		Changes(Function<PersonId, Optional<Person>> lookup) {
			this.lookup = lookup;
		}

		void put(Person person) {
			people.put(person.id(), person);
		}

		void update(PersonId id, UnaryOperator<Person> change) {
			put(change.apply(current(id)));
		}

		List<Person> toList() {
			return List.copyOf(people.values());
		}

		private Person current(PersonId id) {
			var changed = people.get(id);
			return changed != null ? changed : lookup.apply(id).orElseGet(() -> Person.placeholder(id));
		}
	}
}
