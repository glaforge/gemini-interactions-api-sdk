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
import io.github.glaforge.gemini.interactions.model.Content;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.ModelOption;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenApiUpdateTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();

    @Test
    void testModelOptionGemini36FlashConstant() {
        assertEquals("gemini-3.6-flash", ModelOption.GEMINI_3_6_FLASH);
    }

    @Test
    void testInteractionStatusQueuedAndBudgetExceeded() throws Exception {
        String jsonQueued = "\"queued\"";
        Interaction.Status statusQueued = mapper.readValue(jsonQueued, Interaction.Status.class);
        assertEquals(Interaction.Status.QUEUED, statusQueued);

        String jsonBudget = "\"budget_exceeded\"";
        Interaction.Status statusBudget = mapper.readValue(jsonBudget, Interaction.Status.class);
        assertEquals(Interaction.Status.BUDGET_EXCEEDED, statusBudget);
    }

    @Test
    void testTranscriptionConfigSerialization() throws Exception {
        Config.TranscriptionConfig transcriptionConfig = Config.TranscriptionConfig.builder()
            .languageHints(List.of("en-US", "auto"))
            .customVocabulary(List.of("Gemini", "Antigravity"))
            .diarizationMode("speaker")
            .timestampGranularities(List.of("word"))
            .build();

        Config.GenerationConfig genConfig = Config.GenerationConfig.builder()
            .transcriptionConfig(transcriptionConfig)
            .build();

        String json = mapper.writeValueAsString(genConfig);
        assertTrue(json.contains("\"transcription_config\""));
        assertTrue(json.contains("\"language_hints\":[\"en-US\",\"auto\"]"));

        Config.GenerationConfig deserialized = mapper.readValue(json, Config.GenerationConfig.class);
        assertNotNull(deserialized.transcriptionConfig());
        assertEquals(List.of("en-US", "auto"), deserialized.transcriptionConfig().languageHints());
        assertEquals("speaker", deserialized.transcriptionConfig().diarizationMode());
    }

    @Test
    void testWordInfoAnnotationDeserialization() throws Exception {
        String json = """
            {
                "type": "word_info",
                "start_index": 0,
                "end_index": 5,
                "start_offset": "0s",
                "end_offset": "0.5s",
                "text": "Hello",
                "speaker": "spk_1"
            }
            """;

        Content.Annotation annotation = mapper.readValue(json, Content.Annotation.class);
        assertInstanceOf(Content.WordInfo.class, annotation);
        Content.WordInfo wordInfo = (Content.WordInfo) annotation;
        assertEquals("word_info", wordInfo.type());
        assertEquals(0, wordInfo.startIndex());
        assertEquals(5, wordInfo.endIndex());
        assertEquals("Hello", wordInfo.text());
        assertEquals("spk_1", wordInfo.speaker());
    }

    @Test
    void testAntigravityAgentConfigWithModel() throws Exception {
        Config.AntigravityAgentConfig agentConfig = new Config.AntigravityAgentConfig(100000L, ModelOption.GEMINI_3_6_FLASH);
        Agent agent = Agent.builder()
            .id("test-agent")
            .agentConfig(agentConfig)
            .build();

        String json = mapper.writeValueAsString(agent);
        assertTrue(json.contains("\"model\":\"gemini-3.6-flash\""));

        Agent deserialized = mapper.readValue(json, Agent.class);
        assertNotNull(deserialized.agentConfig());
        assertInstanceOf(Config.AntigravityAgentConfig.class, deserialized.agentConfig());
        Config.AntigravityAgentConfig config = (Config.AntigravityAgentConfig) deserialized.agentConfig();
        assertEquals(ModelOption.GEMINI_3_6_FLASH, config.model());
        assertEquals(100000L, config.maxTotalTokens());
    }
}
