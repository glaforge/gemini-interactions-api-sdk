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

import io.github.glaforge.gemini.interactions.model.AgentOption;
import io.github.glaforge.gemini.interactions.model.Config.AntigravityAgentConfig;
import io.github.glaforge.gemini.interactions.model.Config.GenerationConfig;
import io.github.glaforge.gemini.interactions.model.Config.ThinkingLevel;
import io.github.glaforge.gemini.interactions.model.Config.ThinkingSummaries;
import io.github.glaforge.gemini.interactions.model.Events;
import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.LocalEnvironmentConfig;
import io.github.glaforge.gemini.interactions.model.ModelOption;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for Gemini 4 Argon support, Long Decode, ThinkingLevel, StepStop usage, and LocalEnvironmentConfig.
 */
class Gemini4ArgonTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();

    @Test
    void testGemini4ArgonModelConstant() {
        assertEquals("gemini-4-argon", ModelOption.GEMINI_4_ARGON);
    }

    @Test
    void testThinkingLevelsSerializationAndDeserialization() throws Exception {
        for (ThinkingLevel level : ThinkingLevel.values()) {
            GenerationConfig config = GenerationConfig.builder()
                .thinkingLevel(level)
                .thinkingSummaries(ThinkingSummaries.AUTO)
                .build();

            String json = mapper.writeValueAsString(config);
            String expectedValue = level.name().toLowerCase();
            assertTrue(json.contains("\"thinking_level\":\"" + expectedValue + "\""));

            GenerationConfig deserialized = mapper.readValue(json, GenerationConfig.class);
            assertEquals(level, deserialized.thinkingLevel());
            assertEquals(ThinkingSummaries.AUTO, deserialized.thinkingSummaries());
        }
    }

    @Test
    void testGemini4ArgonLongDecodeParams() throws Exception {
        ModelInteractionParams params = ModelInteractionParams.builder()
            .model(ModelOption.GEMINI_4_ARGON)
            .input("Draft a comprehensive technical specification for a distributed database.")
            .background(true)
            .generationConfig(GenerationConfig.builder()
                .maxOutputTokens(200_000)
                .thinkingLevel(ThinkingLevel.MEDIUM)
                .thinkingSummaries(ThinkingSummaries.AUTO)
                .build())
            .build();

        assertEquals("gemini-4-argon", params.model());
        assertEquals(Boolean.TRUE, params.background());
        assertEquals(200_000, params.generationConfig().maxOutputTokens());
        assertEquals(ThinkingLevel.MEDIUM, params.generationConfig().thinkingLevel());

        String json = mapper.writeValueAsString(params);
        assertTrue(json.contains("\"model\":\"gemini-4-argon\""));
        assertTrue(json.contains("\"background\":true"));
        assertTrue(json.contains("\"max_output_tokens\":200000"));
        assertTrue(json.contains("\"thinking_level\":\"medium\""));
    }

    @Test
    void testAntigravityAgentWithGemini4Argon() throws Exception {
        AgentInteractionParams params = AgentInteractionParams.builder()
            .agent(AgentOption.ANTIGRAVITY_PREVIEW_09_2026)
            .input("Analyze and refactor the repository.")
            .background(true)
            .environment("remote")
            .agentConfig(AntigravityAgentConfig.builder()
                .model(ModelOption.GEMINI_4_ARGON)
                .maxTotalTokens(250_000L)
                .build())
            .build();

        String json = mapper.writeValueAsString(params);
        assertTrue(json.contains("\"agent\":\"antigravity-preview-09-2026\""));
        assertTrue(json.contains("\"model\":\"gemini-4-argon\""));
        assertTrue(json.contains("\"max_total_tokens\":250000"));
    }

    @Test
    void testStepStopWithStepUsageAndUsage() throws Exception {
        String eventJson = """
            {
              "event_type": "step.stop",
              "event_id": "evt-step-stop-1",
              "index": 0,
              "step_usage": {
                "total_input_tokens": 1500,
                "total_output_tokens": 65536,
                "total_thought_tokens": 4096,
                "total_tokens": 71132
              },
              "usage": {
                "total_input_tokens": 1500,
                "total_output_tokens": 65536,
                "total_thought_tokens": 4096,
                "total_tokens": 71132
              }
            }
            """;

        Events event = mapper.readValue(eventJson, Events.class);
        assertInstanceOf(Events.StepStop.class, event);
        Events.StepStop stepStop = (Events.StepStop) event;
        assertEquals(Events.EventType.STEP_STOP, stepStop.eventType());
        assertEquals("evt-step-stop-1", stepStop.eventId());
        assertEquals(0, stepStop.index());
        assertNotNull(stepStop.stepUsage());
        assertEquals(65536, stepStop.stepUsage().totalOutputTokens());
        assertEquals(4096, stepStop.stepUsage().totalThoughtTokens());
        assertNotNull(stepStop.usage());
        assertEquals(71132, stepStop.usage().totalTokens());

        // Verify backward-compatible 4-arg constructor
        Events.StepStop legacyStop = new Events.StepStop(Events.EventType.STEP_STOP, "evt-0", 0, null);
        assertNull(legacyStop.stepUsage());
        assertNull(legacyStop.usage());
    }

    @Test
    void testLocalEnvironmentConfigWithEnv() throws Exception {
        LocalEnvironmentConfig localConfig = LocalEnvironmentConfig.builder()
            .env("NODE_ENV", "production")
            .envCredential("GITHUB_TOKEN", "cred_gh_123")
            .build();

        assertEquals("local", localConfig.type());
        assertNotNull(localConfig.env());
        assertEquals(2, localConfig.env().size());

        String json = mapper.writeValueAsString(localConfig);
        assertTrue(json.contains("\"type\":\"local\""));
        assertTrue(json.contains("\"NODE_ENV\":\"production\""));
        assertTrue(json.contains("\"credential\":\"cred_gh_123\""));

        LocalEnvironmentConfig deserialized = mapper.readValue(json, LocalEnvironmentConfig.class);
        assertEquals("local", deserialized.type());
        assertNotNull(deserialized.env());
        assertEquals("production", deserialized.env().get("NODE_ENV"));
    }
}
