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

import io.github.glaforge.gemini.interactions.model.AllowlistEntry;
import io.github.glaforge.gemini.interactions.model.EnvironmentConfig;
import io.github.glaforge.gemini.interactions.model.EnvironmentNetworkEgressAllowlist;
import io.github.glaforge.gemini.interactions.model.Events;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams;
import io.github.glaforge.gemini.interactions.model.ModelOption;
import io.github.glaforge.gemini.interactions.model.NetworkConfiguration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for updates introduced in the October 12, 2026 OpenAPI specification sync.
 */
class OpenApiUpdateOct12Test {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder().build();
    }

    @Test
    void testNewModelOptionConstants() {
        assertEquals("gemini-3.1-flash-lite-image", ModelOption.GEMINI_3_1_FLASH_LITE_IMAGE);
        assertEquals("gemini-nano-banana-2.1", ModelOption.GEMINI_NANO_BANANA_2_1);
        assertEquals("gemini-omni-1.1-flash", ModelOption.GEMINI_OMNI_1_1_FLASH);
        assertEquals("gemini-omni-flash-preview", ModelOption.GEMINI_OMNI_FLASH_PREVIEW);
        assertEquals("lyria-3.5", ModelOption.LYRIA_3_5);
        assertEquals("gemini-3.5-flash-lite", ModelOption.GEMINI_3_5_FLASH_LITE);
    }

    @Test
    void testInteractionContinuationToken() throws Exception {
        String interactionJson = """
            {
              "id": "inter-incomplete-1",
              "model": "gemini-3.8-flash",
              "status": "incomplete",
              "continuation_token": "dG9rZW4tMTIzNDU="
            }
            """;

        Interaction interaction = mapper.readValue(interactionJson, Interaction.class);
        assertNotNull(interaction);
        assertEquals("inter-incomplete-1", interaction.id());
        assertEquals(Interaction.Status.INCOMPLETE, interaction.status());
        assertEquals("dG9rZW4tMTIzNDU=", interaction.continuationToken());

        Interaction built = Interaction.builder()
            .id("inter-incomplete-2")
            .status(Interaction.Status.INCOMPLETE)
            .continuationToken("cmVzdW1lLXRva2Vu")
            .build();
        assertEquals("cmVzdW1lLXRva2Vu", built.continuationToken());

        String serialized = mapper.writeValueAsString(built);
        assertTrue(serialized.contains("\"continuation_token\":\"cmVzdW1lLXRva2Vu\""));
    }

    @Test
    void testModelInteractionParamsContinuationToken() throws Exception {
        InteractionParams.ModelInteractionParams params = InteractionParams.ModelInteractionParams.builder()
            .model(ModelOption.GEMINI_3_8_FLASH)
            .input("Continue generating the long report")
            .continuationToken("Y2hlY2twb2ludC1hYmM=")
            .build();

        assertEquals("Y2hlY2twb2ludC1hYmM=", params.continuationToken());

        String json = mapper.writeValueAsString(params);
        assertTrue(json.contains("\"continuation_token\":\"Y2hlY2twb2ludC1hYmM=\""));

        InteractionParams.ModelInteractionParams deserialized =
            mapper.readValue(json, InteractionParams.ModelInteractionParams.class);
        assertEquals("Y2hlY2twb2ludC1hYmM=", deserialized.continuationToken());
    }

    @Test
    void testAgentInteractionParamsContinuationToken() throws Exception {
        InteractionParams.AgentInteractionParams params = InteractionParams.AgentInteractionParams.builder()
            .agent(ModelOption.ANTIGRAVITY_PREVIEW_09_2026)
            .input("Resume agent execution")
            .continuationToken("YWdlbnQtdG9rZW4tNzg5")
            .build();

        assertEquals("YWdlbnQtdG9rZW4tNzg5", params.continuationToken());

        String json = mapper.writeValueAsString(params);
        assertTrue(json.contains("\"continuation_token\":\"YWdlbnQtdG9rZW4tNzg5\""));

        InteractionParams.AgentInteractionParams deserialized =
            mapper.readValue(json, InteractionParams.AgentInteractionParams.class);
        assertEquals("YWdlbnQtdG9rZW4tNzg5", deserialized.continuationToken());
    }

    @Test
    void testInteractionStatusUpdateWithContinuationTokenAndUsage() throws Exception {
        String eventJson = """
            {
              "event_type": "interaction.status_update",
              "event_id": "evt-status-42",
              "interaction_id": "inter-stream-99",
              "status": "in_progress",
              "continuation_token": "c3RyZWFtLWNoZWNrcG9pbnQtMQ==",
              "usage": {
                "total_input_tokens": 1200,
                "total_output_tokens": 450,
                "total_tokens": 1650
              }
            }
            """;

        Events event = mapper.readValue(eventJson, Events.class);
        assertInstanceOf(Events.InteractionStatusUpdate.class, event);
        Events.InteractionStatusUpdate statusUpdate = (Events.InteractionStatusUpdate) event;

        assertEquals(Events.EventType.INTERACTION_STATUS_UPDATE, statusUpdate.eventType());
        assertEquals("evt-status-42", statusUpdate.eventId());
        assertEquals("inter-stream-99", statusUpdate.interactionId());
        assertEquals("in_progress", statusUpdate.status());
        assertEquals("c3RyZWFtLWNoZWNrcG9pbnQtMQ==", statusUpdate.continuationToken());
        assertNotNull(statusUpdate.usage());
        assertEquals(1200, statusUpdate.usage().totalInputTokens());
        assertEquals(450, statusUpdate.usage().totalOutputTokens());
        assertEquals(1650, statusUpdate.usage().totalTokens());

        // Verify backward-compatible 5-arg constructor
        Events.InteractionStatusUpdate legacyUpdate = new Events.InteractionStatusUpdate(
            Events.EventType.INTERACTION_STATUS_UPDATE,
            "evt-legacy",
            "inter-legacy",
            "completed",
            null
        );
        assertNull(legacyUpdate.continuationToken());
        assertNull(legacyUpdate.usage());
    }

    @Test
    void testEnvironmentNetworkEgressAllowlistDisabledAndEntries() throws Exception {
        // 1. Disabled allowlist {"allowlist": "disabled"}
        EnvironmentNetworkEgressAllowlist disabledAllowlist = EnvironmentNetworkEgressAllowlist.disabled();
        assertTrue(disabledAllowlist.isDisabled());
        assertNull(disabledAllowlist.allowlist());
        assertEquals("disabled", disabledAllowlist.mode());

        String disabledJson = mapper.writeValueAsString(disabledAllowlist);
        assertEquals("{\"allowlist\":\"disabled\"}", disabledJson);

        EnvironmentNetworkEgressAllowlist deserializedDisabled =
            mapper.readValue(disabledJson, EnvironmentNetworkEgressAllowlist.class);
        assertTrue(deserializedDisabled.isDisabled());
        assertNull(deserializedDisabled.allowlist());

        // 2. Domain rules allowlist {"allowlist": [{"domain": "github.com"}]}
        EnvironmentNetworkEgressAllowlist rulesAllowlist = EnvironmentNetworkEgressAllowlist.of(
            AllowlistEntry.of("github.com"),
            AllowlistEntry.of("*.googleapis.com")
        );
        assertFalse(rulesAllowlist.isDisabled());
        assertNotNull(rulesAllowlist.allowlist());
        assertEquals(2, rulesAllowlist.allowlist().size());

        String rulesJson = mapper.writeValueAsString(rulesAllowlist);
        assertTrue(rulesJson.contains("\"allowlist\":[{\"domain\":\"github.com\"},{\"domain\":\"*.googleapis.com\"}]"));

        EnvironmentNetworkEgressAllowlist deserializedRules =
            mapper.readValue(rulesJson, EnvironmentNetworkEgressAllowlist.class);
        assertFalse(deserializedRules.isDisabled());
        assertNotNull(deserializedRules.allowlist());
        assertEquals(2, deserializedRules.allowlist().size());
        assertEquals("github.com", deserializedRules.allowlist().getFirst().domain());

        // 3. Inside EnvironmentConfig
        EnvironmentConfig envConfig = EnvironmentConfig.builder()
            .network(EnvironmentNetworkEgressAllowlist.disabled())
            .build();
        String envJson = mapper.writeValueAsString(envConfig);
        assertTrue(envJson.contains("\"network\":{\"allowlist\":\"disabled\"}"));

        EnvironmentConfig deserializedEnv = mapper.readValue(envJson, EnvironmentConfig.class);
        assertNotNull(deserializedEnv.network());
        assertTrue(deserializedEnv.network().isCustom());
        assertTrue(deserializedEnv.network().config().isDisabled());

        // 4. Inside NetworkConfiguration with top-level string preset
        NetworkConfiguration presetNet = NetworkConfiguration.of("disabled");
        assertEquals("\"disabled\"", mapper.writeValueAsString(presetNet));
    }
}
