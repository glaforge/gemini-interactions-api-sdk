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
import io.github.glaforge.gemini.interactions.model.Agent;
import io.github.glaforge.gemini.interactions.model.Events;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams;
import io.github.glaforge.gemini.interactions.model.ListAgentsResponse;
import io.github.glaforge.gemini.interactions.model.ListWebhooksResponse;
import io.github.glaforge.gemini.interactions.model.PingWebhookResponse;
import io.github.glaforge.gemini.interactions.model.RotateSigningSecretRequest;
import io.github.glaforge.gemini.interactions.model.RotateSigningSecretResponse;
import io.github.glaforge.gemini.interactions.model.Webhook;
import io.github.glaforge.gemini.interactions.model.WebhookUpdate;
import java.util.stream.Stream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.DeserializationFeature;
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
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build();

    // /v1beta/interactions/{id}
    private static final Pattern INTERACTION_ID_PATTERN = Pattern.compile(".*/interactions/([^/]+)$");
    // /v1beta/interactions/{id}/cancel
    private static final Pattern CANCEL_PATTERN = Pattern.compile(".*/interactions/([^/]+)/cancel$");
    // /v1beta/webhooks/{id}
    private static final Pattern WEBHOOK_ID_PATTERN = Pattern.compile(".*/webhooks/([^/:]+)$");
    // /v1beta/webhooks/{id}:ping
    private static final Pattern PING_WEBHOOK_PATTERN = Pattern.compile(".*/webhooks/([^/]+):ping$");
    // /v1beta/webhooks/{id}:rotateSigningSecret
    private static final Pattern ROTATE_SECRET_PATTERN = Pattern.compile(".*/webhooks/([^/]+):rotateSigningSecret$");
    // /v1beta/agents/{id}
    private static final Pattern AGENT_ID_PATTERN = Pattern.compile(".*/agents/([^/]+)$");

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

                // Webhooks routing
                if (path.endsWith("/webhooks")) {
                    if (method.equalsIgnoreCase("POST")) {
                        handleCreateWebhook(exchange);
                    } else if (method.equalsIgnoreCase("GET")) {
                        handleListWebhooks(exchange);
                    } else {
                        sendResponse(exchange, 405, "Method Not Allowed");
                    }
                    return;
                }

                Matcher webhookIdMatcher = WEBHOOK_ID_PATTERN.matcher(path);
                if (webhookIdMatcher.matches()) {
                    String id = webhookIdMatcher.group(1);
                    if (method.equalsIgnoreCase("GET")) {
                        handleGetWebhook(exchange, id);
                    } else if (method.equalsIgnoreCase("PATCH")) {
                        handleUpdateWebhook(exchange, id);
                    } else if (method.equalsIgnoreCase("DELETE")) {
                        handleDeleteWebhook(exchange, id);
                    } else {
                        sendResponse(exchange, 405, "Method Not Allowed");
                    }
                    return;
                }

                Matcher pingMatcher = PING_WEBHOOK_PATTERN.matcher(path);
                if (pingMatcher.matches() && method.equalsIgnoreCase("POST")) {
                    handlePingWebhook(exchange, pingMatcher.group(1));
                    return;
                }

                Matcher rotateMatcher = ROTATE_SECRET_PATTERN.matcher(path);
                if (rotateMatcher.matches() && method.equalsIgnoreCase("POST")) {
                    handleRotateSigningSecret(exchange, rotateMatcher.group(1));
                    return;
                }

                // Agents routing
                if (path.endsWith("/agents")) {
                    if (method.equalsIgnoreCase("POST")) {
                        handleCreateAgent(exchange);
                    } else if (method.equalsIgnoreCase("GET")) {
                        handleListAgents(exchange);
                    } else {
                        sendResponse(exchange, 405, "Method Not Allowed");
                    }
                    return;
                }

                Matcher agentIdMatcher = AGENT_ID_PATTERN.matcher(path);
                if (agentIdMatcher.matches()) {
                    String id = agentIdMatcher.group(1);
                    if (method.equalsIgnoreCase("GET")) {
                        handleGetAgent(exchange, id);
                    } else if (method.equalsIgnoreCase("DELETE")) {
                        handleDeleteAgent(exchange, id);
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
            boolean includeInput = false;
            String query = exchange.getRequestURI().getQuery();
            if (query != null && query.contains("include_input=true")) {
                includeInput = true;
            }
            Interaction interaction = get(id, includeInput);
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

    private void handleCreateWebhook(HttpExchange exchange) throws IOException {
        try {
            Webhook webhook = objectMapper.readValue(exchange.getRequestBody(), Webhook.class);
            Webhook created = createWebhook(webhook);
            sendResponse(exchange, 200, created);
        } catch (Exception e) {
            sendResponse(exchange, 400, "Invalid Request: " + e.getMessage());
        }
    }

    private void handleListWebhooks(HttpExchange exchange) throws IOException {
        try {
            String query = exchange.getRequestURI().getQuery();
            Integer pageSize = null;
            String pageToken = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length > 1) {
                        if (pair[0].equals("page_size")) {
                            pageSize = Integer.parseInt(pair[1]);
                        } else if (pair[0].equals("page_token")) {
                            pageToken = pair[1];
                        }
                    }
                }
            }
            ListWebhooksResponse response = listWebhooks(pageSize, pageToken);
            sendResponse(exchange, 200, response);
        } catch (Exception e) {
            sendResponse(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleGetWebhook(HttpExchange exchange, String id) throws IOException {
        try {
            Webhook webhook = getWebhook(id);
            if (webhook != null) {
                sendResponse(exchange, 200, webhook);
            } else {
                sendResponse(exchange, 404, "Webhook not found");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleUpdateWebhook(HttpExchange exchange, String id) throws IOException {
        try {
            WebhookUpdate update = objectMapper.readValue(exchange.getRequestBody(), WebhookUpdate.class);
            Webhook updated = updateWebhook(id, update);
            sendResponse(exchange, 200, updated);
        } catch (Exception e) {
            sendResponse(exchange, 400, "Invalid Request: " + e.getMessage());
        }
    }

    private void handleDeleteWebhook(HttpExchange exchange, String id) throws IOException {
        try {
            deleteWebhook(id);
            exchange.sendResponseHeaders(204, -1);
        } catch (Exception e) {
            sendResponse(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handlePingWebhook(HttpExchange exchange, String id) throws IOException {
        try {
            PingWebhookResponse response = pingWebhook(id);
            sendResponse(exchange, 200, response);
        } catch (Exception e) {
            sendResponse(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleRotateSigningSecret(HttpExchange exchange, String id) throws IOException {
        try {
            RotateSigningSecretRequest request = objectMapper.readValue(exchange.getRequestBody(), RotateSigningSecretRequest.class);
            RotateSigningSecretResponse response = rotateSigningSecret(id, request);
            sendResponse(exchange, 200, response);
        } catch (Exception e) {
            sendResponse(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleCreateAgent(HttpExchange exchange) throws IOException {
        try {
            Agent agent = objectMapper.readValue(exchange.getRequestBody(), Agent.class);
            Agent created = createAgent(agent);
            sendResponse(exchange, 200, created);
        } catch (Exception e) {
            sendResponse(exchange, 400, "Invalid Request: " + e.getMessage());
        }
    }

    private void handleListAgents(HttpExchange exchange) throws IOException {
        try {
            String query = exchange.getRequestURI().getQuery();
            Integer pageSize = null;
            String pageToken = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length > 1) {
                        if (pair[0].equals("page_size")) {
                            pageSize = Integer.parseInt(pair[1]);
                        } else if (pair[0].equals("page_token")) {
                            pageToken = pair[1];
                        }
                    }
                }
            }
            ListAgentsResponse response = listAgents(pageSize, pageToken);
            sendResponse(exchange, 200, response);
        } catch (Exception e) {
            sendResponse(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleGetAgent(HttpExchange exchange, String id) throws IOException {
        try {
            Agent agent = getAgent(id);
            if (agent != null) {
                sendResponse(exchange, 200, agent);
            } else {
                sendResponse(exchange, 404, "Agent not found");
            }
        } catch (Exception e) {
            sendResponse(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleDeleteAgent(HttpExchange exchange, String id) throws IOException {
        try {
            deleteAgent(id);
            exchange.sendResponseHeaders(204, -1);
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
     * Retrieves an interaction by ID, optionally including the original input.
     * @param id The interaction ID.
     * @param includeInput Whether to include the input in the response.
     * @return The Interaction, or null if not found.
     */
    public Interaction get(String id, boolean includeInput) {
        return get(id);
    }

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

    // --- Webhook abstract methods ---

    /**
     * Creates a new webhook.
     * @param webhook The webhook to create.
     * @return The created Webhook.
     */
    public Webhook createWebhook(Webhook webhook) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Retrieves a webhook by ID.
     * @param id The webhook ID.
     * @return The Webhook, or null if not found.
     */
    public Webhook getWebhook(String id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Lists webhooks.
     * @param pageSize  The maximum number of webhooks to return.
     * @param pageToken A page token.
     * @return The ListWebhooksResponse.
     */
    public ListWebhooksResponse listWebhooks(Integer pageSize, String pageToken) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Updates a webhook.
     * @param id     The webhook ID.
     * @param update The webhook update payload.
     * @return The updated Webhook.
     */
    public Webhook updateWebhook(String id, WebhookUpdate update) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Deletes a webhook by ID.
     * @param id The webhook ID.
     */
    public void deleteWebhook(String id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Pings a webhook to verify it is working.
     * @param id The webhook ID.
     * @return The PingWebhookResponse.
     */
    public PingWebhookResponse pingWebhook(String id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Rotates the signing secret for a webhook.
     * @param id      The webhook ID.
     * @param request The rotation request payload.
     * @return The RotateSigningSecretResponse.
     */
    public RotateSigningSecretResponse rotateSigningSecret(String id, RotateSigningSecretRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    // --- Agent methods ---

    /**
     * Creates a new custom Agent.
     * @param agent The agent to create.
     * @return The created Agent.
     */
    public Agent createAgent(Agent agent) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Retrieves a custom Agent by ID.
     * @param id The agent ID.
     * @return The Agent, or null if not found.
     */
    public Agent getAgent(String id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Lists custom Agents.
     * @param pageSize  The maximum number of agents to return.
     * @param pageToken A page token.
     * @return The ListAgentsResponse.
     */
    public ListAgentsResponse listAgents(Integer pageSize, String pageToken) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Deletes a custom Agent by ID.
     * @param id The agent ID.
     */
    public void deleteAgent(String id) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
