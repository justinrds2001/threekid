package nl.justin.threekid.domain;

public class SelfReferenceException extends RuntimeException {

	public SelfReferenceException(PersonId id) {
		super("Person " + id.value() + " cannot be their own parent, child or partner");
	}
}
