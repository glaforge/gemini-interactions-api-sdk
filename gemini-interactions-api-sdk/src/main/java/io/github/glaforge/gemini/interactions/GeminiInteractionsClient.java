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
import com.google.auth.oauth2.GoogleCredentials;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import io.github.glaforge.gemini.interactions.model.Events;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams;
import io.github.glaforge.gemini.interactions.model.ListWebhooksResponse;
import io.github.glaforge.gemini.interactions.model.PingWebhookResponse;
import io.github.glaforge.gemini.interactions.model.RotateSigningSecretRequest;
import io.github.glaforge.gemini.interactions.model.Agent;
import io.github.glaforge.gemini.interactions.model.ListAgentsResponse;
import io.github.glaforge.gemini.interactions.model.RotateSigningSecretResponse;
import io.github.glaforge.gemini.interactions.model.Webhook;
import io.github.glaforge.gemini.interactions.model.WebhookUpdate;
import io.github.glaforge.gemini.interactions.model.Trigger;
import io.github.glaforge.gemini.interactions.model.TriggerCreateParams;
import io.github.glaforge.gemini.interactions.model.TriggerUpdate;
import io.github.glaforge.gemini.interactions.model.ListTriggersResponse;
import io.github.glaforge.gemini.interactions.model.ListTriggerExecutionsResponse;
import io.github.glaforge.gemini.interactions.model.TriggerExecution;
import io.github.glaforge.gemini.interactions.model.Environment;
import io.github.glaforge.gemini.interactions.model.CreateEnvironmentRequest;
import io.github.glaforge.gemini.interactions.model.NetworkConfiguration;
import io.github.glaforge.gemini.interactions.model.ListEnvironmentsResponse;
import io.github.glaforge.gemini.interactions.model.EnvironmentFile;
import io.github.glaforge.gemini.interactions.model.GetEnvironmentFilesResponse;
import io.github.glaforge.gemini.interactions.model.Source;
import io.github.glaforge.gemini.interactions.model.Credential;
import io.github.glaforge.gemini.interactions.model.CredentialUpdate;
import io.github.glaforge.gemini.interactions.model.ListCredentialsResponse;
import io.github.glaforge.gemini.interactions.model.CreateVoiceRequest;
import io.github.glaforge.gemini.interactions.model.DeleteVoiceResponse;
import io.github.glaforge.gemini.interactions.model.ListVoicesResponse;
import io.github.glaforge.gemini.interactions.model.Voice;
import io.github.glaforge.gemini.interactions.model.VoiceListFilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
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
    private final String project;
    private final String location;
    private final GoogleCredentials credentials;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private GeminiInteractionsClient(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.version = builder.version;
        this.apiKey = builder.apiKey;
        this.project = builder.project;
        this.location = builder.location;
        this.credentials = builder.credentials;
        this.httpClient = builder.httpClient != null ? builder.httpClient : HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(30))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
        this.objectMapper = JsonMapper.builder()
            .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
            .build();
    }

    private String buildUrl(String resourcePath) {
        if (project != null) {
            String host = baseUrl != null && !baseUrl.equals(DEFAULT_BASE_URL) ? baseUrl : (location.equals("global") ? "https://aiplatform.googleapis.com" : String.format("https://%s-aiplatform.googleapis.com", location));
            return String.format("%s/%s/projects/%s/locations/%s/%s", host, version, project, location, resourcePath);
        } else {
            String host = baseUrl != null ? baseUrl : DEFAULT_BASE_URL;
            return String.format("%s/%s/%s", host, version, resourcePath);
        }
    }

    private HttpRequest.Builder newRequestBuilder(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(java.time.Duration.ofMinutes(5))
            .header("Api-Revision", "2026-05-20");

        if (project != null) {
            try {
                credentials.refreshIfExpired();
                builder.header("Authorization", "Bearer " + credentials.getAccessToken().getTokenValue());
            } catch (IOException e) {
                throw new GeminiInteractionsException("Failed to refresh Google Cloud credentials", e);
            }
        } else {
            builder.header("x-goog-api-key", apiKey);
        }
        return builder;
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
            String url = buildUrl("interactions");

            HttpRequest httpRequest = newRequestBuilder(url)
                .header("Content-Type", "application/json")
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
            String url = buildUrl("interactions?alt=sse");

            HttpRequest httpRequest = newRequestBuilder(url)
                .header("Content-Type", "application/json")
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
                .takeWhile(json -> !json.equals("[DONE]"))
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

            HttpRequest httpRequest = newRequestBuilder(url)
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

            HttpRequest httpRequest = newRequestBuilder(url)
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

            HttpRequest httpRequest = newRequestBuilder(url)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), Interaction.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Cancels an interaction by ID.
     * Alias for {@link #cancel(String)}.
     *
     * @param id The interaction ID.
     * @return The updated Interaction (status should be cancelled).
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     * @see #cancel(String)
     */
    public Interaction cancelInteraction(String id) {
        return cancel(id);
    }

    // --- Webhook Operations ---

    /**
     * Creates a new webhook.
     *
     * @param webhook The webhook to create.
     * @return The created Webhook.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Webhook createWebhook(Webhook webhook) {
        try {
            String requestBody = objectMapper.writeValueAsString(webhook);
            String url = buildUrl("webhooks");

            HttpRequest httpRequest = newRequestBuilder(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), Webhook.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Retrieves a webhook by ID.
     *
     * @param id The webhook ID.
     * @return The Webhook.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Webhook getWebhook(String id) {
        try {
            String url = String.format("%s/%s/webhooks/%s", baseUrl, version, id);

            HttpRequest httpRequest = newRequestBuilder(url)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), Webhook.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Lists webhooks.
     *
     * @param pageSize  The maximum number of webhooks to return.
     * @param pageToken A page token, received from a previous list call.
     * @return The ListWebhooksResponse.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public ListWebhooksResponse listWebhooks(Integer pageSize, String pageToken) {
        try {
            StringBuilder urlBuilder = new StringBuilder(buildUrl("webhooks"));
            boolean hasParam = false;
            if (pageSize != null) {
                urlBuilder.append("?page_size=").append(pageSize);
                hasParam = true;
            }
            if (pageToken != null && !pageToken.isEmpty()) {
                urlBuilder.append(hasParam ? "&" : "?").append("page_token=").append(pageToken);
            }

            HttpRequest httpRequest = newRequestBuilder(urlBuilder.toString())
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), ListWebhooksResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Updates a webhook.
     *
     * @param id     The webhook ID.
     * @param update The webhook update payload.
     * @return The updated Webhook.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Webhook updateWebhook(String id, WebhookUpdate update) {
        try {
            String requestBody = objectMapper.writeValueAsString(update);
            String url = String.format("%s/%s/webhooks/%s", baseUrl, version, id);

            HttpRequest httpRequest = newRequestBuilder(url)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), Webhook.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Deletes a webhook by ID.
     *
     * @param id The webhook ID.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public void deleteWebhook(String id) {
        try {
            String url = String.format("%s/%s/webhooks/%s", baseUrl, version, id);

            HttpRequest httpRequest = newRequestBuilder(url)
                .DELETE()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Pings a webhook to verify it is working.
     *
     * @param id The webhook ID.
     * @return The PingWebhookResponse.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public PingWebhookResponse pingWebhook(String id) {
        try {
            String requestBody = "{}";
            String url = String.format("%s/%s/webhooks/%s:ping", baseUrl, version, id);

            HttpRequest httpRequest = newRequestBuilder(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), PingWebhookResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Rotates the signing secret for a webhook.
     *
     * @param id      The webhook ID.
     * @param request The rotation request payload.
     * @return The RotateSigningSecretResponse.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public RotateSigningSecretResponse rotateSigningSecret(String id, RotateSigningSecretRequest request) {
        try {
            String requestBody = objectMapper.writeValueAsString(request);
            String url = String.format("%s/%s/webhooks/%s:rotateSigningSecret", baseUrl, version, id);

            HttpRequest httpRequest = newRequestBuilder(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), RotateSigningSecretResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    // --- Credential Operations ---

    /**
     * Creates a new server-managed Credential.
     *
     * @param credential The credential to provision.
     * @return The created Credential (metadata only, secrets are omitted).
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Credential createCredential(Credential credential) {
        try {
            String requestBody = objectMapper.writeValueAsString(credential);
            String url = buildUrl("credentials");

            HttpRequest httpRequest = newRequestBuilder(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), Credential.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Retrieves metadata for a Credential by ID.
     *
     * @param id The credential ID.
     * @return The Credential metadata (secrets are omitted).
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Credential getCredential(String id) {
        try {
            String url = String.format("%s/%s/credentials/%s", baseUrl, version, id);

            HttpRequest httpRequest = newRequestBuilder(url)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), Credential.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Lists credentials.
     *
     * @return The ListCredentialsResponse.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public ListCredentialsResponse listCredentials() {
        return listCredentials(null, null);
    }

    /**
     * Lists credentials with pagination.
     *
     * @param pageSize  The maximum number of credentials to return.
     * @param pageToken A page token, received from a previous list call.
     * @return The ListCredentialsResponse.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public ListCredentialsResponse listCredentials(Integer pageSize, String pageToken) {
        try {
            StringBuilder urlBuilder = new StringBuilder(buildUrl("credentials"));
            boolean hasParam = false;
            if (pageSize != null) {
                urlBuilder.append("?page_size=").append(pageSize);
                hasParam = true;
            }
            if (pageToken != null && !pageToken.isEmpty()) {
                urlBuilder.append(hasParam ? "&" : "?").append("page_token=").append(pageToken);
            }

            HttpRequest httpRequest = newRequestBuilder(urlBuilder.toString())
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), ListCredentialsResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Updates or rotates a Credential (PATCH /v1beta/credentials/{id}).
     *
     * @param id     The credential ID.
     * @param update The credential update payload containing the rotated secrets.
     * @return The updated Credential metadata.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Credential updateCredential(String id, CredentialUpdate update) {
        try {
            String requestBody = objectMapper.writeValueAsString(update);
            String url = String.format("%s/%s/credentials/%s", baseUrl, version, id);

            HttpRequest httpRequest = newRequestBuilder(url)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), Credential.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Deletes a Credential by ID.
     *
     * @param id The credential ID.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public void deleteCredential(String id) {
        try {
            String url = String.format("%s/%s/credentials/%s", baseUrl, version, id);

            HttpRequest httpRequest = newRequestBuilder(url)
                .DELETE()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    // --- Voice Operations ---

    /**
     * Creates a custom voice from a prompt (Voice Design) or audio samples (Voice Replication).
     *
     * @param request The voice creation request.
     * @return The created Voice.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Voice createVoice(CreateVoiceRequest request) {
        try {
            String requestBody = objectMapper.writeValueAsString(request);
            String url = buildUrl("voices");

            HttpRequest httpRequest = newRequestBuilder(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), Voice.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Creates a custom voice with default storage behavior.
     *
     * @param voice The voice specification.
     * @return The created Voice.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Voice createVoice(Voice voice) {
        return createVoice(CreateVoiceRequest.of(voice));
    }

    /**
     * Creates a custom voice specifying whether it should be stored.
     *
     * @param voice The voice specification.
     * @param store Whether to store the voice in Google's managed project storage.
     * @return The created Voice.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Voice createVoice(Voice voice, boolean store) {
        return createVoice(CreateVoiceRequest.of(voice, store));
    }

    /**
     * Retrieves a stored custom voice by ID or resource name.
     *
     * @param id The voice ID (e.g. "voice_abc123") or resource name (e.g. "voices/voice_abc123").
     * @return The Voice.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Voice getVoice(String id) {
        try {
            String voiceId = id != null && id.startsWith("voices/") ? id.substring("voices/".length()) : id;
            String url = String.format("%s/%s/voices/%s", baseUrl, version, voiceId);

            HttpRequest httpRequest = newRequestBuilder(url)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), Voice.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Lists voices from Google's voice catalog and project-stored custom voices.
     *
     * @return The ListVoicesResponse.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public ListVoicesResponse listVoices() {
        return listVoices(null);
    }

    /**
     * Lists voices with filtering parameters (language, gender, pitch, type, search, etc.).
     *
     * @param filter The filter parameters.
     * @return The ListVoicesResponse.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public ListVoicesResponse listVoices(VoiceListFilter filter) {
        try {
            StringBuilder urlBuilder = new StringBuilder(buildUrl("voices"));
            if (filter != null) {
                filter.appendQueryParams(urlBuilder);
            }

            HttpRequest httpRequest = newRequestBuilder(urlBuilder.toString())
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), ListVoicesResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Deletes a stored custom voice by ID or resource name.
     *
     * @param id The voice ID (e.g. "voice_abc123") or resource name (e.g. "voices/voice_abc123").
     * @return The DeleteVoiceResponse.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public DeleteVoiceResponse deleteVoice(String id) {
        try {
            String voiceId = id != null && id.startsWith("voices/") ? id.substring("voices/".length()) : id;
            String url = String.format("%s/%s/voices/%s", baseUrl, version, voiceId);

            HttpRequest httpRequest = newRequestBuilder(url)
                .DELETE()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), DeleteVoiceResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    // --- Agent Operations ---

    /**
     * Creates a new custom Agent.
     *
     * @param agent The agent definition to create.
     * @return The created Agent.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Agent createAgent(Agent agent) {
        try {
            String requestBody = objectMapper.writeValueAsString(agent);
            String url = buildUrl("agents");

            HttpRequest httpRequest = newRequestBuilder(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), Agent.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Retrieves a custom Agent by ID.
     *
     * @param id The agent ID.
     * @return The Agent.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Agent getAgent(String id) {
        try {
            String url = String.format("%s/%s/agents/%s", baseUrl, version, id);

            HttpRequest httpRequest = newRequestBuilder(url)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), Agent.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Lists custom agents.
     *
     * @param pageSize  The maximum number of agents to return.
     * @param pageToken A page token, received from a previous list call.
     * @return The ListAgentsResponse.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public ListAgentsResponse listAgents(Integer pageSize, String pageToken) {
        try {
            StringBuilder urlBuilder = new StringBuilder(buildUrl("agents"));
            boolean hasParam = false;
            if (pageSize != null) {
                urlBuilder.append("?page_size=").append(pageSize);
                hasParam = true;
            }
            if (pageToken != null && !pageToken.isEmpty()) {
                urlBuilder.append(hasParam ? "&" : "?").append("page_token=").append(pageToken);
            }

            HttpRequest httpRequest = newRequestBuilder(urlBuilder.toString())
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), ListAgentsResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Deletes a custom Agent by ID.
     *
     * @param id The agent ID.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public void deleteAgent(String id) {
        try {
            String url = String.format("%s/%s/agents/%s", baseUrl, version, id);

            HttpRequest httpRequest = newRequestBuilder(url)
                .DELETE()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    // --- Environment Operations ---

    /**
     * Creates a new execution environment.
     *
     * @param request The environment creation request.
     * @return The created Environment.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Environment createEnvironment(CreateEnvironmentRequest request) {
        try {
            String requestBody = objectMapper.writeValueAsString(request);
            String url = buildUrl("environments");

            HttpRequest httpRequest = newRequestBuilder(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), Environment.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Creates an execution environment using a raw or legacy network configuration object.
     *
     * @param network Network configuration (NetworkConfiguration, EnvironmentNetworkEgressAllowlist, or string preset like "disabled").
     * @param sources Sources to mount into the environment.
     * @return The created Environment.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Environment createEnvironment(Object network, java.util.List<Source> sources) {
        return createEnvironment(new CreateEnvironmentRequest(NetworkConfiguration.of(network), sources));
    }

    /**
     * Creates an execution environment with a NetworkConfiguration.
     *
     * @param network Network configuration.
     * @param sources Sources to mount into the environment.
     * @return The created Environment.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Environment createEnvironment(NetworkConfiguration network, java.util.List<Source> sources) {
        return createEnvironment(new CreateEnvironmentRequest(network, sources));
    }

    /**
     * Creates a new execution environment cloned/forked from an existing environment.
     *
     * @param fromEnvironment The source environment ID or path (e.g. "environments/env_123" or "env_123") to copy from.
     * @return The created Environment.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Environment createEnvironmentFrom(String fromEnvironment) {
        return createEnvironment(CreateEnvironmentRequest.builder().fromEnvironment(fromEnvironment).build());
    }

    /**
     * Creates a new execution environment cloned/forked from an existing environment with a NetworkConfiguration.
     *
     * @param fromEnvironment The source environment ID or path to copy from.
     * @param network Network configuration.
     * @return The created Environment.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Environment createEnvironmentFrom(String fromEnvironment, NetworkConfiguration network) {
        return createEnvironment(CreateEnvironmentRequest.builder().fromEnvironment(fromEnvironment).network(network).build());
    }

    /**
     * Creates a new execution environment cloned/forked from an existing environment with a network configuration object or preset string.
     *
     * @param fromEnvironment The source environment ID or path to copy from.
     * @param network Network configuration (NetworkConfiguration, EnvironmentNetworkEgressAllowlist, or string preset like "disabled").
     * @return The created Environment.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Environment createEnvironmentFrom(String fromEnvironment, Object network) {
        return createEnvironment(CreateEnvironmentRequest.builder().fromEnvironment(fromEnvironment).network(NetworkConfiguration.of(network)).build());
    }

    /**
     * Retrieves an Environment by ID.
     *
     * @param id The environment ID.
     * @return The Environment.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Environment getEnvironment(String id) {
        try {
            String url = String.format("%s/%s/environments/%s", baseUrl, version, id);

            HttpRequest httpRequest = newRequestBuilder(url)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), Environment.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Lists Environments.
     *
     * @return The ListEnvironmentsResponse.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public ListEnvironmentsResponse listEnvironments() {
        return listEnvironments(null, null);
    }

    /**
     * Lists Environments with pagination.
     *
     * @param pageSize  The maximum number of environments to return.
     * @param pageToken A page token, received from a previous list call.
     * @return The ListEnvironmentsResponse.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public ListEnvironmentsResponse listEnvironments(Integer pageSize, String pageToken) {
        try {
            StringBuilder urlBuilder = new StringBuilder(buildUrl("environments"));
            boolean hasParam = false;
            if (pageSize != null) {
                urlBuilder.append("?page_size=").append(pageSize);
                hasParam = true;
            }
            if (pageToken != null && !pageToken.isEmpty()) {
                urlBuilder.append(hasParam ? "&" : "?").append("page_token=").append(pageToken);
            }

            HttpRequest httpRequest = newRequestBuilder(urlBuilder.toString())
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), ListEnvironmentsResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Deletes an Environment.
     *
     * @param id The environment ID.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public void deleteEnvironment(String id) {
        try {
            String url = String.format("%s/%s/environments/%s", baseUrl, version, id);

            HttpRequest httpRequest = newRequestBuilder(url)
                .DELETE()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Retrieves file or directory metadata from an environment's snapshot.
     *
     * @param environmentId Environment ID.
     * @param path          Relative file or directory path in the environment (e.g. "workspace" or "workspace/src/main.py").
     * @return GetEnvironmentFilesResponse containing file metadata.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    private String encodeEnvironmentFilePath(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        String p = path.startsWith("/") ? path.substring(1) : path;
        String[] segments = p.split("/", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) {
                sb.append("/");
            }
            sb.append(URLEncoder.encode(segments[i], StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return sb.toString();
    }

    private String buildUploadBaseUrl() {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return base + "/upload";
    }

    /**
     * Retrieves file or directory metadata from an environment's snapshot.
     *
     * @param environmentId Environment ID.
     * @param path          Relative file or directory path in the environment.
     * @return GetEnvironmentFilesResponse containing file metadata.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public GetEnvironmentFilesResponse getEnvironmentFiles(String environmentId, String path) {
        return getEnvironmentFiles(environmentId, path, null, null, null);
    }

    /**
     * Retrieves file or directory metadata from an environment's snapshot with pagination and options.
     *
     * @param environmentId Environment ID.
     * @param path          Relative file or directory path in the environment.
     * @param pageSize      Optional maximum number of entries to return.
     * @param pageToken     Optional pagination token.
     * @param recursive     Optional flag to recursively list all files in a directory.
     * @return GetEnvironmentFilesResponse containing file metadata.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public GetEnvironmentFilesResponse getEnvironmentFiles(String environmentId, String path, Integer pageSize, String pageToken, Boolean recursive) {
        try {
            String encodedPath = encodeEnvironmentFilePath(path);
            StringBuilder urlBuilder = new StringBuilder(String.format("%s/%s/environments/%s/files%s",
                baseUrl, version, environmentId, encodedPath.isEmpty() ? "" : "/" + encodedPath));
            boolean hasParam = false;
            if (pageSize != null) {
                urlBuilder.append(hasParam ? "&" : "?").append("page_size=").append(pageSize);
                hasParam = true;
            }
            if (pageToken != null && !pageToken.isEmpty()) {
                urlBuilder.append(hasParam ? "&" : "?").append("page_token=").append(URLEncoder.encode(pageToken, StandardCharsets.UTF_8));
                hasParam = true;
            }
            if (recursive != null) {
                urlBuilder.append(hasParam ? "&" : "?").append("recursive=").append(recursive);
                hasParam = true;
            }

            HttpRequest httpRequest = newRequestBuilder(urlBuilder.toString())
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            checkError(response);

            return objectMapper.readValue(response.body(), GetEnvironmentFilesResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Retrieves metadata for a specific file in an environment.
     *
     * @param environmentId Environment ID.
     * @param path          Relative file path in the environment.
     * @return An Optional containing the EnvironmentFile if found, or empty if not found.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Optional<EnvironmentFile> getEnvironmentFile(String environmentId, String path) {
        GetEnvironmentFilesResponse response = getEnvironmentFiles(environmentId, path);
        if (response.files() != null && !response.files().isEmpty()) {
            return Optional.of(response.files().get(0));
        }
        return Optional.empty();
    }

    /**
     * Downloads the raw content of a specific file, or a TAR archive of a directory, from an environment.
     *
     * @param environmentId Environment ID.
     * @param path          Relative file or directory path in the environment (or empty/null for root).
     * @return An InputStream containing the raw file content or TAR archive.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public InputStream downloadEnvironmentFile(String environmentId, String path) {
        return downloadEnvironmentFile(environmentId, path, (Boolean) null);
    }

    /**
     * Downloads the raw content of a specific file, or a TAR archive of a directory, from an environment.
     *
     * @param environmentId Environment ID.
     * @param path          Relative file or directory path in the environment (or empty/null for root).
     * @param recursive     Optional flag. When downloading a directory, whether to recursively include subdirectories.
     * @return An InputStream containing the raw file content or TAR archive.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public InputStream downloadEnvironmentFile(String environmentId, String path, Boolean recursive) {
        try {
            String encodedPath = encodeEnvironmentFilePath(path);
            StringBuilder urlBuilder = new StringBuilder(String.format("%s/%s/environments/%s/files%s?alt=media",
                baseUrl, version, environmentId, encodedPath.isEmpty() ? "" : "/" + encodedPath));
            if (recursive != null) {
                urlBuilder.append("&recursive=").append(recursive);
            }

            HttpRequest httpRequest = newRequestBuilder(urlBuilder.toString())
                .GET()
                .build();

            HttpResponse<InputStream> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() >= 300) {
                try (InputStream errorStream = response.body()) {
                    String errorBody = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                    throw new GeminiInteractionsException("API Request failed to download environment file", response.statusCode(), errorBody);
                }
            }

            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Downloads the raw content of a file or directory archive and writes it directly to an OutputStream.
     *
     * @param environmentId Environment ID.
     * @param path          Relative file or directory path in the environment.
     * @param outputStream  OutputStream to write the content to.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public void downloadEnvironmentFile(String environmentId, String path, OutputStream outputStream) {
        downloadEnvironmentFile(environmentId, path, null, outputStream);
    }

    /**
     * Downloads the raw content of a file or directory archive and writes it directly to an OutputStream.
     *
     * @param environmentId Environment ID.
     * @param path          Relative file or directory path in the environment.
     * @param recursive     Optional flag for directory archive recursion.
     * @param outputStream  OutputStream to write the content to.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public void downloadEnvironmentFile(String environmentId, String path, Boolean recursive, OutputStream outputStream) {
        try (InputStream in = downloadEnvironmentFile(environmentId, path, recursive)) {
            in.transferTo(outputStream);
        } catch (IOException e) {
            throw new GeminiInteractionsException("Failed to stream environment file content", e);
        }
    }

    /**
     * Downloads the raw content of a file or directory archive and saves it to a local file.
     *
     * @param environmentId Environment ID.
     * @param path          Relative file or directory path in the environment.
     * @param targetFile    Destination local file path.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public void downloadEnvironmentFile(String environmentId, String path, Path targetFile) {
        downloadEnvironmentFile(environmentId, path, null, targetFile);
    }

    /**
     * Downloads the raw content of a file or directory archive and saves it to a local file.
     *
     * @param environmentId Environment ID.
     * @param path          Relative file or directory path in the environment.
     * @param recursive     Optional flag for directory archive recursion.
     * @param targetFile    Destination local file path.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public void downloadEnvironmentFile(String environmentId, String path, Boolean recursive, Path targetFile) {
        try (InputStream in = downloadEnvironmentFile(environmentId, path, recursive)) {
            if (targetFile.getParent() != null) {
                Files.createDirectories(targetFile.getParent());
            }
            Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new GeminiInteractionsException("Failed to save environment file to " + targetFile, e);
        }
    }

    /**
     * Downloads the raw bytes of a specific file from an environment.
     *
     * @param environmentId Environment ID.
     * @param path          Relative file path in the environment.
     * @return Byte array containing the file content.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public byte[] downloadEnvironmentFileBytes(String environmentId, String path) {
        try (InputStream in = downloadEnvironmentFile(environmentId, path)) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new GeminiInteractionsException("Failed to read environment file bytes", e);
        }
    }

    /**
     * Uploads file content or extracts a directory archive into an environment sandbox.
     *
     * @param environmentId Environment ID.
     * @param path          Relative destination path in the environment sandbox.
     * @param content       The file or archive content as a byte array.
     * @param mimeType      Optional MIME type of the content. Defaults to application/octet-stream if null or blank.
     * @param overwrite     Optional flag. If true, replaces existing files; otherwise returns 409 Conflict if target exists.
     * @param extract       Optional flag. If true, unpacks the uploaded .tar or .tar.gz archive into the destination directory.
     * @return GetEnvironmentFilesResponse containing metadata for the written file(s).
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public GetEnvironmentFilesResponse uploadEnvironmentFile(String environmentId, String path, byte[] content, String mimeType, Boolean overwrite, Boolean extract) {
        try {
            String encodedPath = encodeEnvironmentFilePath(path);
            StringBuilder urlBuilder = new StringBuilder(String.format("%s/%s/environments/%s/files%s",
                buildUploadBaseUrl(), version, environmentId, encodedPath.isEmpty() ? "" : "/" + encodedPath));
            boolean hasParam = false;
            if (overwrite != null) {
                urlBuilder.append(hasParam ? "&" : "?").append("overwrite=").append(overwrite);
                hasParam = true;
            }
            if (extract != null) {
                urlBuilder.append(hasParam ? "&" : "?").append("extract=").append(extract);
                hasParam = true;
            }

            String contentType = (mimeType != null && !mimeType.isBlank()) ? mimeType : "application/octet-stream";

            HttpRequest httpRequest = newRequestBuilder(urlBuilder.toString())
                .header("Content-Type", contentType)
                .PUT(HttpRequest.BodyPublishers.ofByteArray(content != null ? content : new byte[0]))
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            checkError(response);

            return objectMapper.readValue(response.body(), GetEnvironmentFilesResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Uploads a single file to the environment sandbox.
     *
     * @param environmentId Environment ID.
     * @param path          Relative destination path in the environment sandbox.
     * @param content       The file content as a byte array.
     * @param mimeType      Optional MIME type of the content.
     * @param overwrite     Optional flag to overwrite existing file if true.
     * @return GetEnvironmentFilesResponse containing metadata for the uploaded file.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public GetEnvironmentFilesResponse uploadEnvironmentFile(String environmentId, String path, byte[] content, String mimeType, Boolean overwrite) {
        return uploadEnvironmentFile(environmentId, path, content, mimeType, overwrite, null);
    }

    /**
     * Uploads a text file to the environment sandbox using UTF-8 encoding.
     *
     * @param environmentId Environment ID.
     * @param path          Relative destination path in the environment sandbox.
     * @param textContent   The text content of the file.
     * @param overwrite     Optional flag to overwrite existing file if true.
     * @return GetEnvironmentFilesResponse containing metadata for the uploaded file.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public GetEnvironmentFilesResponse uploadEnvironmentFile(String environmentId, String path, String textContent, Boolean overwrite) {
        byte[] bytes = textContent != null ? textContent.getBytes(StandardCharsets.UTF_8) : new byte[0];
        return uploadEnvironmentFile(environmentId, path, bytes, "text/plain; charset=utf-8", overwrite, null);
    }

    /**
     * Uploads a local file to the environment sandbox.
     *
     * @param environmentId Environment ID.
     * @param path          Relative destination path in the environment sandbox.
     * @param localFile     Path to the local file to upload.
     * @param overwrite     Optional flag to overwrite existing file if true.
     * @return GetEnvironmentFilesResponse containing metadata for the uploaded file.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public GetEnvironmentFilesResponse uploadEnvironmentFile(String environmentId, String path, Path localFile, Boolean overwrite) {
        try {
            byte[] bytes = Files.readAllBytes(localFile);
            String probedType = Files.probeContentType(localFile);
            return uploadEnvironmentFile(environmentId, path, bytes, probedType, overwrite, null);
        } catch (IOException e) {
            throw new GeminiInteractionsException("Failed to read local file for upload: " + localFile, e);
        }
    }

    /**
     * Uploads and extracts a TAR or TAR.GZ directory archive into an environment sandbox.
     *
     * @param environmentId   Environment ID.
     * @param destinationPath Relative destination directory path in the environment sandbox.
     * @param archiveBytes    The archive bytes (.tar or .tar.gz).
     * @param overwrite       Optional flag to overwrite existing files if true.
     * @return GetEnvironmentFilesResponse containing metadata for all extracted files.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public GetEnvironmentFilesResponse uploadEnvironmentArchive(String environmentId, String destinationPath, byte[] archiveBytes, Boolean overwrite) {
        return uploadEnvironmentFile(environmentId, destinationPath, archiveBytes, "application/x-tar", overwrite, true);
    }

    /**
     * Uploads and extracts a local TAR or TAR.GZ directory archive into an environment sandbox.
     *
     * @param environmentId   Environment ID.
     * @param destinationPath Relative destination directory path in the environment sandbox.
     * @param archiveFile     Path to the local .tar or .tar.gz archive file.
     * @param overwrite       Optional flag to overwrite existing files if true.
     * @return GetEnvironmentFilesResponse containing metadata for all extracted files.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public GetEnvironmentFilesResponse uploadEnvironmentArchive(String environmentId, String destinationPath, Path archiveFile, Boolean overwrite) {
        try {
            byte[] bytes = Files.readAllBytes(archiveFile);
            return uploadEnvironmentFile(environmentId, destinationPath, bytes, "application/x-tar", overwrite, true);
        } catch (IOException e) {
            throw new GeminiInteractionsException("Failed to read archive file for upload: " + archiveFile, e);
        }
    }

    // --- Workspace Sandbox Operations ---

    /**
     * Downloads the environment workspace snapshot for a given interaction or environment ID as an InputStream containing the TAR archive.
     * <p>
     * Prefers the modern {@code GET /{version}/environments/{id}/files?alt=media} endpoint,
     * with automatic fallback to the legacy {@code GET /{version}/files/environment-{id}:download?alt=media} endpoint.
     * </p>
     *
     * @param environmentOrInteractionId The environment or interaction ID.
     * @return An InputStream containing the TAR archive.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public InputStream downloadEnvironment(String environmentOrInteractionId) {
        if (environmentOrInteractionId != null && (environmentOrInteractionId.startsWith("env_") || environmentOrInteractionId.startsWith("env-"))) {
            try {
                // Prefer modern environment files download endpoint: GET /{version}/environments/{id}/files?alt=media
                String modernUrl = String.format("%s/%s/environments/%s/files?alt=media", baseUrl, version, environmentOrInteractionId);
                HttpRequest modernRequest = newRequestBuilder(modernUrl)
                    .GET()
                    .build();

                HttpResponse<InputStream> response = httpClient.send(modernRequest, HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() < 300) {
                    return response.body();
                }

                // Close error stream from modern endpoint attempt
                try (InputStream errorStream = response.body()) {
                    errorStream.readAllBytes();
                }
            } catch (IOException | InterruptedException e) {
                // Fall through to legacy endpoint
            }
        }

        try {
            // Fallback / legacy endpoint: GET /{version}/files/environment-{id}:download?alt=media
            String legacyUrl = String.format("%s/%s/files/environment-%s:download?alt=media", baseUrl, version, environmentOrInteractionId);
            HttpRequest legacyRequest = newRequestBuilder(legacyUrl)
                .GET()
                .build();

            HttpResponse<InputStream> legacyResponse = httpClient.send(legacyRequest, HttpResponse.BodyHandlers.ofInputStream());
            if (legacyResponse.statusCode() >= 300) {
                try (InputStream errorStream = legacyResponse.body()) {
                    String errorBody = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                    throw new GeminiInteractionsException("API Request failed to download environment", legacyResponse.statusCode(), errorBody);
                }
            }

            return legacyResponse.body();
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Downloads the environment workspace snapshot for a given interaction or environment ID and writes it directly to an OutputStream.
     *
     * @param environmentOrInteractionId The environment or interaction ID.
     * @param outputStream               The OutputStream to write the TAR archive to.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public void downloadEnvironment(String environmentOrInteractionId, OutputStream outputStream) {
        try (InputStream in = downloadEnvironment(environmentOrInteractionId)) {
            in.transferTo(outputStream);
        } catch (IOException e) {
            throw new GeminiInteractionsException("Failed to stream environment snapshot", e);
        }
    }

    /**
     * Downloads the environment workspace snapshot for a given interaction or environment ID and saves it to a local file.
     *
     * @param environmentOrInteractionId The environment or interaction ID.
     * @param targetFile                 The destination file path for the TAR archive.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public void downloadEnvironment(String environmentOrInteractionId, Path targetFile) {
        try (InputStream in = downloadEnvironment(environmentOrInteractionId)) {
            if (targetFile.getParent() != null) {
                Files.createDirectories(targetFile.getParent());
            }
            Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new GeminiInteractionsException("Failed to save environment snapshot to " + targetFile, e);
        }
    }

    /**
     * Gets the stateful {@link EnvironmentWorkspace} for the given environment or interaction.
     * <p>
     * Note: This method returns a new workspace wrapper. You must call {@link EnvironmentWorkspace#refresh()}
     * on the returned workspace to download its contents.
     * </p>
     *
     * @param environmentOrInteractionId The environment or interaction ID.
     * @return A stateful EnvironmentWorkspace manager.
     */
    public EnvironmentWorkspace getWorkspace(String environmentOrInteractionId) {
        return new EnvironmentWorkspace(environmentOrInteractionId, this);
    }

    /**
     * Gets the stateful {@link EnvironmentWorkspace} for the given environment or interaction.
     *
     * @param environmentOrInteractionId The environment or interaction ID.
     * @return A stateful EnvironmentWorkspace manager.
     */
    public EnvironmentWorkspace getEnvironmentWorkspace(String environmentOrInteractionId) {
        return getWorkspace(environmentOrInteractionId);
    }

    // --- Trigger Operations ---

    /**
     * Creates a new Trigger.
     *
     * @param params The trigger creation parameters.
     * @return The created Trigger.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Trigger createTrigger(TriggerCreateParams params) {
        try {
            String requestBody = objectMapper.writeValueAsString(params);
            String url = buildUrl("triggers");

            HttpRequest httpRequest = newRequestBuilder(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), Trigger.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Retrieves a Trigger by ID.
     *
     * @param id The trigger ID.
     * @return The Trigger.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Trigger getTrigger(String id) {
        try {
            String url = String.format("%s/%s/triggers/%s", baseUrl, version, id);

            HttpRequest httpRequest = newRequestBuilder(url)
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), Trigger.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Lists Triggers with optional filter and pagination parameters.
     *
     * @param filter    An optional filter expression to restrict the returned triggers.
     * @param pageSize  The maximum number of triggers to return.
     * @param pageToken A page token, received from a previous list call.
     * @return The ListTriggersResponse.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public ListTriggersResponse listTriggers(String filter, Integer pageSize, String pageToken) {
        try {
            StringBuilder urlBuilder = new StringBuilder(buildUrl("triggers"));
            boolean hasParam = false;
            if (filter != null && !filter.isEmpty()) {
                urlBuilder.append("?filter=").append(URLEncoder.encode(filter, StandardCharsets.UTF_8));
                hasParam = true;
            }
            if (pageSize != null) {
                urlBuilder.append(hasParam ? "&" : "?").append("page_size=").append(pageSize);
                hasParam = true;
            }
            if (pageToken != null && !pageToken.isEmpty()) {
                urlBuilder.append(hasParam ? "&" : "?").append("page_token=").append(pageToken);
            }

            HttpRequest httpRequest = newRequestBuilder(urlBuilder.toString())
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), ListTriggersResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Lists Triggers.
     *
     * @param pageSize  The maximum number of triggers to return.
     * @param pageToken A page token, received from a previous list call.
     * @return The ListTriggersResponse.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public ListTriggersResponse listTriggers(Integer pageSize, String pageToken) {
        return listTriggers(null, pageSize, pageToken);
    }

    /**
     * Lists Triggers matching a filter.
     *
     * @param filter An optional filter expression to restrict the returned triggers.
     * @return The ListTriggersResponse.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public ListTriggersResponse listTriggers(String filter) {
        return listTriggers(filter, null, null);
    }

    /**
     * Lists Triggers.
     *
     * @return The ListTriggersResponse.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public ListTriggersResponse listTriggers() {
        return listTriggers(null, null, null);
    }

    /**
     * Updates a Trigger.
     *
     * @param id     The trigger ID.
     * @param update The trigger update payload.
     * @return The updated Trigger.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Trigger updateTrigger(String id, TriggerUpdate update) {
        try {
            String requestBody = objectMapper.writeValueAsString(update);
            String url = String.format("%s/%s/triggers/%s", baseUrl, version, id);

            HttpRequest httpRequest = newRequestBuilder(url)
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), Trigger.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Pauses a trigger by updating its status to {@link Trigger.Status#PAUSED}.
     *
     * @param id The trigger ID.
     * @return The updated Trigger.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Trigger pauseTrigger(String id) {
        return updateTrigger(id, TriggerUpdate.builder().status(Trigger.Status.PAUSED).build());
    }

    /**
     * Resumes a trigger by updating its status to {@link Trigger.Status#ACTIVE}.
     *
     * @param id The trigger ID.
     * @return The updated Trigger.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public Trigger resumeTrigger(String id) {
        return updateTrigger(id, TriggerUpdate.builder().status(Trigger.Status.ACTIVE).build());
    }

    /**
     * Deletes a Trigger by ID.
     *
     * @param id The trigger ID.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public void deleteTrigger(String id) {
        try {
            String url = String.format("%s/%s/triggers/%s", baseUrl, version, id);

            HttpRequest httpRequest = newRequestBuilder(url)
                .DELETE()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Manually triggers an execution of a Trigger immediately.
     *
     * @param id The trigger ID.
     * @return The created TriggerExecution.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public TriggerExecution runTrigger(String id) {
        try {
            String url = String.format("%s/%s/triggers/%s/executions", baseUrl, version, id);

            HttpRequest httpRequest = newRequestBuilder(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), TriggerExecution.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Lists Executions of a Trigger.
     *
     * @param triggerId The trigger ID.
     * @param pageSize  The maximum number of executions to return.
     * @param pageToken A page token, received from a previous list call.
     * @return The ListTriggerExecutionsResponse.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public ListTriggerExecutionsResponse listTriggerExecutions(String triggerId, Integer pageSize, String pageToken) {
        try {
            StringBuilder urlBuilder = new StringBuilder(String.format("%s/%s/triggers/%s/executions", baseUrl, version, triggerId));
            boolean hasParam = false;
            if (pageSize != null) {
                urlBuilder.append("?page_size=").append(pageSize);
                hasParam = true;
            }
            if (pageToken != null && !pageToken.isEmpty()) {
                urlBuilder.append(hasParam ? "&" : "?").append("page_token=").append(pageToken);
            }

            HttpRequest httpRequest = newRequestBuilder(urlBuilder.toString())
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            checkError(response);

            return objectMapper.readValue(response.body(), ListTriggerExecutionsResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new GeminiInteractionsException(e);
        }
    }

    /**
     * Lists Executions of a Trigger.
     *
     * @param triggerId The trigger ID.
     * @return The ListTriggerExecutionsResponse.
     * @throws GeminiInteractionsException If the API request fails or an error occurs.
     */
    public ListTriggerExecutionsResponse listTriggerExecutions(String triggerId) {
        return listTriggerExecutions(triggerId, null, null);
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
        private String version = null;
        private String apiKey;
        private String project;
        private String location = "global";
        private GoogleCredentials credentials;
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
         * Sets the Google Cloud Project ID (for Vertex AI).
         *
         * @param project The Project ID.
         * @return This builder.
         */
        public Builder project(String project) {
            this.project = project;
            return this;
        }

        /**
         * Sets the Google Cloud Location (for Vertex AI).
         *
         * @param location The Location (e.g., "global", "europe-west1").
         * @return This builder.
         */
        public Builder location(String location) {
            this.location = location;
            return this;
        }

        /**
         * Sets the Google Cloud Credentials (for Vertex AI).
         *
         * @param credentials The GoogleCredentials.
         * @return This builder.
         */
        public Builder credentials(GoogleCredentials credentials) {
            this.credentials = credentials;
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
            if (apiKey == null && project == null) {
                throw new IllegalStateException("Either API Key or Project must be provided");
            }
            if (project != null && credentials == null) {
                try {
                    this.credentials = GoogleCredentials.getApplicationDefault();
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to load Google Application Default Credentials", e);
                }
            }
            if (version == null) {
                version = project != null ? "v1beta1" : "v1beta";
            }
            return new GeminiInteractionsClient(this);
        }
    }
}
