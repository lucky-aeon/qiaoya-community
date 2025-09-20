# Repository Guidelines

## Project Structure & Module Organization
- Java 17 with Spring Boot 3, built with Maven.
- Source: `src/main/java/org/xhy/community` using DDD layers:
  - `interfaces` (REST controllers, `*Request` DTOs)
  - `application` (use cases, assemblers)
  - `domain` (entities, value objects, `*DomainService`, `*Repository`)
  - `infrastructure` (config, persistence, email, security)
- Config & migrations: `src/main/resources/application.yml`, `src/main/resources/db/migration`.
- Tests: `src/test/java` with profile config in `src/test/resources/application-test.yml`.
- Design docs: `docs/`.

## Build, Test, and Development Commands
- Build & verify: `mvn clean verify`
- Run locally: `DB_HOST=localhost DB_PORT=5432 mvn spring-boot:run`
- Package JAR: `mvn -DskipTests package` (output in `target/`)
- Run tests: `mvn test` (JUnit 5; many tests use `@SpringBootTest` and `@ActiveProfiles("test")`).

## Coding Style & Naming Conventions
- Follow standard Java conventions; 4-space indentation.
- Packages: lowercase; classes: UpperCamelCase; fields/params: lowerCamelCase; constants: UPPER_SNAKE_CASE.
- Naming patterns: requests `*Request` (e.g., `interfaces/user/request/RegisterRequest.java`), controllers `*Controller`, domain services `*DomainService`, app services `*AppService`, entities `*Entity`, repositories `*Repository`, error codes `*ErrorCode`.

## Testing Guidelines
- Frameworks: JUnit 5 + Spring Boot Test.
- Place tests under mirrored packages; name files `*Test.java`.
- Prefer domain-level tests for business rules; use `@SpringBootTest` for integration.
- Tests run with the `test` profile; set `DB_*` env vars as needed per `application-test.yml` (H2 is available for lightweight tests).

## Commit & Pull Request Guidelines
- Commit style: Conventional Commits (seen in history), e.g. `feat(course): add price and cover fields`, `refactor(updatelog): adjust user info retrieval`.
- PRs should include: clear description, linked issues, sample requests/responses for API changes, notes on DB migrations (`src/main/resources/db/migration`), and updated/added tests. Ensure `mvn clean verify` passes.

## Security & Configuration Tips
- Never commit secrets. Configure via env vars used in `application.yml` (e.g., `DB_*`, `ALIYUN_*`).
- Respect layer boundaries: controllers → application → domain → infrastructure; avoid cross-layer coupling.

## Agent-Specific Notes
- When changing APIs, update `interfaces/*/request` DTOs, controllers, and related assemblers/tests.
- Keep patches focused and minimal; avoid unrelated refactors.

