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

import com.sun.net.httpserver.HttpServer;
import io.github.glaforge.gemini.interactions.GeminiInteractionsClient;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.time.Instant;
import java.util.stream.Stream;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InteractionsServerTest {

    private HttpServer server;
    private int port;
    private GeminiInteractionsClient client;
    private ExecutorService executor;

    @BeforeEach
    void setUp() throws IOException {
        executor = Executors.newSingleThreadExecutor();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.setExecutor(executor);
        port = server.getAddress().getPort();

        // v1beta handling
        server.createContext("/v1beta", new InteractionsHandler() {
            @Override
            public Interaction create(InteractionParams.Request request) {
                return new Interaction(
                    "interaction-123",
                    "gemini-pro",
                    null,
                    "interaction",
                    Instant.parse("2025-01-01T00:00:00Z"),
                    Instant.parse("2025-01-01T00:00:00Z"),
                    Interaction.Role.MODEL,
                    Interaction.Status.COMPLETED,
                    Collections.emptyList(),
                    null,
                    null
                );
            }

            @Override
            public Interaction get(String id) {
                 return new Interaction(
                    id,
                    "gemini-pro",
                    null,
                    "interaction",
                    Instant.parse("2025-01-01T00:00:00Z"),
                    Instant.parse("2025-01-01T00:00:00Z"),
                    Interaction.Role.MODEL,
                    Interaction.Status.COMPLETED,
                    Collections.emptyList(),
                    null,
                    null
                );
            }

            @Override
            public void delete(String id) {
                // no-op
            }

            @Override
            public Interaction cancel(String id) {
                 return new Interaction(
                    id,
                    "gemini-pro",
                    null,
                    "interaction",
                    Instant.parse("2025-01-01T00:00:00Z"),
                    Instant.parse("2025-01-01T00:00:00Z"),
                    Interaction.Role.MODEL,
                    Interaction.Status.CANCELLED,
                    Collections.emptyList(),
                    null,
                    null
                );
            }


            @Override
            public Stream<Events> stream(InteractionParams.Request request) {
                return Stream.of(
                    new Events.InteractionCreated(
                        Events.EventType.INTERACTION_CREATED,
                        "evt-1",
                        new Interaction("interaction-123", "gemini-pro", null, "interaction", Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-01-01T00:00:00Z"), Interaction.Role.MODEL, Interaction.Status.IN_PROGRESS, Collections.emptyList(), null, null)
                    ),
                    new Events.StepDelta(
                        Events.EventType.STEP_DELTA,
                        "evt-2",
                        0,
                        new Events.TextDelta(Events.DeltaType.TEXT, "Hello world", null)
                    )
                );
            }

            @Override
            public Webhook createWebhook(Webhook webhook) {
                return new Webhook("wh-1", webhook.name(), webhook.uri(), webhook.subscribedEvents(), Webhook.State.ENABLED, "2025-01-01T00:00:00Z", "2025-01-01T00:00:00Z", Collections.emptyList(), "secret-123");
            }

            @Override
            public Webhook getWebhook(String id) {
                return new Webhook(id, "test", "http://test", Collections.emptyList(), Webhook.State.ENABLED, "2025-01-01T00:00:00Z", "2025-01-01T00:00:00Z", Collections.emptyList(), null);
            }

            @Override
            public ListWebhooksResponse listWebhooks(Integer pageSize, String pageToken) {
                return new ListWebhooksResponse(Collections.singletonList(getWebhook("wh-1")), "next-token");
            }

            @Override
            public Webhook updateWebhook(String id, WebhookUpdate update) {
                return new Webhook(id, update.name(), update.uri(), update.subscribedEvents(), update.state(), "2025-01-01T00:00:00Z", "2025-01-01T00:00:00Z", Collections.emptyList(), null);
            }

            @Override
            public void deleteWebhook(String id) {
                // no-op
            }

            @Override
            public PingWebhookResponse pingWebhook(String id) {
                return new PingWebhookResponse();
            }

            @Override
            public RotateSigningSecretResponse rotateSigningSecret(String id, RotateSigningSecretRequest request) {
                return new RotateSigningSecretResponse("new-secret-123");
            }

            @Override
            public Agent createAgent(Agent agent) {
                return Agent.builder()
                    .id(agent.id())
                    .description(agent.description())
                    .baseAgent(agent.baseAgent())
                    .systemInstruction(agent.systemInstruction())
                    .build();
            }

            @Override
            public Agent getAgent(String id) {
                return Agent.builder()
                    .id(id)
                    .description("Test Description")
                    .baseAgent("antigravity-preview-05-2026")
                    .build();
            }

            @Override
            public ListAgentsResponse listAgents(Integer pageSize, String pageToken) {
                return new ListAgentsResponse(
                    Collections.singletonList(getAgent("agent-123")),
                    "next-token-123"
                );
            }

            @Override
            public void deleteAgent(String id) {
                // no-op
            }
        });
        server.start();

        client = GeminiInteractionsClient.builder()
            .baseUrl("http://localhost:" + port)
            .apiKey("test-key")
            .build();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
        executor.shutdownNow();
    }

    @Test
    void testCreateInteraction() throws IOException, InterruptedException {
        InteractionParams.Request request = InteractionParams.ModelInteractionParams.builder()
            .model("gemini-pro")
            .input("Hello")
            .build();

        Interaction interaction = client.create(request);
        assertNotNull(interaction);
        assertEquals("interaction-123", interaction.id());
    }

    @Test
    void testGetInteraction() throws IOException, InterruptedException {
        Interaction interaction = client.get("foo-bar");
        assertEquals("foo-bar", interaction.id());
        assertEquals(Interaction.Status.COMPLETED, interaction.status());
    }

    @Test
    void testCancelInteraction() throws IOException, InterruptedException {
        Interaction interaction = client.cancel("foo-bar");
        assertEquals("foo-bar", interaction.id());
        assertEquals(Interaction.Status.CANCELLED, interaction.status());
    }

    @Test
    void testStreamInteraction() throws IOException, InterruptedException {
        InteractionParams.Request request = InteractionParams.ModelInteractionParams.builder()
            .model("gemini-pro")
            .input("Hello")
            .build();

        Stream<Events> eventsStream = client.stream(request);
        assertNotNull(eventsStream);
        long count = eventsStream.count();
        assertEquals(2, count);
    }

    @Test
    void testCreateWebhook() {
        Webhook webhook = new Webhook(null, "Test Hook", "http://test", Collections.emptyList(), null, null, null, null, null);
        Webhook result = client.createWebhook(webhook);
        assertEquals("wh-1", result.id());
        assertEquals("secret-123", result.newSigningSecret());
    }

    @Test
    void testGetWebhook() {
        Webhook result = client.getWebhook("wh-1");
        assertEquals("wh-1", result.id());
    }

    @Test
    void testListWebhooks() {
        ListWebhooksResponse result = client.listWebhooks(10, null);
        assertEquals(1, result.webhooks().size());
        assertEquals("wh-1", result.webhooks().get(0).id());
        assertEquals("next-token", result.nextPageToken());
    }

    @Test
    void testUpdateWebhook() {
        WebhookUpdate update = new WebhookUpdate("Updated Name", null, null, null);
        Webhook result = client.updateWebhook("wh-1", update);
        assertEquals("wh-1", result.id());
        assertEquals("Updated Name", result.name());
    }

    @Test
    void testDeleteWebhook() {
        client.deleteWebhook("wh-1"); // Should not throw
    }

    @Test
    void testPingWebhook() {
        PingWebhookResponse result = client.pingWebhook("wh-1");
        assertNotNull(result);
    }

    @Test
    void testRotateSigningSecret() {
        RotateSigningSecretResponse result = client.rotateSigningSecret("wh-1", new RotateSigningSecretRequest(RotateSigningSecretRequest.RevocationBehavior.REVOKE_PREVIOUS_SECRETS_AFTER_H24));
        assertEquals("new-secret-123", result.secret());
    }

    @Test
    void testCreateAgent() {
        Agent agent = Agent.builder()
            .id("test-agent")
            .description("A test agent")
            .baseAgent("antigravity-preview-05-2026")
            .systemInstruction("Be helpful")
            .build();
        Agent result = client.createAgent(agent);
        assertEquals("test-agent", result.id());
        assertEquals("A test agent", result.description());
        assertEquals("antigravity-preview-05-2026", result.baseAgent());
        assertEquals("Be helpful", result.systemInstruction());
    }

    @Test
    void testGetAgent() {
        Agent result = client.getAgent("test-agent");
        assertEquals("test-agent", result.id());
        assertEquals("Test Description", result.description());
        assertEquals("antigravity-preview-05-2026", result.baseAgent());
    }

    @Test
    void testListAgents() {
        ListAgentsResponse result = client.listAgents(10, "start-token");
        assertEquals(1, result.agents().size());
        assertEquals("agent-123", result.agents().get(0).id());
        assertEquals("next-token-123", result.nextPageToken());
    }

    @Test
    void testDeleteAgent() {
        client.deleteAgent("test-agent"); // Should not throw
    }
}
