package nl.justin.threekid.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public class FamilyLinker {

	public List<Person> link(Person incoming, PersonLookup lookup) {
		var changes = new Changes(lookup);
		unlinkLeftOut(incoming, changes.current(incoming.id()), changes);
		changes.put(incoming);
		incoming.children().forEach(child -> changes.update(child, c -> c.withParent(incoming.id())));
		incoming.parents().forEach(parent -> changes.update(parent, p -> p.withChild(incoming.id())));
		if (incoming.partner() != null) {
			linkPartner(incoming, changes);
		}
		return changes.toList();
	}

	private static void unlinkLeftOut(Person incoming, Person previous, Changes changes) {
		var id = incoming.id();
		previous.children().stream()
				.filter(child -> !incoming.children().contains(child))
				.forEach(child -> changes.update(child, c -> c.withoutParent(id)));
		previous.parents().stream()
				.filter(parent -> !incoming.parents().contains(parent))
				.forEach(parent -> changes.update(parent, p -> p.withoutChild(id)));
		if (previous.partner() != null && !previous.partner().equals(incoming.partner())) {
			changes.update(previous.partner(), p -> p.withPartner(null));
		}
	}

	private static void linkPartner(Person incoming, Changes changes) {
		var previousPartner = changes.current(incoming.partner()).partner();
		if (previousPartner != null && !previousPartner.equals(incoming.id())) {
			changes.update(previousPartner, p -> p.withPartner(null));
		}
		changes.update(incoming.partner(), p -> p.withPartner(incoming.id()));
	}

	private static final class Changes {

		private final Map<PersonId, Person> people = new LinkedHashMap<>();
		private final PersonLookup lookup;

		Changes(PersonLookup lookup) {
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

		Person current(PersonId id) {
			var changed = people.get(id);
			return changed != null ? changed : lookup.findById(id).orElseGet(() -> Person.placeholder(id));
		}
	}
}
