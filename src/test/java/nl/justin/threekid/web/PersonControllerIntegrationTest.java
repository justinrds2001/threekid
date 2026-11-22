package nl.justin.threekid.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PersonControllerIntegrationTest.FixedClock.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PersonControllerIntegrationTest {

	@Autowired
	private MockMvcTester mvc;

	@Test
	void answers444UntilAPersonMatchesThePattern() {
		assertThat(post("""
				{"id": 1, "name": "Ada", "birthDate": "1990-05-01", "partner": {"id": 2}}
				""")).hasStatus(444).body().isEmpty();
		assertThat(post(child(10, "2015-01-01"))).hasStatus(444);
		assertThat(post(child(11, "1990-01-01"))).hasStatus(444);

		assertThat(post(child(12, "1990-01-01"))).hasStatusOk()
				.bodyJson().extractingPath("$[*].id").asArray().containsExactly(1, 2);
	}

	@Test
	void respondsWithEverythingKnownAboutTheMatchingPeople() {
		post("""
				{"id": 1, "name": "Ada", "birthDate": "1990-05-01", "partner": {"id": 2}}
				""");
		post(child(10, "2015-01-01"));
		post(child(11, "1990-01-01"));

		assertThat(post(child(12, "1990-01-01"))).hasStatusOk().bodyJson().isStrictlyEqualTo("""
				[
				  {"id": 1, "name": "Ada", "birthDate": "1990-05-01",
				   "parent1": null, "parent2": null, "partner": {"id": 2},
				   "children": [{"id": 10}, {"id": 11}, {"id": 12}]},
				  {"id": 2, "name": null, "birthDate": null,
				   "parent1": null, "parent2": null, "partner": {"id": 1},
				   "children": [{"id": 10}, {"id": 11}, {"id": 12}]}
				]
				""");
	}

	private MvcTestResult post(String json) {
		return mvc.post().uri("/api/v1/people").contentType(MediaType.APPLICATION_JSON).content(json).exchange();
	}

	private static String child(long id, String birthDate) {
		return """
				{"id": %d, "birthDate": "%s", "parent1": {"id": 1}, "parent2": {"id": 2}}
				""".formatted(id, birthDate);
	}

	@TestConfiguration
	static class FixedClock {

		@Bean
		@Primary
		Clock fixedClock() {
			return Clock.fixed(Instant.parse("2026-11-22T12:00:00Z"), ZoneOffset.UTC);
		}
	}
}
