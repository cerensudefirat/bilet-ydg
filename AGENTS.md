AGENTS.md

Purpose
- Provide brief, repo-specific instructions for coding agents working in this project.

Project Basics
- Stack: Spring Boot 3, Java 17, JPA, H2/Postgres.
- Build tool: Maven wrapper (`./mvnw` on macOS/Linux, `.\mvnw.cmd` on Windows).

Run / Test
- Unit tests (skip ITs): `./mvnw -DskipITs test`
- Integration tests: `./mvnw verify`
- App run (local JVM): `./mvnw spring-boot:run`
- Docker: `docker compose up -d --build` (brings Postgres + app)

Database Notes
- Default local run may require Postgres credentials; prefer Docker Compose for a known-good setup.
- Integration tests use H2 in-memory via `src/test/resources/application-test.properties`.

Editing Guidelines
- Keep changes minimal and focused.
- Prefer updating existing configs over adding new files unless required.
- If you introduce new config keys, update `README.md` briefly.
