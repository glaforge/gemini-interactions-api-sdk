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

package io.github.glaforge.gemini.interactions;

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
import io.github.glaforge.gemini.interactions.model.CreateVoiceRequest;
import io.github.glaforge.gemini.interactions.model.DeleteVoiceResponse;
import io.github.glaforge.gemini.interactions.model.ListVoicesResponse;
import io.github.glaforge.gemini.interactions.model.Voice;
import io.github.glaforge.gemini.interactions.model.VoiceListFilter;
import io.github.glaforge.gemini.interactions.model.VoiceType;
import com.fasterxml.jackson.annotation.JsonInclude;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InteractionsClientTest {

    private MockWebServer mockWebServer;
    private GeminiInteractionsClient client;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        client = GeminiInteractionsClient.builder()
            .baseUrl(mockWebServer.url("/").toString().replaceAll("/$", ""))
            .apiKey("test-key")
            .build();

        objectMapper = JsonMapper.builder()
            .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
            .build();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testCreateInteraction() throws Exception {
        Interaction expected = Interaction.builder()
            .id("interaction-123")
            .model("gemini-pro")
            .created(Instant.parse("2025-01-01T00:00:00Z"))
            .updated(Instant.parse("2025-01-01T00:00:00Z"))
            .status(Interaction.Status.COMPLETED)
            .steps(Collections.emptyList())
            .build();

        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(expected))
            .addHeader("Content-Type", "application/json"));

        InteractionParams.Request request = InteractionParams.ModelInteractionParams.builder()
            .model("gemini-pro")
            .input("Hello")
            .build();

        Interaction interaction = client.create(request);
        assertNotNull(interaction);
        assertEquals("interaction-123", interaction.id());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/v1beta/interactions", recordedRequest.getPath());
        assertEquals("POST", recordedRequest.getMethod());
    }

    @Test
    void testGetInteraction() throws Exception {
        Interaction expected = Interaction.builder()
            .id("foo-bar")
            .model("gemini-pro")
            .created(Instant.parse("2025-01-01T00:00:00Z"))
            .updated(Instant.parse("2025-01-01T00:00:00Z"))
            .status(Interaction.Status.COMPLETED)
            .steps(Collections.emptyList())
            .build();

        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(expected))
            .addHeader("Content-Type", "application/json"));

        Interaction interaction = client.get("foo-bar");
        assertEquals("foo-bar", interaction.id());
        assertEquals(Interaction.Status.COMPLETED, interaction.status());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/v1beta/interactions/foo-bar", recordedRequest.getPath());
        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    void testCancelInteraction() throws Exception {
        Interaction expected = Interaction.builder()
            .id("foo-bar")
            .model("gemini-pro")
            .created(Instant.parse("2025-01-01T00:00:00Z"))
            .updated(Instant.parse("2025-01-01T00:00:00Z"))
            .status(Interaction.Status.CANCELLED)
            .steps(Collections.emptyList())
            .build();

        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(expected))
            .addHeader("Content-Type", "application/json"));

        Interaction interaction = client.cancel("foo-bar");
        assertEquals("foo-bar", interaction.id());
        assertEquals(Interaction.Status.CANCELLED, interaction.status());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/v1beta/interactions/foo-bar/cancel", recordedRequest.getPath());
        assertEquals("POST", recordedRequest.getMethod());
    }

    @Test
    void testStreamInteraction() throws Exception {
        Events.InteractionCreated ev1 = new Events.InteractionCreated(
            Events.EventType.INTERACTION_CREATED,
            "evt-1",
            Interaction.builder()
                .id("interaction-123")
                .model("gemini-pro")
                .created(Instant.parse("2025-01-01T00:00:00Z"))
                .updated(Instant.parse("2025-01-01T00:00:00Z"))
                .status(Interaction.Status.IN_PROGRESS)
                .steps(Collections.emptyList())
                .build(),
            null
        );

        Events.StepDelta ev2 = new Events.StepDelta(
            Events.EventType.STEP_DELTA,
            "evt-2",
            0,
            new Events.TextDelta(Events.DeltaType.TEXT, "Hello world", null),
            null
        );

        String sseBody = "data: " + objectMapper.writeValueAsString(ev1) + "\n\n" +
                         "data: " + objectMapper.writeValueAsString(ev2) + "\n\n" +
                         "data: [DONE]\n\n";

        mockWebServer.enqueue(new MockResponse()
            .setBody(sseBody)
            .addHeader("Content-Type", "text/event-stream"));

        InteractionParams.Request request = InteractionParams.ModelInteractionParams.builder()
            .model("gemini-pro")
            .input("Hello")
            .build();

        Stream<Events> eventsStream = client.stream(request);
        assertNotNull(eventsStream);
        long count = eventsStream.count();
        assertEquals(2, count);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/v1beta/interactions?alt=sse", recordedRequest.getPath());
        assertEquals("POST", recordedRequest.getMethod());
    }

    @Test
    void testCreateWebhook() throws Exception {
        Webhook expected = new Webhook("wh-1", "Test Hook", "http://test", Collections.emptyList(), Webhook.State.ENABLED, "2025-01-01T00:00:00Z", "2025-01-01T00:00:00Z", Collections.emptyList(), "secret-123");

        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(expected))
            .addHeader("Content-Type", "application/json"));

        Webhook webhook = new Webhook(null, "Test Hook", "http://test", Collections.emptyList(), null, null, null, null, null);
        Webhook result = client.createWebhook(webhook);
        assertEquals("wh-1", result.id());
        assertEquals("secret-123", result.newSigningSecret());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/v1beta/webhooks", recordedRequest.getPath());
        assertEquals("POST", recordedRequest.getMethod());
    }

    @Test
    void testGetWebhook() throws Exception {
        Webhook expected = new Webhook("wh-1", "test", "http://test", Collections.emptyList(), Webhook.State.ENABLED, "2025-01-01T00:00:00Z", "2025-01-01T00:00:00Z", Collections.emptyList(), null);

        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(expected))
            .addHeader("Content-Type", "application/json"));

        Webhook result = client.getWebhook("wh-1");
        assertEquals("wh-1", result.id());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/v1beta/webhooks/wh-1", recordedRequest.getPath());
        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    void testListWebhooks() throws Exception {
        Webhook webhook = new Webhook("wh-1", "test", "http://test", Collections.emptyList(), Webhook.State.ENABLED, "2025-01-01T00:00:00Z", "2025-01-01T00:00:00Z", Collections.emptyList(), null);
        ListWebhooksResponse expected = new ListWebhooksResponse(Collections.singletonList(webhook), "next-token");

        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(expected))
            .addHeader("Content-Type", "application/json"));

        ListWebhooksResponse result = client.listWebhooks(10, null);
        assertEquals(1, result.webhooks().size());
        assertEquals("wh-1", result.webhooks().get(0).id());
        assertEquals("next-token", result.nextPageToken());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/v1beta/webhooks?page_size=10", recordedRequest.getPath());
        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    void testUpdateWebhook() throws Exception {
        Webhook expected = new Webhook("wh-1", "Updated Name", "http://test", Collections.emptyList(), Webhook.State.ENABLED, "2025-01-01T00:00:00Z", "2025-01-01T00:00:00Z", Collections.emptyList(), null);

        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(expected))
            .addHeader("Content-Type", "application/json"));

        WebhookUpdate update = new WebhookUpdate("Updated Name", null, null, null);
        Webhook result = client.updateWebhook("wh-1", update);
        assertEquals("wh-1", result.id());
        assertEquals("Updated Name", result.name());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/v1beta/webhooks/wh-1", recordedRequest.getPath());
        assertEquals("PATCH", recordedRequest.getMethod());
    }

    @Test
    void testDeleteWebhook() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));

        client.deleteWebhook("wh-1");

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/v1beta/webhooks/wh-1", recordedRequest.getPath());
        assertEquals("DELETE", recordedRequest.getMethod());
    }

    @Test
    void testPingWebhook() throws Exception {
        PingWebhookResponse expected = new PingWebhookResponse();

        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(expected))
            .addHeader("Content-Type", "application/json"));

        PingWebhookResponse result = client.pingWebhook("wh-1");
        assertNotNull(result);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/v1beta/webhooks/wh-1:ping", recordedRequest.getPath());
        assertEquals("POST", recordedRequest.getMethod());
    }

    @Test
    void testRotateSigningSecret() throws Exception {
        RotateSigningSecretResponse expected = new RotateSigningSecretResponse("new-secret-123");

        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(expected))
            .addHeader("Content-Type", "application/json"));

        RotateSigningSecretResponse result = client.rotateSigningSecret("wh-1", new RotateSigningSecretRequest(RotateSigningSecretRequest.RevocationBehavior.REVOKE_PREVIOUS_SECRETS_AFTER_H24));
        assertEquals("new-secret-123", result.secret());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/v1beta/webhooks/wh-1:rotateSigningSecret", recordedRequest.getPath());
        assertEquals("POST", recordedRequest.getMethod());
    }

    @Test
    void testCreateAgent() throws Exception {
        Agent expected = Agent.builder()
            .id("test-agent")
            .description("A test agent")
            .baseAgent("antigravity-preview-05-2026")
            .systemInstruction("Be helpful")
            .build();

        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(expected))
            .addHeader("Content-Type", "application/json"));

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

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/v1beta/agents", recordedRequest.getPath());
        assertEquals("POST", recordedRequest.getMethod());
    }

    @Test
    void testGetAgent() throws Exception {
        Agent expected = Agent.builder()
            .id("test-agent")
            .description("Test Description")
            .baseAgent("antigravity-preview-05-2026")
            .build();

        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(expected))
            .addHeader("Content-Type", "application/json"));

        Agent result = client.getAgent("test-agent");
        assertEquals("test-agent", result.id());
        assertEquals("Test Description", result.description());
        assertEquals("antigravity-preview-05-2026", result.baseAgent());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/v1beta/agents/test-agent", recordedRequest.getPath());
        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    void testListAgents() throws Exception {
        Agent agent = Agent.builder()
            .id("agent-123")
            .description("Test Description")
            .baseAgent("antigravity-preview-05-2026")
            .build();
        ListAgentsResponse expected = new ListAgentsResponse(
            Collections.singletonList(agent),
            "next-token-123"
        );

        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(expected))
            .addHeader("Content-Type", "application/json"));

        ListAgentsResponse result = client.listAgents(10, "start-token");
        assertEquals(1, result.agents().size());
        assertEquals("agent-123", result.agents().get(0).id());
        assertEquals("next-token-123", result.nextPageToken());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/v1beta/agents?page_size=10&page_token=start-token", recordedRequest.getPath());
        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    void testDeleteAgent() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));

        client.deleteAgent("test-agent");

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/v1beta/agents/test-agent", recordedRequest.getPath());
        assertEquals("DELETE", recordedRequest.getMethod());
    }

    @Test
    void testCreateVoice() throws Exception {
        Voice created = Voice.builder()
            .id("voice-123")
            .name("voices/voice-123")
            .type(VoiceType.PROMPTED)
            .displayName("Custom Voice")
            .build();

        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(created))
            .addHeader("Content-Type", "application/json"));

        Voice result = client.createVoice(Voice.prompted("Gentle British narrator"));
        assertNotNull(result);
        assertEquals("voice-123", result.id());
        assertEquals("Custom Voice", result.displayName());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/v1beta/voices", recordedRequest.getPath());
        assertEquals("POST", recordedRequest.getMethod());
    }

    @Test
    void testGetVoice() throws Exception {
        Voice voice = Voice.builder()
            .id("voice-123")
            .name("voices/voice-123")
            .type(VoiceType.PROMPTED)
            .displayName("Custom Voice")
            .build();

        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(voice))
            .addHeader("Content-Type", "application/json"));

        Voice result = client.getVoice("voices/voice-123");
        assertNotNull(result);
        assertEquals("voice-123", result.id());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/v1beta/voices/voice-123", recordedRequest.getPath());
        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    void testListVoices() throws Exception {
        Voice voice = Voice.builder()
            .id("voice-123")
            .type(VoiceType.PROMPTED)
            .displayName("Custom Voice")
            .build();
        ListVoicesResponse expected = new ListVoicesResponse(
            Collections.singletonList(voice),
            "next-page"
        );

        mockWebServer.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(expected))
            .addHeader("Content-Type", "application/json"));

        VoiceListFilter filter = VoiceListFilter.builder()
            .types(VoiceType.PROMPTED)
            .languageCodes("en-US")
            .build();

        ListVoicesResponse result = client.listVoices(filter);
        assertEquals(1, result.voices().size());
        assertEquals("voice-123", result.voices().get(0).id());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/v1beta/voices?type=prompted&language_code=en-US", recordedRequest.getPath());
        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    void testDeleteVoice() throws Exception {
        mockWebServer.enqueue(new MockResponse()
            .setBody("{}")
            .addHeader("Content-Type", "application/json"));

        DeleteVoiceResponse response = client.deleteVoice("voice-123");
        assertNotNull(response);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/v1beta/voices/voice-123", recordedRequest.getPath());
        assertEquals("DELETE", recordedRequest.getMethod());
    }
}
