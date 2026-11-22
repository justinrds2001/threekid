package nl.justin.threekid.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import nl.justin.threekid.application.PersonService;
import nl.justin.threekid.domain.Person;
import nl.justin.threekid.domain.PersonId;

@Tag("benchmark")
@SpringBootTest
@AutoConfigureMockMvc
class PostBenchmark {

	private static final int FAMILIES = 2_500;
	private static final int WARM_UP = 1_000;
	private static final int MEASURED = 1_000;

	@Autowired
	private MockMvcTester mvc;

	@Autowired
	private PersonService service;

	@Test
	void postWithTenThousandPeopleInStore() {
		for (int family = 0; family < FAMILIES; family++) {
			service.save(parent(family));
		}

		for (int i = 0; i < WARM_UP; i++) {
			post(i % FAMILIES);
		}
		var start = System.nanoTime();
		for (int i = 0; i < MEASURED; i++) {
			post(i % FAMILIES);
		}
		var averageMicros = (System.nanoTime() - start) / MEASURED / 1_000;

		System.out.printf("Average POST with %,d people in store: %,d µs%n", FAMILIES * 4, averageMicros);
	}

	// Each family is two partners with two children: four people, none of them a match.
	private static Person parent(int family) {
		var id = 4L * family + 1;
		return new Person(new PersonId(id), null, null, Set.of(), new PersonId(id + 1),
				Set.of(new PersonId(id + 2), new PersonId(id + 3)));
	}

	private void post(int family) {
		var id = 4L * family + 1;
		var json = """
				{"id": %d, "partner": {"id": %d}, "children": [{"id": %d}, {"id": %d}]}
				""".formatted(id, id + 1, id + 2, id + 3);
		var result = mvc.post()
				.uri("/api/v1/people")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json)
				.exchange();
		assertThat(result).hasStatus(444);
	}
}
