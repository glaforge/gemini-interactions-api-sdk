# AI Coding Agent Guidelines

Welcome, AI coding agent! This document contains guidelines and conventions for working on the `gemini-interactions-api-sdk` project.
This project is a Java implementation of the [Gemini Interactions API](https://ai.google.dev/gemini-api/docs/interactions/interactions-overview). 
This API goes beyond standard model generation (text, images, audio, video...) by allowing developers to provision, configure, and interact with stateful managed agents (like the Deep Research Agent). 
These agents can be equipped with complex tools (Google Search, Code Execution, File Search, etc.) and execute autonomously inside isolated remote sandbox environments.

## API key requirement
- **API Key**: Set the `GEMINI_API_KEY` environment variable to your API key.
- If you don't have an API key, you can create one [on the Google AI Studio website](https://aistudio.google.com/app/apikey).
- For running the tests, you must have the API key set for the tests to pass. The tests will not run without a valid API key, but they will not fail either (they will be skipped).
- **Never hardcode API keys in source code or commit `.env` files.**

## Commands

### File-scoped (preferred - fast feedback)
- **Run a specific unit test:** `./mvnw test -Dtest=ClassNameTest`
- **Run a specific integration test:** `./mvnw verify -Dtest=ClassNameIT`

### Full suite (only when explicitly requested)
- **Build and package:** `./mvnw clean package`
- **Run all fast tests:** `./mvnw test`
- **Run all integration tests:** `./mvnw verify`

## Code style
- Use standard Java formatting conventions.
- Organize imports systematically. **Always import classes instead of using Fully Qualified Names (FQN). Never use star imports (`import java.util.*;`).**
- Favor records (`public record ...`) over traditional POJOs for immutable configuration models.
- Ensure proper Javadoc for all public API surfaces (including builder classes and enums). Always verify that there are no Javadoc warnings by running `./mvnw clean javadoc:javadoc`.

## Project Structure

The project is structured as a Maven multi-module reactor:
- **`gemini-interactions-api-sdk/`** — The core client SDK:
  - `model/` — Java records representing API resources (`Agent`, `Interaction`, `InteractionParams`, `Tool`, `Config`, `Events`, `Step`, etc.). Most are immutable records with nested `Builder` classes.
  - `GeminiInteractionsClient` — The main SDK entry point. Handles all HTTP communication with the Gemini API (CRUD for agents, interactions, webhooks, environment downloads).
  - `EnvironmentWorkspace` — Stateful wrapper for downloading and inspecting a remote agent's sandbox filesystem (TAR archive).
  - `deserializer/` — Custom Jackson deserializers for polymorphic API responses.
  - `ResearchFrontend` (in `src/test/java`) — A standalone demo web application for the Deep Research agent, deployable to Google Cloud Run. See `researcher-deployment.md` for deployment instructions.
- **`gemini-interactions-server/`** — The server-side integration:
  - `server/` — Contains `InteractionsHandler` for server-side webhook handling.

## Key Documents
- `UPDATING_SDK.md` — Instructions for keeping the SDK implementation in sync with the remote Gemini Interactions API contract via its OpenAPI JSON definition (`openapi.json`). Read this before adding or modifying API models, and remember to update `README.md` and `skills/gemini-interactions-java-api/SKILL.md` to reflect the new capabilities.
- `researcher-deployment.md` — Step-by-step guide to building and deploying the `ResearchFrontend` demo to Google Cloud Run.
- `RELEASE.md` — Release process using JReleaser and GitHub Actions to publish to Maven Central. See `README.md` for additional context on versioning and usage.

## Code Patterns

### ✅ Good Patterns
- **Type-safe union records:** Use dedicated union records (`SpeechConfiguration`, `NetworkConfiguration`, `BaseEnvironment`, `MediaProcessingConfiguration`) rather than `Object` fields to handle polymorphic API payloads.
- **Type-safe overloads:** Provide overloaded builder methods accepting both domain objects, strings, or union records (see `GenerationConfig.Builder.speechConfig()`, `Agent.Builder.baseEnvironment()`).
- **AutoCloseable:** `EnvironmentWorkspace` implements `AutoCloseable`; always use it in a try-with-resources block.

### ❌ Avoid
- Using Fully Qualified Names inline (e.g., `java.io.InputStream`) — always add an import statement.
- Star imports (`import java.util.*;`).
- Mutable POJOs where an immutable `record` would suffice.

## Testing Strategy

This project uses Maven with both the **Surefire** (unit tests) and **Failsafe** (integration tests) plugins to separate fast tests from long-running tests.

### Fast Tests (Unit Tests)
- **Convention:** Files ending in `*Test.java`
- **Execution:** Run using `./mvnw test`
- **When to run:** Run these frequently while iterating on code. They execute mock-based tests and basic assertions that complete in milliseconds.

### Slow Tests (Integration Tests)
- **Convention:** Files ending in `*IT.java` (e.g., `ResearchAgentIT.java`, `IntegrationIT.java`)
- **Execution:** Run using `./mvnw verify` (or `./mvnw integration-test`)
- **When to run:** Run these before making commits, finalizing a pull request, or validating end-to-end functionality. They often instantiate actual agents, connect to external endpoints, and can take up to 10 minutes to complete.

### Writing New Tests
- If your new test is purely local, mocks the `GeminiInteractionsClient`, or executes quickly, name it `*Test.java`.
- If your new test makes live network calls, provisions remote agents, or performs heavy data generation, name it `*IT.java`.

## Permissions

### Allowed without prompting
- Read files, list directories, and search the project code.
- Perform single-file edits or multi-file refactoring.
- Run unit tests on specific files.

### Require approval first
- Making large structural architectural changes.
- Running the full 10-minute integration test suite (`./mvnw verify`).
- Committing or pushing to the repository.

## PR Requirements
- Ensure code is formatted correctly.
- Add or update tests for any changed logic.
- Keep diffs small and focused; avoid committing unnecessary files.
