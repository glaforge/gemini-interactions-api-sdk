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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.glaforge.gemini.interactions.model.AgentOption;
import io.github.glaforge.gemini.interactions.model.Config.AntigravityAgentConfig;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;
import io.github.glaforge.gemini.interactions.model.ModelOption;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AntigravityAgentTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void testAgentOptionConstants() {
        assertEquals("antigravity-preview-09-2026", AgentOption.ANTIGRAVITY_PREVIEW_09_2026);
        assertEquals("antigravity-preview-05-2026", AgentOption.ANTIGRAVITY_PREVIEW_05_2026);
    }

    @Test
    void testModelOptionConstants() {
        assertEquals("antigravity-preview-09-2026", ModelOption.ANTIGRAVITY_PREVIEW_09_2026);
        assertEquals("gemini-3.5-flash-lite", ModelOption.GEMINI_3_5_FLASH_LITE);
        assertEquals("gemini-3.8-flash", ModelOption.GEMINI_3_8_FLASH);
    }

    @Test
    void testAntigravityAgentConfigBuilder() throws Exception {
        AntigravityAgentConfig config = AntigravityAgentConfig.builder()
            .model(ModelOption.GEMINI_3_5_FLASH_LITE)
            .maxTotalTokens(50000L)
            .build();

        assertEquals("antigravity", config.type());
        assertEquals(ModelOption.GEMINI_3_5_FLASH_LITE, config.model());
        assertEquals(50000L, config.maxTotalTokens());

        String json = mapper.writeValueAsString(config);
        assertTrue(json.contains("\"type\":\"antigravity\""));
        assertTrue(json.contains("\"model\":\"gemini-3.5-flash-lite\""));
        assertTrue(json.contains("\"max_total_tokens\":50000"));

        AntigravityAgentConfig deserialized = mapper.readValue(json, AntigravityAgentConfig.class);
        assertEquals("antigravity", deserialized.type());
        assertEquals(ModelOption.GEMINI_3_5_FLASH_LITE, deserialized.model());
        assertEquals(50000L, deserialized.maxTotalTokens());
    }

    @Test
    void testAntigravityAgentConfigStringTokenBuilder() {
        AntigravityAgentConfig config = AntigravityAgentConfig.builder()
            .model("gemini-3.8-flash")
            .maxTotalTokens("75000")
            .build();

        assertEquals("antigravity", config.type());
        assertEquals("gemini-3.8-flash", config.model());
        assertEquals(75000L, config.maxTotalTokens());
    }

    @Test
    void testAntigravityAgentConfigModelConstructor() {
        AntigravityAgentConfig config = new AntigravityAgentConfig(ModelOption.GEMINI_3_7_FLASH);

        assertEquals("antigravity", config.type());
        assertEquals(ModelOption.GEMINI_3_7_FLASH, config.model());
        assertNull(config.maxTotalTokens());
    }

    @Test
    void testInteractionParamsWithAntigravity() throws Exception {
        AgentInteractionParams params = AgentInteractionParams.builder()
            .agent(AgentOption.ANTIGRAVITY_PREVIEW_09_2026)
            .input("Run analysis")
            .environment("remote")
            .agentConfig(AntigravityAgentConfig.builder()
                .model(ModelOption.GEMINI_3_8_FLASH)
                .maxTotalTokens(50000L)
                .build())
            .build();

        assertEquals(AgentOption.ANTIGRAVITY_PREVIEW_09_2026, params.agent());
        assertInstanceOf(AntigravityAgentConfig.class, params.agentConfig());

        String json = mapper.writeValueAsString(params);
        assertTrue(json.contains("\"agent\":\"antigravity-preview-09-2026\""));
        assertTrue(json.contains("\"model\":\"gemini-3.8-flash\""));
    }

    @Test
    void testInteractionStatusIsFinished() {
        // Terminal states must return true
        assertTrue(Interaction.Status.COMPLETED.isFinished());
        assertTrue(Interaction.Status.FAILED.isFinished());
        assertTrue(Interaction.Status.CANCELLED.isFinished());
        assertTrue(Interaction.Status.INCOMPLETE.isFinished());
        assertTrue(Interaction.Status.BUDGET_EXCEEDED.isFinished());

        // In-progress / queued states must return false
        assertFalse(Interaction.Status.IN_PROGRESS.isFinished());
        assertFalse(Interaction.Status.QUEUED.isFinished());
        assertFalse(Interaction.Status.REQUIRES_ACTION.isFinished());
    }

    @Test
    void testInteractionStatusHelpers() {
        assertTrue(Interaction.Status.INCOMPLETE.isIncomplete());
        assertFalse(Interaction.Status.COMPLETED.isIncomplete());

        assertTrue(Interaction.Status.COMPLETED.isCompleted());
        assertFalse(Interaction.Status.FAILED.isCompleted());

        assertTrue(Interaction.Status.FAILED.isFailed());
        assertFalse(Interaction.Status.CANCELLED.isFailed());
    }
}
