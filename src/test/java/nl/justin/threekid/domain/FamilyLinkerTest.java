package nl.justin.threekid.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class FamilyLinkerTest {

	private static final PersonId ADA = new PersonId(1);
	private static final PersonId BOB = new PersonId(2);
	private static final PersonId CAROL = new PersonId(3);
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

	@Test
	void makesThePartnerMutual() {
		var result = link(partnered(ADA, BOB), partnered(BOB, null));

		assertThat(result.get(BOB).partner()).isEqualTo(ADA);
	}

	@Test
	void unlinksThePreviousPartnerOfTheNewPartner() {
		var result = link(partnered(ADA, BOB), partnered(BOB, CAROL), partnered(CAROL, BOB));

		assertThat(result.get(BOB).partner()).isEqualTo(ADA);
		assertThat(result.get(CAROL).partner()).isNull();
	}

	@Test
	void unlinksAChildTheUpdateLeavesOut() {
		var previous = person(ADA, Set.of(), Set.of(KID));
		var kid = person(KID, Set.of(ADA), Set.of());

		var result = link(person(ADA, Set.of(), Set.of()), previous, kid);

		assertThat(result.get(KID).parents()).isEmpty();
	}

	@Test
	void unlinksAParentTheUpdateLeavesOut() {
		var previous = person(KID, Set.of(ADA), Set.of());
		var ada = person(ADA, Set.of(), Set.of(KID));

		var result = link(person(KID, Set.of(), Set.of()), previous, ada);

		assertThat(result.get(ADA).children()).isEmpty();
	}

	@Test
	void unlinksAPartnerTheUpdateLeavesOut() {
		var result = link(partnered(ADA, null), partnered(ADA, BOB), partnered(BOB, ADA));

		assertThat(result.get(BOB).partner()).isNull();
	}

	@Test
	void unlinksThePreviousPartnerWhenSwitchingPartners() {
		var incoming = partnered(ADA, CAROL);

		var result = link(incoming, partnered(ADA, BOB), partnered(BOB, ADA), partnered(CAROL, null));

		assertThat(result.get(BOB).partner()).isNull();
		assertThat(result.get(CAROL).partner()).isEqualTo(ADA);
	}

	@Test
	void rejectsAThirdParentForAChild() {
		var kid = person(KID, Set.of(BOB, CAROL), Set.of());

		assertThatThrownBy(() -> link(person(ADA, Set.of(), Set.of(KID)), kid))
				.isInstanceOf(TooManyParentsException.class);
	}

	private Map<PersonId, Person> link(Person incoming, Person... stored) {
		var store = Arrays.stream(stored).collect(Collectors.toMap(Person::id, Function.identity()));
		return linker.link(incoming, id -> Optional.ofNullable(store.get(id))).stream()
				.collect(Collectors.toMap(Person::id, Function.identity()));
	}

	private static Person person(PersonId id, Set<PersonId> parents, Set<PersonId> children) {
		return new Person(id, null, null, parents, null, children);
	}

	private static Person partnered(PersonId id, PersonId partner) {
		return new Person(id, null, null, Set.of(), partner, Set.of());
	}
}
