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
import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.ModelOption;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for model option constants and deprecation annotations updated in November 2026.
 */
class ModelOptionUpdateNov11Test {

    private final ObjectMapper mapper = JsonMapper.builder().build();

    @Test
    void testNewModelOptionConstants() {
        assertEquals("gemini-4-carbon", ModelOption.GEMINI_4_CARBON);
        assertEquals("gemini-decision-1", ModelOption.GEMINI_DECISION_1);
        assertEquals("gemini-omni-1.2-flash", ModelOption.GEMINI_OMNI_1_2_FLASH);
        assertEquals("gemini-3.5-transcribe", ModelOption.GEMINI_3_5_TRANSCRIBE);
    }

    @Test
    void testActiveModelsAreNotDeprecated() throws Exception {
        List<String> activeFields = List.of(
            "GEMINI_4_CARBON",
            "GEMINI_4_ARGON",
            "GEMINI_DECISION_1",
            "GEMINI_OMNI_1_2_FLASH",
            "GEMINI_OMNI_1_1_FLASH",
            "GEMINI_3_8_FLASH",
            "GEMINI_3_5_FLASH_LITE",
            "GEMINI_3_5_TRANSCRIBE",
            "GEMINI_2_5_FLASH_LITE",
            "GEMINI_EMBEDDING_2",
            "ANTIGRAVITY_PREVIEW_09_2026",
            "DEEP_RESEARCH_PREVIEW_04_2026"
        );

        for (String fieldName : activeFields) {
            Field field = ModelOption.class.getField(fieldName);
            assertFalse(
                field.isAnnotationPresent(Deprecated.class),
                "Expected ModelOption." + fieldName + " not to be @Deprecated"
            );
        }

        assertFalse(AgentOption.class.getField("ANTIGRAVITY_PREVIEW_09_2026").isAnnotationPresent(Deprecated.class));
        assertFalse(AgentOption.class.getField("DEEP_RESEARCH_PREVIEW_04_2026").isAnnotationPresent(Deprecated.class));
    }

    @Test
    void testDeprecatedModelsAndAgentsHaveDeprecatedAnnotation() throws Exception {
        List<String> deprecatedModelFields = List.of(
            "ANTIGRAVITY_PREVIEW_05_2026",
            "DEEP_RESEARCH_PRO_PREVIEW_12_2025",
            "GEMINI_2_0_FLASH",
            "GEMINI_2_0_FLASH_001",
            "GEMINI_2_0_FLASH_LITE",
            "GEMINI_2_0_FLASH_LITE_001",
            "GEMINI_2_5_COMPUTER_USE_PREVIEW_10_2025",
            "GEMINI_2_5_FLASH",
            "GEMINI_2_5_FLASH_LITE_PREVIEW_09_2025",
            "GEMINI_2_5_FLASH_PREVIEW_09_2025",
            "GEMINI_2_5_PRO",
            "GEMINI_3_PRO_IMAGE_PREVIEW",
            "GEMINI_3_PRO_PREVIEW",
            "GEMINI_3_1_FLASH_IMAGE_PREVIEW",
            "GEMINI_3_1_FLASH_LITE_PREVIEW",
            "GEMINI_EMBEDDING_2_PREVIEW",
            "GEMINI_OMNI_FLASH_PREVIEW",
            "GEMINI_ROBOTICS_ER_1_5_PREVIEW",
            "GEMINI_ROBOTICS_ER_1_6_PREVIEW",
            "IMAGEN_4_0_FAST_GENERATE_001",
            "IMAGEN_4_0_GENERATE_001",
            "IMAGEN_4_0_ULTRA_GENERATE_001",
            "VEO_2_0_GENERATE_001",
            "VEO_3_0_FAST_GENERATE_001",
            "VEO_3_0_GENERATE_001",
            "VEO_3_1_FAST_GENERATE_PREVIEW",
            "VEO_3_1_GENERATE_PREVIEW",
            "VEO_3_1_LITE_GENERATE_PREVIEW"
        );

        for (String fieldName : deprecatedModelFields) {
            Field field = ModelOption.class.getField(fieldName);
            assertTrue(
                field.isAnnotationPresent(Deprecated.class),
                "Expected ModelOption." + fieldName + " to be @Deprecated"
            );
        }

        assertTrue(AgentOption.class.getField("ANTIGRAVITY_PREVIEW_05_2026").isAnnotationPresent(Deprecated.class));
        assertTrue(AgentOption.class.getField("DEEP_RESEARCH_PRO_PREVIEW_12_2025").isAnnotationPresent(Deprecated.class));
    }

    @Test
    void testGemini4CarbonInteractionSerialization() throws Exception {
        ModelInteractionParams params = ModelInteractionParams.builder()
            .model(ModelOption.GEMINI_4_CARBON)
            .input("Design a fault-tolerant consensus protocol.")
            .background(true)
            .generationConfig(GenerationConfig.builder()
                .maxOutputTokens(128_000)
                .thinkingLevel(ThinkingLevel.HIGH)
                .build())
            .build();

        String json = mapper.writeValueAsString(params);
        assertTrue(json.contains("\"model\":\"gemini-4-carbon\""));
        assertTrue(json.contains("\"thinking_level\":\"high\""));
        assertTrue(json.contains("\"max_output_tokens\":128000"));

        AgentInteractionParams agentParams = AgentInteractionParams.builder()
            .agent(AgentOption.ANTIGRAVITY_PREVIEW_09_2026)
            .input("Audit security controls.")
            .agentConfig(AntigravityAgentConfig.builder()
                .model(ModelOption.GEMINI_4_CARBON)
                .build())
            .build();

        String agentJson = mapper.writeValueAsString(agentParams);
        assertTrue(agentJson.contains("\"model\":\"gemini-4-carbon\""));
    }
}
