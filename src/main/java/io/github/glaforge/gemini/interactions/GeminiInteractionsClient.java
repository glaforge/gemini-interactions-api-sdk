/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.glaforge.gemini.interactions;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import io.github.glaforge.gemini.interactions.model.Events;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Client for the Gemini Interactions API.
 * <p>
 * This client allows you to interact with the Gemini API to create interactions, retrieve past interactions, and more.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * GeminiInteractionsClient client = GeminiInteractionsClient.builder()
 *     .apiKey(System.getenv("GEMINI_API_KEY"))
 *     .build();
 *
 * InteractionParams.Request request = ModelInteractionParams.builder()
 *     .model("gemini-2.5-flash")
 *     .input("Hello, world!")
 *     .build();
 *
 * Interaction interaction = client.create(request);
 * System.out.println(interaction.outputs().get(0).text());
 * }</pre>
 */
public class GeminiInteractionsClient {

    private static final String DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com";
    private static final String DEFAULT_VERSION = "v1beta";

    private final String baseUrl;
    private final String version;
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private GeminiInteractionsClient(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.version = builder.version;
        this.apiKey = builder.apiKey;
        this.httpClient = builder.httpClient != null ? builder.httpClient : HttpClient.newHttpClient();
        this.objectMapper = JsonMapper.builder()
            .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
            .build();
    }

    /**
     * Creates a new builder for the GeminiInteractionsClient.
     *
     * @return A new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new interaction.
     *
     * @param request The interaction request parameters (Model or Agent).
     * @return The created Interaction.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     * @see <a href="https://ai.google.dev/api/interactions-api#CreateInteraction">Create Interaction API Reference</a>
     */
    public Interaction create(InteractionParams.Request request) {
        try {
            String requestBody = objectMapper.writeValueAsString(request);
            String url = String.format("%s/%s/interactions", baseUrl, version);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), Interaction.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Creates a streaming interaction.
     *
     * @param request The interaction request parameters (Model or Agent).
     * @return A Stream of Events.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Stream<Events> stream(InteractionParams.Request request) {
        try {
            String requestBody = objectMapper.writeValueAsString(request);
            String url = String.format("%s/%s/interactions?alt=sse", baseUrl, version);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<Stream<String>> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofLines());

            if (response.statusCode() >= 400) {
                String errorBody = response.body().collect(Collectors.joining("\n"));
                throw new GeminiInteractionsException("API Request failed", response.statusCode(), errorBody);
            }

            return response.body()
                .filter(line -> line.startsWith("data: "))
                .map(line -> line.substring(6))
                .filter(json -> !json.equals("[DONE]"))
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, Events.class);
                    } catch (JacksonException e) {
                        throw new GeminiInteractionsException("Failed to parse event", e);
                    }
                });
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Retrieves an interaction by ID.
     *
     * @param id The interaction ID.
     * @return The Interaction.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     * @see <a href="https://ai.google.dev/api/interactions-api#getInteractionById">Get Interaction API Reference</a>
     */
    public Interaction get(String id) {
        return get(id, false);
    }

    /**
     * Retrieves an interaction by ID, optionally including the original input.
     *
     * @param id           The interaction ID.
     * @param includeInput Whether to include the input in the response.
     * @return The Interaction.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     * @see <a href="https://ai.google.dev/api/interactions-api#getInteractionById">Get Interaction API Reference</a>
     */
    public Interaction get(String id, boolean includeInput) {
        try {
            String url = String.format("%s/%s/interactions/%s", baseUrl, version, id);
            if (includeInput) {
                url += "?include_input=true";
            }

            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-goog-api-key", apiKey)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), Interaction.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Deletes an interaction by ID.
     *
     * @param id The interaction ID.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     * @see <a href="https://ai.google.dev/api/interactions-api#deleteInteraction">Delete Interaction API Reference</a>
     */
    public void delete(String id) {
        try {
            String url = String.format("%s/%s/interactions/%s", baseUrl, version, id);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-goog-api-key", apiKey)
                .DELETE()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Cancels an interaction by ID.
     *
     * @param id The interaction ID.
     * @return The updated Interaction (status should be cancelled).
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     * @see <a href="https://ai.google.dev/api/interactions-api#cancelInteractionById">Cancel Interaction API Reference</a>
     */
    public Interaction cancel(String id) {
        try {
            String url = String.format("%s/%s/interactions/%s/cancel", baseUrl, version, id);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), Interaction.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    private void checkError(HttpResponse<String> response) {
         if (response.statusCode() >= 400) {
            throw new GeminiInteractionsException("API Request failed", response.statusCode(), response.body());
        }
    }

    /**
     * Builder for {@link GeminiInteractionsClient}.
     */
    public static class Builder {
        /** Creates a new Builder. */
        public Builder() {}
        private String baseUrl = DEFAULT_BASE_URL;
        private String version = DEFAULT_VERSION;
        private String apiKey;
        private HttpClient httpClient;

        /**
         * Sets the base URL.
         *
         * @param baseUrl The base URL.
         * @return This builder.
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Sets the API version.
         *
         * @param version The API version.
         * @return This builder.
         */
        public Builder version(String version) {
            this.version = version;
            return this;
        }

        /**
         * Sets the API key.
         *
         * @param apiKey The API key.
         * @return This builder.
         */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         * Sets the HTTP client.
         *
         * @param httpClient The HTTP client.
         * @return This builder.
         */
        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        /**
         * Builds the GeminiInteractionsClient.
         *
         * @return The GeminiInteractionsClient.
         * @throws IllegalStateException If the API key is not provided.
         */
        public GeminiInteractionsClient build() {
            if (apiKey == null) {
                throw new IllegalStateException("API Key must be provided");
            }
            return new GeminiInteractionsClient(this);
        }
    }
}
