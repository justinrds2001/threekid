package nl.justin.threekid.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class FamilyLinkerTest {

	private static final PersonId ADA = new PersonId(1);
	private static final PersonId KID = new PersonId(10);

	private final FamilyLinker linker = new FamilyLinker();

	@Test
	void addsTheParentToEachChildItNames() {
		var result = link(person(ADA, Set.of(), Set.of(KID)), person(KID, Set.of(), Set.of()));

		assertThat(result.get(KID).parents()).containsExactly(ADA);
	}

	@Test
	void addsTheChildToEachParentItNames() {
		var result = link(person(KID, Set.of(ADA), Set.of()), person(ADA, Set.of(), Set.of()));

		assertThat(result.get(ADA).children()).containsExactly(KID);
	}

	@Test
	void linksAPlaceholderChildBackToItsParent() {
		var result = link(person(ADA, Set.of(), Set.of(KID)));

		assertThat(result.get(KID).parents()).containsExactly(ADA);
	}

	private Map<PersonId, Person> link(Person incoming, Person... stored) {
		var store = Arrays.stream(stored).collect(Collectors.toMap(Person::id, Function.identity()));
		return linker.link(incoming, id -> Optional.ofNullable(store.get(id))).stream()
				.collect(Collectors.toMap(Person::id, Function.identity()));
	}

	private static Person person(PersonId id, Set<PersonId> parents, Set<PersonId> children) {
		return new Person(id, null, null, parents, null, children);
	}
}
