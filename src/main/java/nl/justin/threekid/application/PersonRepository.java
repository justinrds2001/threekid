package nl.justin.threekid.application;

import nl.justin.threekid.domain.Person;
import nl.justin.threekid.domain.PersonLookup;

public interface PersonRepository extends PersonLookup {

	void save(Person person);
}
