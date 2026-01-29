/*
 * Copyright 2026 Google LLC
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

package io.github.glaforge.gemini.interactions.server;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams;
import io.github.glaforge.gemini.interactions.model.Events;
import java.util.stream.Stream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for handling Gemini Interactions API requests in an {@link HttpHandler}.
 * <p>
 * Subclasses must implement the abstract methods to provide the actual logic for
 * creating, retrieving, deleting, and cancelling interactions, as well as streaming events.
 */
public abstract class InteractionsHandler implements HttpHandler {
    /** Default constructor for InteractionsHandler. */
    protected InteractionsHandler() {}

    private static final ObjectMapper objectMapper = JsonMapper.builder()
        .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
        .build();

    // /v1beta/interactions/{id}
    private static final Pattern INTERACTION_ID_PATTERN = Pattern.compile(".*/interactions/([^/]+)$");
    // /v1beta/interactions/{id}/cancel
    private static final Pattern CANCEL_PATTERN = Pattern.compile(".*/interactions/([^/]+)/cancel$");

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if (path.endsWith("/interactions") && method.equalsIgnoreCase("POST")) {
                // Check for streaming
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.contains("alt=sse")) {
                    handleStream(exchange);
                } else {
                    handleCreate(exchange);
                }
            } else {
                Matcher cancelMatcher = CANCEL_PATTERN.matcher(path);
                if (cancelMatcher.matches() && method.equalsIgnoreCase("POST")) {
                    handleCancel(exchange, cancelMatcher.group(1));
                    return;
                }

                Matcher idMatcher = INTERACTION_ID_PATTERN.matcher(path);
                if (idMatcher.matches()) {
                    String id = idMatcher.group(1);
                    if (method.equalsIgnoreCase("GET")) {
                        handleGet(exchange, id);
                    } else if (method.equalsIgnoreCase("DELETE")) {
                        handleDelete(exchange, id);
                    } else {
                        sendResponse(exchange, 405, "Method Not Allowed");
                    }
                    return;
                }

                sendResponse(exchange, 404, "Not Found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        try {
            JsonNode node = objectMapper.readTree(exchange.getRequestBody());
            InteractionParams.Request request;
            if (node.has("agent")) {
                request = objectMapper.treeToValue(node, InteractionParams.AgentInteractionParams.class);
            } else {
                request = objectMapper.treeToValue(node, InteractionParams.ModelInteractionParams.class);
            }

            Interaction interaction = create(request);
            sendResponse(exchange, 200, interaction);
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 400, "Invalid Request: " + e.getMessage());
        }
    }

    private void handleStream(HttpExchange exchange) throws IOException {
        try {
            JsonNode node = objectMapper.readTree(exchange.getRequestBody());
            InteractionParams.Request request;
            if (node.has("agent")) {
                request = objectMapper.treeToValue(node, InteractionParams.AgentInteractionParams.class);
            } else {
                request = objectMapper.treeToValue(node, InteractionParams.ModelInteractionParams.class);
            }

            Stream<Events> events = stream(request);

            exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
            exchange.getResponseHeaders().set("Cache-Control", "no-cache");
            exchange.sendResponseHeaders(200, 0);

            try (OutputStream os = exchange.getResponseBody()) {
                events.forEach(event -> {
                    try {
                        String json = objectMapper.writeValueAsString(event);
                        os.write(("data: " + json + "\n\n").getBytes(StandardCharsets.UTF_8));
                        os.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                os.write("data: [DONE]\n\n".getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleGet(HttpExchange exchange, String id) throws IOException {
        try {
            Interaction interaction = get(id);
            if (interaction != null) {
                sendResponse(exchange, 200, interaction);
            } else {
                sendResponse(exchange, 404, "Interaction not found");
            }
        } catch (Exception e) {
             sendResponse(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange, String id) throws IOException {
        try {
            delete(id);
            // No content
            exchange.sendResponseHeaders(204, -1);
        } catch (Exception e) {
            sendResponse(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleCancel(HttpExchange exchange, String id) throws IOException {
        try {
            Interaction interaction = cancel(id);
            sendResponse(exchange, 200, interaction);
        } catch (Exception e) {
            sendResponse(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, Object body) throws IOException {
        String jsonResponse = "";
        if (body instanceof String) {
            jsonResponse = (String) body;
        } else {
            jsonResponse = objectMapper.writeValueAsString(body);
        }

        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    // --- Abstract methods to be implemented by the user ---

    /**
     * Creates a new interaction.
     * @param request The interaction request parameters.
     * @return The created Interaction.
     */
    public abstract Interaction create(InteractionParams.Request request);

    /**
     * Retrieves an interaction by ID.
     * @param id The interaction ID.
     * @return The Interaction, or null if not found.
     */
    public abstract Interaction get(String id);

    /**
     * Deletes an interaction by ID.
     * @param id The interaction ID.
     */
    public abstract void delete(String id);

    /**
     * Cancels an interaction by ID.
     * @param id The interaction ID.
     * @return The updated Interaction.
     */
    public abstract Interaction cancel(String id);

    /**
     * Returns a stream of events for an interaction request.
     * @param request The interaction request parameters.
     * @return A stream of Events.
     */
    public abstract Stream<Events> stream(InteractionParams.Request request);
}
