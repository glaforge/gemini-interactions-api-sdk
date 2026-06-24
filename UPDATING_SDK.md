# Updating the Gemini Interactions API SDK

This document describes how to update the SDK when the remote Gemini Interactions API evolves.

## Updating the OpenAPI Specification

The SDK models and client are built around the Gemini Interactions OpenAPI specification. To ensure the SDK is up-to-date, follow these steps:

1. **Locate the remote spec**: The official remote `openapi.json` file is hosted at:
   `https://ai.google.dev/static/api/interactions.openapi.json`

2. **Compare with the local spec**: You can check for updates by comparing the remote specification against the local copy at the root of the project (`openapi.json`):
   ```bash
   curl -s https://ai.google.dev/static/api/interactions.openapi.json > remote_openapi.json
   diff -u openapi.json remote_openapi.json
   ```

3. **Update the local spec**: If there are differences (such as new models, new event types, or changes in the expected payloads), download the latest version and overwrite the local `openapi.json`:
   ```bash
   curl -s https://ai.google.dev/static/api/interactions.openapi.json > openapi.json
   ```

4. **Update SDK Code**: Review the changes in the `openapi.json` and manually update the Java SDK models (e.g., `Events.java`, `Interaction.java`) to reflect any new endpoints, properties, or Server-Sent Events (SSE) delta types.

5. **Update Documentation & Agent Skills**: Whenever you update the models or capabilities of the SDK, you **must** update `README.md` and the `skills/gemini-interactions-java-api/SKILL.md` file so that developers and AI coding assistants are aware of the latest patterns.
