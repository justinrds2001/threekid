# Three-Kid Family Challenge

A Spring Boot REST service that stores people one at a time and, after every POST, returns everyone who has a partner and exactly three children with that partner, at least one of them under 18.

## Running

You need JDK 25. If it is not installed, Gradle downloads it on the first build.

```bash
./gradlew bootRun      # start the service on port 8080
./gradlew build        # compile and run all tests
./gradlew benchmark    # measure a POST with 10,000 people in store
```

A short session with `curl`:

```bash
post() { curl -s -w '\n%{http_code}\n' -X POST localhost:8080/api/v1/people \
  -H 'Content-Type: application/json' -d "$1"; }

post '{"id": 1, "name": "Ada", "birthDate": "1990-05-01", "partner": {"id": 2}}'          # 444
post '{"id": 10, "birthDate": "2015-01-01", "parent1": {"id": 1}, "parent2": {"id": 2}}'  # 444
post '{"id": 11, "birthDate": "1990-01-01", "parent1": {"id": 1}, "parent2": {"id": 2}}'  # 444
post '{"id": 12, "birthDate": "1990-01-01", "parent1": {"id": 1}, "parent2": {"id": 2}}'  # 200, Ada and person 2
post '{"id": 3, "children": [{"id": 10}]}'                                                 # 409, child 10 already has two parents
```

Data lives in memory and is gone after a restart.

## Architecture

The code follows a light hexagonal layout in four packages:

| Package | Contents |
| --- | --- |
| `domain` | `Person`, `PersonId`, `FamilyLinker`, `FamilyPatternMatcher` and the domain exceptions. Plain Java without Spring. |
| `application` | `PersonService` and the `PersonRepository` port. |
| `infrastructure` | The in-memory repository and the `Clock` bean. |
| `web` | The controller, request and response records, and the mapping of exceptions to `400` and `409`. |

A POST goes through these steps:

1. The controller validates the request and converts it to a domain `Person`.
2. `FamilyLinker` works out every record that has to change. The incoming record is the truth about its own relations, and the other side is repaired to match. If Ada names three children, each child gets Ada as a parent. If an update leaves a child out, that child loses Ada as a parent. A child that would get a third parent stops the whole POST with a `409` before anything is stored.
3. `PersonService` stores the changed records and reassesses only those people.
4. The service keeps a set of everyone with the right family shape: a partner and exactly three children who all have that partner as a parent. The age check happens when the matches are returned, using the `Clock`, so nobody keeps matching after their youngest child turns 18.

`save` is `synchronized`, so concurrent POSTs cannot overwrite each other's links.

## Assumptions

Every open question in the assignment, with the options I weighed and why I chose what I did, is in [`docs/LOGBOEK.md`](docs/LOGBOEK.md) (in Dutch). The most important ones:

- A child without a birth date does not count as under 18, because that cannot be proven.
- A third parent for a child is rejected with `409` instead of silently replacing one of the two parents.
- Relations are repaired automatically on both sides, including partners and links that an update leaves out.
- A placeholder, a person only known through references, can match if other records give them a partner and children.
- An invalid request gets `400` with a `ProblemDetail` body. The body of a `444` is empty.

## Time spent

About 2 hours and 45 minutes, against an estimate of 2 hours. The breakdown per step is in [`docs/LOGBOEK.md`](docs/LOGBOEK.md). Most of the extra time went into relation repair (step 4) and performance (step 6). I skipped phase 2 to stay close to the estimate.

## Performance

`./gradlew benchmark` fills the store with 10,000 people (2,500 couples with two children each) and measures the average of 1,000 POSTs through MockMvc, after 1,000 warm-up POSTs. Each POST updates an existing parent. The normal build skips this test.

| Version | Average POST |
| --- | --- |
| Full scan after every POST | 385 µs |
| Only reassess the people the POST changed | 165 µs |

The remaining time is mostly MockMvc, JSON and the linker. It no longer grows with the number of people, while the full scan did.

## Approach and AI use

I started with a step plan and worked through it one step at a time: make it work, make it right, make it fast. Every behaviour started with a failing test, and every commit is one small change that builds. For tests that guard a boundary, such as the 18th birthday, I temporarily broke the code to check that the test actually fails.

I used Claude Code as a pair programmer. It proposed an approach for each step, wrote the code test-first and ran the build. I made every design choice from two or three options and approved every commit before it was made. A few moments from the process:

- To make a POST atomic, I first had a test save 100 children of the same parent at the same time. Without a lock, two threads would read the same version of the parent and the last write would drop the other child. The parent ended up with only 19 of the 100 children. With `synchronized` on `save` it keeps all 100.
- I asked whether the DTOs should move to their own package. We kept them in `web`, so the mapping methods can stay package-private.
- Before writing the README I started the app for real and replayed the scenario in Bruno, because until then everything had only been proven through tests.

## With more time

- **Phase 2, `DELETE /api/v1/people`.** Keep a set of deleted ids. Deleting removes the person and their links on both sides, and the linker and matcher skip any deleted id from then on.
- **Persistent storage.** A database adapter behind the existing port, with each POST in a transaction so a failure halfway leaves nothing behind. The set of matching people would then be rebuilt from storage at startup.
- **Field errors in the `400` body.** Spring's default detail only says "Invalid request content."
- **A `web.dto` package** once the web layer grows beyond a few classes.
- **A load test with many clients**, for example with `k6`, to measure throughput under the lock instead of the time of a single POST.
