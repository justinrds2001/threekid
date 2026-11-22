package nl.justin.threekid.domain;

public class TooManyParentsException extends RuntimeException {

	public TooManyParentsException(PersonId child) {
		super("Person " + child.value() + " already has two parents");
	}
}
