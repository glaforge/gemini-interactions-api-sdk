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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * specific implementation of {@link HttpHandler} to handle Interactions API requests.
 */
public abstract class InteractionsHandler implements HttpHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new Jdk8Module())
        .registerModule(new JavaTimeModule())
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);

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
                handleCreate(exchange);
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
            com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(exchange.getRequestBody());
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

    public abstract Interaction create(InteractionParams.Request request);

    public abstract Interaction get(String id);

    public abstract void delete(String id);

    public abstract Interaction cancel(String id);
}
