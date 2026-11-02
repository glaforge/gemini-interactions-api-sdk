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
import io.github.glaforge.gemini.interactions.model.Config;
import io.github.glaforge.gemini.interactions.model.EnvironmentConfig;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams;
import io.github.glaforge.gemini.interactions.model.ListTriggerExecutionsResponse;
import io.github.glaforge.gemini.interactions.model.PingWebhookRequest;
import io.github.glaforge.gemini.interactions.model.PingWebhookResponse;
import io.github.glaforge.gemini.interactions.model.PromptedVoice;
import io.github.glaforge.gemini.interactions.model.RotateSigningSecretRequest;
import io.github.glaforge.gemini.interactions.model.RotateSigningSecretResponse;
import io.github.glaforge.gemini.interactions.model.SpeechConfiguration;
import io.github.glaforge.gemini.interactions.model.Step;
import io.github.glaforge.gemini.interactions.model.Voice;
import io.github.glaforge.gemini.interactions.model.Webhook;
import io.github.glaforge.gemini.interactions.model.WebhookConfig;
import java.util.List;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for updates introduced in the November 2, 2026 OpenAPI specification sync.
 */
class OpenApiUpdateNov02Test {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder().build();
    }

    @Test
    void testInteractionRequiresContinuationStatusAndNewProperties() throws Exception {
        String json = """
            {
                "id": "inter-cont-1",
                "model": "gemini-3-flash-preview",
                "status": "requires_continuation",
                "continuation_token": "tok_resume_xyz",
                "max_total_tokens": "1000000",
                "webhook_config": {
                    "uris": ["https://example.com/hook"],
                    "user_metadata": {"job": "batch-42"}
                },
                "labels": {
                    "env": "prod",
                    "team": "research"
                },
                "usage": {
                    "total_input_tokens": 500,
                    "initial_input_tokens": 120,
                    "total_output_tokens": 800,
                    "total_tokens": 1300
                }
            }
            """;

        Interaction interaction = mapper.readValue(json, Interaction.class);
        assertEquals("inter-cont-1", interaction.id());
        assertEquals(Interaction.Status.REQUIRES_CONTINUATION, interaction.status());
        assertTrue(interaction.status().requiresContinuation());
        assertFalse(interaction.status().isFinished());
        assertEquals("tok_resume_xyz", interaction.continuationToken());
        assertEquals("1000000", interaction.maxTotalTokens());

        assertNotNull(interaction.webhookConfig());
        assertEquals(List.of("https://example.com/hook"), interaction.webhookConfig().uris());
        assertEquals("batch-42", interaction.webhookConfig().userMetadata().get("job"));

        assertNotNull(interaction.labels());
        assertEquals("prod", interaction.labels().get("env"));
        assertEquals("research", interaction.labels().get("team"));

        assertNotNull(interaction.usage());
        assertEquals(500, interaction.usage().totalInputTokens());
        assertEquals(120, interaction.usage().initialInputTokens());
        assertEquals(800, interaction.usage().totalOutputTokens());
        assertEquals(1300, interaction.usage().totalTokens());
    }

    @Test
    void testInteractionBuilderWithNewFields() {
        WebhookConfig webhookConfig = new WebhookConfig(List.of("https://example.com/wh"), Map.of("k", "v"));
        Interaction interaction = Interaction.builder()
            .id("inter-2")
            .status(Interaction.Status.REQUIRES_CONTINUATION)
            .continuationToken("tok-123")
            .maxTotalTokens("500000")
            .webhookConfig(webhookConfig)
            .label("project", "gemini-sdk")
            .build();

        assertEquals("inter-2", interaction.id());
        assertTrue(interaction.status().requiresContinuation());
        assertEquals("tok-123", interaction.continuationToken());
        assertEquals("500000", interaction.maxTotalTokens());
        assertEquals(webhookConfig, interaction.webhookConfig());
        assertEquals("gemini-sdk", interaction.labels().get("project"));
    }

    @Test
    void testStructuredSpeechConfigAndBackwardCompatibility() throws Exception {
        Config.SpeechConfig speech = new Config.SpeechConfig("Puck", "en-US", "narrator");
        Config.GenerationConfig config = Config.GenerationConfig.builder()
            .structuredSpeechConfig(List.of(speech))
            .build();

        String json = mapper.writeValueAsString(config);
        assertTrue(json.contains("\"structured_speech_config\""));
        assertFalse(json.contains("\"speech_config\""));

        Config.GenerationConfig deserialized = mapper.readValue(json, Config.GenerationConfig.class);
        assertNotNull(deserialized.structuredSpeechConfig());
        assertNotNull(deserialized.speechConfig());
        SpeechConfiguration sc = deserialized.structuredSpeechConfig();
        assertTrue(sc.isSingleSpeaker());
        assertEquals("Puck", sc.singleSpeakerConfigs().get(0).voice());

        // Verify legacy "speech_config" JSON still deserializes via @JsonAlias
        String legacyJson = """
            {
                "speech_config": [
                    {"voice": "Kore", "language": "fr-FR"}
                ]
            }
            """;
        Config.GenerationConfig fromLegacy = mapper.readValue(legacyJson, Config.GenerationConfig.class);
        assertNotNull(fromLegacy.structuredSpeechConfig());
        assertEquals("Kore", fromLegacy.structuredSpeechConfig().singleSpeakerConfigs().get(0).voice());
    }

    @Test
    void testPromptedVoiceWithBaseVoice() throws Exception {
        PromptedVoice prompted = PromptedVoice.of("Make the voice sound more excited and faster", "Puck");
        assertEquals("Make the voice sound more excited and faster", prompted.input());
        assertEquals("Puck", prompted.baseVoice());

        Voice voice = Voice.builder()
            .displayName("Excited Puck")
            .prompted("Make the voice sound more excited and faster", "Puck")
            .build();

        String json = mapper.writeValueAsString(voice);
        assertTrue(json.contains("\"base_voice\":\"Puck\""));
        assertTrue(json.contains("\"input\":\"Make the voice sound more excited and faster\""));

        Voice deserialized = mapper.readValue(json, Voice.class);
        assertNotNull(deserialized.prompted());
        assertEquals("Puck", deserialized.prompted().baseVoice());
    }

    @Test
    void testModelAndAgentInteractionParamsWithLabelsWebhookAndEnvironment() throws Exception {
        WebhookConfig whConfig = new WebhookConfig(List.of("https://hook.example.com"), Map.of("trace", "123"));
        EnvironmentConfig envConfig = EnvironmentConfig.builder().environmentId("env_custom_1").build();

        InteractionParams.ModelInteractionParams modelParams = InteractionParams.ModelInteractionParams.builder()
            .model("gemini-2.5-flash")
            .input("Run analysis")
            .environment(envConfig)
            .webhookConfig(whConfig)
            .label("env", "staging")
            .label("owner", "alice")
            .continuationToken("cont_tok_1")
            .build();

        String modelJson = mapper.writeValueAsString(modelParams);
        assertTrue(modelJson.contains("\"webhook_config\""));
        assertTrue(modelJson.contains("\"labels\""));
        assertTrue(modelJson.contains("\"environment\""));
        assertTrue(modelJson.contains("\"continuation_token\":\"cont_tok_1\""));

        InteractionParams.Request deserializedReq = mapper.readValue(modelJson, InteractionParams.Request.class);
        InteractionParams.ModelInteractionParams deserializedModel =
            assertInstanceOf(InteractionParams.ModelInteractionParams.class, deserializedReq);
        assertEquals("staging", deserializedModel.labels().get("env"));
        assertEquals("alice", deserializedModel.labels().get("owner"));
        assertEquals(List.of("https://hook.example.com"), deserializedModel.webhookConfig().uris());
        assertNotNull(deserializedModel.environment());
        assertTrue(deserializedModel.environment().isCustom());

        InteractionParams.AgentInteractionParams agentParams = InteractionParams.AgentInteractionParams.builder()
            .agent("deep-research-preview-04-2026")
            .input("Research topic")
            .webhookConfig(whConfig)
            .label("priority", "high")
            .build();

        String agentJson = mapper.writeValueAsString(agentParams);
        InteractionParams.AgentInteractionParams deserializedAgent =
            mapper.readValue(agentJson, InteractionParams.AgentInteractionParams.class);
        assertEquals("high", deserializedAgent.labels().get("priority"));
        assertNotNull(deserializedAgent.webhookConfig());
    }

    @Test
    void testCodeExecutionBashLanguageStep() throws Exception {
        String stepJson = """
            {
                "type": "code_execution_call",
                "id": "call_bash_1",
                "arguments": {
                    "language": "bash",
                    "code": "ls -la /workspace"
                }
            }
            """;

        Step step = mapper.readValue(stepJson, Step.class);
        Step.CodeExecutionCallStep bashStep = assertInstanceOf(Step.CodeExecutionCallStep.class, step);
        assertEquals("bash", bashStep.arguments().language());
        assertEquals("ls -la /workspace", bashStep.arguments().code());
    }

    @Test
    void testClientQueryParamsAndWebhookRequests() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.start();
            GeminiInteractionsClient client = GeminiInteractionsClient.builder()
                .baseUrl(server.url("").toString().replaceAll("/$", ""))
                .apiKey("test-key")
                .build();

            // 1. createWebhook with webhook_id
            server.enqueue(new MockResponse()
                .setBody("{\"id\":\"wh-custom\",\"uri\":\"https://example.com\"}")
                .addHeader("Content-Type", "application/json"));
            Webhook createdWh = client.createWebhook(
                new Webhook(null, "test-wh", "https://example.com", List.of("interaction.completed"), Webhook.State.ENABLED, null, null, null, null),
                "wh-custom"
            );
            assertEquals("wh-custom", createdWh.id());
            RecordedRequest req1 = server.takeRequest();
            assertEquals("/v1beta/webhooks?webhook_id=wh-custom", req1.getPath());

            // 2. pingWebhook sends id in PingWebhookRequest body
            server.enqueue(new MockResponse()
                .setBody("{}")
                .addHeader("Content-Type", "application/json"));
            PingWebhookResponse pingResp = client.pingWebhook("wh-custom");
            assertNotNull(pingResp);
            RecordedRequest req2 = server.takeRequest();
            assertEquals("/v1beta/webhooks/wh-custom:ping", req2.getPath());
            PingWebhookRequest sentPing = mapper.readValue(req2.getBody().readUtf8(), PingWebhookRequest.class);
            assertEquals("wh-custom", sentPing.id());

            // 3. rotateSigningSecret populates id in RotateSigningSecretRequest body
            server.enqueue(new MockResponse()
                .setBody("{\"secret\":\"sec_999\"}")
                .addHeader("Content-Type", "application/json"));
            RotateSigningSecretResponse rotateResp = client.rotateSigningSecret(
                "wh-custom",
                new RotateSigningSecretRequest(RotateSigningSecretRequest.RevocationBehavior.REVOKE_PREVIOUS_SECRETS_IMMEDIATELY)
            );
            assertEquals("sec_999", rotateResp.secret());
            RecordedRequest req3 = server.takeRequest();
            RotateSigningSecretRequest sentRotate = mapper.readValue(req3.getBody().readUtf8(), RotateSigningSecretRequest.class);
            assertEquals("wh-custom", sentRotate.id());
            assertEquals(RotateSigningSecretRequest.RevocationBehavior.REVOKE_PREVIOUS_SECRETS_IMMEDIATELY, sentRotate.revocationBehavior());

            // 4. createAgent with parent query param
            server.enqueue(new MockResponse()
                .setBody("{\"id\":\"agent-1\",\"description\":\"My Agent\"}")
                .addHeader("Content-Type", "application/json"));
            Agent createdAgent = client.createAgent(
                Agent.builder().id("agent-1").description("My Agent").build(),
                "projects/my-proj/locations/us-central1"
            );
            assertEquals("agent-1", createdAgent.id());
            RecordedRequest req4 = server.takeRequest();
            assertEquals("/v1beta/agents?parent=projects%2Fmy-proj%2Flocations%2Fus-central1", req4.getPath());

            // 5. listTriggerExecutions with filter query param
            server.enqueue(new MockResponse()
                .setBody("{\"trigger_executions\":[]}")
                .addHeader("Content-Type", "application/json"));
            ListTriggerExecutionsResponse execResp = client.listTriggerExecutions("trig-1", 10, "tok-p2", "status=\"COMPLETED\"");
            assertNotNull(execResp);
            RecordedRequest req5 = server.takeRequest();
            assertEquals("/v1beta/triggers/trig-1/executions?filter=status%3D%22COMPLETED%22&page_size=10&page_token=tok-p2", req5.getPath());
        }
    }
}
