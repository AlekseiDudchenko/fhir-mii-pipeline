# CLAUDE.md

## Project overview

FHIR R4 REST API with MII Kerndatensatz profile validation. Spring Boot 3.4 + HAPI FHIR 7.6 plain server (not JPA server). Portfolio/demo project — clarity and correctness over completeness.

## Tech stack

- Java 21, Spring Boot 3.4, Maven
- HAPI FHIR 7.6 (`RestfulServer`, not full JPA)
- PostgreSQL 16 (jsonb storage for raw FHIR resources)
- H2 in-memory for tests (PostgreSQL compatibility mode)
- JUnit 5, Docker + docker-compose

## Build & test

```bash
mvn compile          # compile
mvn test             # run all tests (uses H2, no Postgres needed)
mvn package -DskipTests  # build JAR
docker-compose up --build # run app + postgres
```

## Architecture

- `/fhir/*` — HAPI `RestfulServer` servlet (not Spring MVC controllers)
- Resource providers implement `IResourceProvider` with HAPI annotations (`@Create`, `@Read`)
- Validation uses `NpmPackageValidationSupport` with bundled MII profile packages in `src/main/resources/packages/`
- Persistence stores raw FHIR JSON in a single `fhir_resources` table (no relational mapping)

## Package structure

```
com.alekseidudchenko.fhirmiipipeline
  ├── config/        FhirContext bean, RestfulServer servlet registration
  ├── resource/      FHIR resource providers (Patient, Observation)
  ├── validation/    Profile validator (MII Kerndatensatz)
  ├── persistence/   JDBC repository
  └── model/         DTOs (reserved)
```

## Conventions

- No authentication/authorization — out of scope
- Commit messages: imperative mood, first line under 72 chars, body explains "why"
- Keep dependencies minimal — no Lombok, no MapStruct, no unnecessary abstractions
- Tests must pass before pushing (`mvn test`)
- PR descriptions in English
