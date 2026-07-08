# 0001 — Spring Boot 4 / Jackson 3

**Status:** Accepted

## Context

Downstream applications (e.g. the personal AI workbench) run on Spring Boot 4.1 / Spring Framework 7
/ Java 21. Spring Boot 4 ships **Jackson 3** (`tools.jackson.*`) as the default JSON stack and
reorganizes several auto-configuration packages. Spring Boot 3.5 and 4.x cannot coexist on one
classpath, so the library has to move to Boot 4 to be usable from a Boot 4 host.

## Decision

Build `spring-persistent-tasks` against Spring Boot 4.1 (`spring-boot.version` in the root POM).
The required source changes are:

- **Jackson 3:** `JacksonStateSerializer` uses `tools.jackson.databind.ObjectMapper`. Jackson 3
  exceptions are unchecked (`tools.jackson.core.JacksonException`), so the checked-exception handling
  was simplified accordingly. Tests that referenced `com.fasterxml.jackson.*` were moved to the
  `tools.jackson` API.
- **Moved auto-config package:** `@EntityScan` moved from
  `org.springframework.boot.autoconfigure.domain` to
  `org.springframework.boot.persistence.autoconfigure` (Boot 4 split `spring-boot-autoconfigure`
  into per-concern modules).
- **Removed stale imports:** a stray actuator-quartz import and a `scheduling.config.Task` import in
  `TriggerService` (only referenced from Javadoc `{@link}`) were dropped.
- **Liquibase auto-config module:** Boot 4 extracted the Liquibase auto-configuration into its own
  `spring-boot-liquibase` module (no longer part of `spring-boot-autoconfigure`). The `db` module now
  depends on it, so every consumer of `spring-persistent-tasks-db` still gets the changelog applied
  automatically — without each app having to add the module itself.

## Consequences

- The published artifact now **requires Spring Boot 4.x / Jackson 3** on the host. Boot 3 hosts must
  stay on the previous release line.
- `JacksonStateSerializer`'s constructor now takes a `tools.jackson.databind.ObjectMapper` — a
  breaking change for callers that built one explicitly. The default Java serializer is unaffected.
- QueryDSL (`-jakarta`), Liquibase and Hibernate are managed transitively by the Boot 4 BOM and
  required no source changes.
