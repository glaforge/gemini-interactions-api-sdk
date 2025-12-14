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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
 * InteractionParams.Request request = new InteractionParams.Request(
 *     new InteractionParams.ModelInteractionParams(
 *         "gemini-2.5-flash",
 *         List.of(new Content(List.of(new Content.Part(new Content.Text("Hello, world!")))))
 *     )
 * );
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
        this.objectMapper = new ObjectMapper()
            .registerModule(new Jdk8Module());
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new interaction.
     *
     * @param request The interaction request parameters (Model or Agent).
     * @return The created Interaction.
     * @throws IOException          If an I/O error occurs.
     * @throws InterruptedException If the operation is interrupted.
     * @see <a href="https://ai.google.dev/api/interactions#method:-interactions.create">Create Interaction API Reference</a>
     */
    public Interaction create(InteractionParams.Request request) throws IOException, InterruptedException {
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
    }

    /**
     * Retrieves an interaction by ID.
     *
     * @param id The interaction ID.
     * @return The Interaction.
     * @throws IOException          If an I/O error occurs.
     * @throws InterruptedException If the operation is interrupted.
     * @see <a href="https://ai.google.dev/api/interactions#method:-interactions.get">Get Interaction API Reference</a>
     */
    public Interaction get(String id) throws IOException, InterruptedException {
        String url = String.format("%s/%s/interactions/%s", baseUrl, version, id);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("x-goog-api-key", apiKey)
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        checkError(response);

        return objectMapper.readValue(response.body(), Interaction.class);
    }

    /**
     * Deletes an interaction by ID.
     *
     * @param id The interaction ID.
     * @throws IOException          If an I/O error occurs.
     * @throws InterruptedException If the operation is interrupted.
     * @see <a href="https://ai.google.dev/api/interactions#method:-interactions.delete">Delete Interaction API Reference</a>
     */
    public void delete(String id) throws IOException, InterruptedException {
        String url = String.format("%s/%s/interactions/%s", baseUrl, version, id);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("x-goog-api-key", apiKey)
            .DELETE()
            .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        checkError(response);
    }

    /**
     * Cancels an interaction by ID.
     *
     * @param id The interaction ID.
     * @return The updated Interaction (status should be cancelled).
     * @throws IOException          If an I/O error occurs.
     * @throws InterruptedException If the operation is interrupted.
     */
    public Interaction cancel(String id) throws IOException, InterruptedException {
        String url = String.format("%s/%s/interactions/%s/cancel", baseUrl, version, id);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("x-goog-api-key", apiKey)
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        checkError(response);

        return objectMapper.readValue(response.body(), Interaction.class);
    }

    private void checkError(HttpResponse<String> response) throws IOException {
         if (response.statusCode() >= 400) {
            throw new IOException("API Request failed with status code: " + response.statusCode() + ", body: " + response.body());
        }
    }

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
