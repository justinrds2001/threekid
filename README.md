# Three-Kid Family Challenge

## Running

## Architecture

## Assumptions

## Time spent

## Performance

`./gradlew benchmark` fills the store with 10,000 people (2,500 couples with two children each) and measures the average of 1,000 POSTs through MockMvc, after 1,000 warm-up POSTs. Each POST updates an existing parent. The normal build skips this test.

| Version | Average POST |
| --- | --- |
| Full scan after every POST | 385 µs |
| Only reassess the people the POST changed | 165 µs |

## Approach and AI use

## With more time

- Rebuild the set of matching people from storage at startup, once storage is persistent.
