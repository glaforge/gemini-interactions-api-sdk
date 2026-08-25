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
import io.github.glaforge.gemini.interactions.model.CreateEnvironmentRequest;
import io.github.glaforge.gemini.interactions.model.Environment;
import io.github.glaforge.gemini.interactions.model.Events;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams;
import io.github.glaforge.gemini.interactions.model.ListEnvironmentsResponse;
import io.github.glaforge.gemini.interactions.model.ModelOption;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
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
        assertEquals("gemini-robotics-er-2-preview", ModelOption.GEMINI_ROBOTICS_ER_2_PREVIEW);
    }

    @Test
    void testServiceTierDeferred() throws Exception {
        String jsonDeferred = "\"deferred\"";
        InteractionParams.ServiceTier tier = mapper.readValue(jsonDeferred, InteractionParams.ServiceTier.class);
        assertEquals(InteractionParams.ServiceTier.DEFERRED, tier);
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
            .languageCodes(List.of("en-US", "auto"))
            .customVocabulary(List.of("Gemini", "Antigravity"))
            .diarizationMode("speaker")
            .timestampGranularities(List.of("word"))
            .build();

        Config.GenerationConfig genConfig = Config.GenerationConfig.builder()
            .transcriptionConfig(transcriptionConfig)
            .build();

        String json = mapper.writeValueAsString(genConfig);
        assertTrue(json.contains("\"transcription_config\""));
        assertTrue(json.contains("\"language_codes\":[\"en-US\",\"auto\"]"));

        Config.GenerationConfig deserialized = mapper.readValue(json, Config.GenerationConfig.class);
        assertNotNull(deserialized.transcriptionConfig());
        assertEquals(List.of("en-US", "auto"), deserialized.transcriptionConfig().languageCodes());
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

    @Test
    void testEnvironmentSerializationAndDeserialization() throws Exception {
        Environment env = Environment.builder()
            .id("env-456")
            .status(Environment.Status.ACTIVE)
            .fileCount(12L)
            .sizeBytes(204800L)
            .created(Instant.parse("2026-08-15T12:00:00Z"))
            .network("disabled")
            .build();

        String json = mapper.writeValueAsString(env);
        assertTrue(json.contains("\"id\":\"env-456\""));
        assertTrue(json.contains("\"status\":\"active\""));
        assertTrue(json.contains("\"file_count\":12"));
        assertTrue(json.contains("\"size_bytes\":204800"));
        assertTrue(json.contains("\"network\":\"disabled\""));

        Environment deserialized = mapper.readValue(json, Environment.class);
        assertNotNull(deserialized);
        assertEquals("env-456", deserialized.id());
        assertEquals(Environment.Status.ACTIVE, deserialized.status());
        assertEquals(12L, deserialized.fileCount());
        assertEquals(204800L, deserialized.sizeBytes());
        assertEquals("disabled", deserialized.network().preset());
    }

    @Test
    void testCreateEnvironmentRequestAndListResponse() throws Exception {
        CreateEnvironmentRequest req = CreateEnvironmentRequest.builder()
            .network("disabled")
            .build();

        String reqJson = mapper.writeValueAsString(req);
        assertTrue(reqJson.contains("\"network\":\"disabled\""));

        String listJson = """
            {
                "environments": [
                    {
                        "id": "env-1",
                        "status": "active"
                    },
                    {
                        "id": "env-2",
                        "status": "expired"
                    }
                ],
                "next_page_token": "token-xyz"
            }
            """;

        ListEnvironmentsResponse listResp = mapper.readValue(listJson, ListEnvironmentsResponse.class);
        assertNotNull(listResp);
        assertEquals(2, listResp.environments().size());
        assertEquals("env-1", listResp.environments().get(0).id());
        assertEquals(Environment.Status.ACTIVE, listResp.environments().get(0).status());
        assertEquals(Environment.Status.EXPIRED, listResp.environments().get(1).status());
        assertEquals("token-xyz", listResp.nextPageToken());
    }

    @Test
    void testVideoContentWithStaticMediaProcessing() throws Exception {
        Content.StaticMediaProcessing processing = new Content.StaticMediaProcessing("1.5s", "10s", 2.0);
        Content.VideoContent video = new Content.VideoContent("video", null, "https://example.com/video.mp4", "video/mp4", null, processing);

        String json = mapper.writeValueAsString(video);
        assertTrue(json.contains("\"processing\""));
        assertTrue(json.contains("\"start_offset\":\"1.5s\""));
        assertTrue(json.contains("\"fps\":2.0"));

        Content deserialized = mapper.readValue(json, Content.class);
        assertInstanceOf(Content.VideoContent.class, deserialized);
        Content.VideoContent deserializedVideo = (Content.VideoContent) deserialized;
        assertNotNull(deserializedVideo.processing());
        assertTrue(deserializedVideo.processing().isCustom());
        Content.StaticMediaProcessing deserializedProc = (Content.StaticMediaProcessing) deserializedVideo.processing().config();
        assertEquals("1.5s", deserializedProc.startOffset());
        assertEquals("10s", deserializedProc.endOffset());
        assertEquals(2.0, deserializedProc.fps());
    }

    @Test
    void testInteractionErrorsDeserialization() throws Exception {
        String json = """
            {
                "id": "interaction-err-123",
                "status": "failed",
                "errors": [
                    {
                        "code": "QUOTA_EXCEEDED",
                        "message": "Resource quota limit exceeded."
                    }
                ]
            }
            """;

        Interaction interaction = mapper.readValue(json, Interaction.class);
        assertNotNull(interaction);
        assertEquals("interaction-err-123", interaction.id());
        assertEquals(Interaction.Status.FAILED, interaction.status());
        assertNotNull(interaction.errors());
        assertEquals(1, interaction.errors().size());
        Events.Error error = interaction.errors().get(0);
        assertEquals("QUOTA_EXCEEDED", error.code());
        assertEquals("Resource quota limit exceeded.", error.message());
    }
}
