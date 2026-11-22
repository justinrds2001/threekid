package nl.justin.threekid.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.api.Test;

class PersonTest {

	private static final PersonId ADA = new PersonId(1);

	@Test
	void rejectsBeingTheirOwnParent() {
		assertThatThrownBy(() -> new Person(ADA, null, null, Set.of(ADA), null, Set.of()))
				.isInstanceOf(SelfReferenceException.class);
	}

	@Test
	void rejectsBeingTheirOwnChild() {
		assertThatThrownBy(() -> new Person(ADA, null, null, Set.of(), null, Set.of(ADA)))
				.isInstanceOf(SelfReferenceException.class);
	}

	@Test
	void rejectsBeingTheirOwnPartner() {
		assertThatThrownBy(() -> new Person(ADA, null, null, Set.of(), ADA, Set.of()))
				.isInstanceOf(SelfReferenceException.class);
	}
}
