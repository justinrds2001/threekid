package nl.justin.threekid.web;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nl.justin.threekid.application.PersonService;

@RestController
@RequestMapping("/api/v1/people")
public class PersonController {

	private static final int NO_MATCH = 444;

	private final PersonService service;

	public PersonController(PersonService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<List<PersonResponse>> save(@RequestBody PersonRequest request) {
		var matches = service.save(request.toDomain());
		if (matches.isEmpty()) {
			return ResponseEntity.status(NO_MATCH).build();
		}
		return ResponseEntity.ok(matches.stream()
				.map(PersonResponse::from)
				.sorted(Comparator.comparingLong(PersonResponse::id))
				.toList());
	}
}
