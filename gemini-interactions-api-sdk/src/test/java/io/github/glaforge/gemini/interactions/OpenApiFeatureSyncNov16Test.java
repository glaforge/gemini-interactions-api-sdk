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

import io.github.glaforge.gemini.interactions.model.Content;
import io.github.glaforge.gemini.interactions.model.Content.AudioContent;
import io.github.glaforge.gemini.interactions.model.Content.FileCitation;
import io.github.glaforge.gemini.interactions.model.Events;
import io.github.glaforge.gemini.interactions.model.Events.AudioDelta;
import io.github.glaforge.gemini.interactions.model.Events.DeltaType;
import io.github.glaforge.gemini.interactions.model.Events.GoogleSearchCallDelta;
import io.github.glaforge.gemini.interactions.model.Events.StepDelta;
import io.github.glaforge.gemini.interactions.model.Step;
import io.github.glaforge.gemini.interactions.model.Step.GoogleSearchCallArguments;
import io.github.glaforge.gemini.interactions.model.Step.GoogleSearchCallStep;
import io.github.glaforge.gemini.interactions.model.Step.GoogleSearchResult;
import io.github.glaforge.gemini.interactions.model.Step.GoogleSearchResultStep;
import io.github.glaforge.gemini.interactions.model.Tool;
import io.github.glaforge.gemini.interactions.model.Tool.ComputerUse;
import io.github.glaforge.gemini.interactions.model.Tool.GoogleMaps;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests verifying OpenAPI schema alignment for GoogleMaps, ComputerUse, FileCitation,
 * GoogleSearchCallStep, GoogleSearchResult, AudioContent, and AudioDelta.
 */
class OpenApiFeatureSyncNov16Test {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testGoogleMapsToolSerializationAndDeserialization() {
        GoogleMaps mapsTool = GoogleMaps.builder()
            .latitude(37.7749)
            .longitude(-122.4194)
            .enableWidget(true)
            .build();

        assertEquals("google_maps", mapsTool.type());
        assertEquals(37.7749, mapsTool.latitude());
        assertEquals(-122.4194, mapsTool.longitude());
        assertTrue(mapsTool.enableWidget());

        String json = mapper.writeValueAsString(mapsTool);
        JsonNode node = mapper.readTree(json);
        assertEquals("google_maps", node.get("type").asText());
        assertEquals(37.7749, node.get("latitude").asDouble());
        assertEquals(-122.4194, node.get("longitude").asDouble());
        assertTrue(node.get("enable_widget").asBoolean());

        Tool deserialized = mapper.readValue(json, Tool.class);
        GoogleMaps parsed = assertInstanceOf(GoogleMaps.class, deserialized);
        assertEquals(37.7749, parsed.latitude());
        assertEquals(-122.4194, parsed.longitude());
        assertTrue(parsed.enableWidget());

        // Convenience constructors
        GoogleMaps coordsOnly = new GoogleMaps(48.8566, 2.3522);
        assertEquals(48.8566, coordsOnly.latitude());
        assertEquals(2.3522, coordsOnly.longitude());
        assertNull(coordsOnly.enableWidget());

        GoogleMaps defaultMaps = new GoogleMaps();
        String defaultJson = mapper.writeValueAsString(defaultMaps);
        JsonNode defaultNode = mapper.readTree(defaultJson);
        assertEquals("google_maps", defaultNode.get("type").asText());
        assertFalse(defaultNode.has("latitude"));
        assertFalse(defaultNode.has("longitude"));
        assertFalse(defaultNode.has("enable_widget"));
    }

    @Test
    void testComputerUseToolSafetyPoliciesAndSnakeCase() {
        ComputerUse computerUse = ComputerUse.builder()
            .environment("desktop")
            .excludedPredefinedFunctions("click", "scroll")
            .enablePromptInjectionDetection(true)
            .disabledSafetyPolicies(
                ComputerUse.POLICY_FINANCIAL_TRANSACTIONS,
                ComputerUse.POLICY_COMMUNICATION_TOOL
            )
            .build();

        assertEquals("computer_use", computerUse.type());
        assertEquals("desktop", computerUse.environment());
        assertEquals(List.of("click", "scroll"), computerUse.excludedPredefinedFunctions());
        assertTrue(computerUse.enablePromptInjectionDetection());
        assertEquals(
            List.of("financial_transactions", "communication_tool"),
            computerUse.disabledSafetyPolicies()
        );

        String json = mapper.writeValueAsString(computerUse);
        JsonNode node = mapper.readTree(json);
        assertEquals("computer_use", node.get("type").asText());
        assertEquals("desktop", node.get("environment").asText());
        assertTrue(node.has("excluded_predefined_functions"));
        assertTrue(node.has("disabled_safety_policies"));
        assertEquals("financial_transactions", node.get("disabled_safety_policies").get(0).asText());
        assertEquals("communication_tool", node.get("disabled_safety_policies").get(1).asText());

        // Test deserialization from snake_case and camelCase aliases
        String snakeCasePayload = """
            {
              "type": "computer_use",
              "environment": "mobile",
              "excluded_predefined_functions": ["drag"],
              "enable_prompt_injection_detection": false,
              "disabled_safety_policies": [
                "sensitive_data_modification",
                "account_creation",
                "data_modification",
                "user_consent_management",
                "legal_terms_and_agreements"
              ]
            }
            """;
        ComputerUse fromSnake = assertInstanceOf(ComputerUse.class, mapper.readValue(snakeCasePayload, Tool.class));
        assertEquals("mobile", fromSnake.environment());
        assertEquals(List.of("drag"), fromSnake.excludedPredefinedFunctions());
        assertFalse(fromSnake.enablePromptInjectionDetection());
        assertEquals(5, fromSnake.disabledSafetyPolicies().size());
        assertTrue(fromSnake.disabledSafetyPolicies().contains(ComputerUse.POLICY_USER_CONSENT_MANAGEMENT));
        assertTrue(fromSnake.disabledSafetyPolicies().contains(ComputerUse.POLICY_LEGAL_TERMS_AND_AGREEMENTS));

        String camelCasePayload = """
            {
              "type": "computer_use",
              "environment": "browser",
              "excludedPredefinedFunctions": ["type_text"]
            }
            """;
        ComputerUse fromCamel = assertInstanceOf(ComputerUse.class, mapper.readValue(camelCasePayload, Tool.class));
        assertEquals(List.of("type_text"), fromCamel.excludedPredefinedFunctions());
    }

    @Test
    void testFileCitationPageNumberMediaIdAndMetadata() {
        String json = """
            {
              "type": "file_citation",
              "start_index": 10,
              "end_index": 42,
              "document_uri": "gs://bucket/report.pdf",
              "file_name": "report.pdf",
              "source": "rag_store",
              "page_number": 7,
              "media_id": "img_chart_01",
              "custom_metadata": {
                "department": "finance",
                "year": 2026
              }
            }
            """;

        Content.Annotation annotation = mapper.readValue(json, Content.Annotation.class);
        FileCitation citation = assertInstanceOf(FileCitation.class, annotation);
        assertEquals("file_citation", citation.type());
        assertEquals(10, citation.startIndex());
        assertEquals(42, citation.endIndex());
        assertEquals("gs://bucket/report.pdf", citation.documentUri());
        assertEquals("report.pdf", citation.fileName());
        assertEquals("rag_store", citation.source());
        assertEquals(7, citation.pageNumber());
        assertEquals("img_chart_01", citation.mediaId());
        assertNotNull(citation.customMetadata());
        assertEquals("finance", citation.customMetadata().get("department"));
        assertEquals(2026, citation.customMetadata().get("year"));

        // Backward-compatible 6-arg constructor
        FileCitation legacyCitation = new FileCitation(
            "file_citation", 0, 5, "gs://bucket/doc.pdf", "doc.pdf", "store"
        );
        assertNull(legacyCitation.pageNumber());
        assertNull(legacyCitation.mediaId());
        assertNull(legacyCitation.customMetadata());

        String serialized = mapper.writeValueAsString(citation);
        JsonNode serializedNode = mapper.readTree(serialized);
        assertEquals(7, serializedNode.get("page_number").asInt());
        assertEquals("img_chart_01", serializedNode.get("media_id").asText());
        assertEquals("finance", serializedNode.get("custom_metadata").get("department").asText());
    }

    @Test
    void testGoogleSearchCallAndResultEnhancements() {
        String callStepJson = """
            {
              "type": "google_search_call",
              "id": "search_call_123",
              "arguments": {
                "queries": ["Gemini Interactions API"]
              },
              "search_type": "image_search",
              "signature": "sig_abc_999"
            }
            """;

        Step step = mapper.readValue(callStepJson, Step.class);
        GoogleSearchCallStep searchCall = assertInstanceOf(GoogleSearchCallStep.class, step);
        assertEquals("search_call_123", searchCall.id());
        assertEquals(List.of("Gemini Interactions API"), searchCall.arguments().queries());
        assertEquals("image_search", searchCall.searchType());
        assertEquals("sig_abc_999", searchCall.signature());

        // Backward-compatible constructor
        GoogleSearchCallStep legacyCall = new GoogleSearchCallStep(
            "google_search_call", "id_1", new GoogleSearchCallArguments(List.of("q"))
        );
        assertNull(legacyCall.searchType());
        assertNull(legacyCall.signature());

        // GoogleSearchResult with search_suggestions and snippet alias
        String resultStepJson = """
            {
              "type": "google_search_result",
              "call_id": "search_call_123",
              "signature": "sig_res_456",
              "is_error": false,
              "result": [
                {
                  "url": "https://ai.google.dev",
                  "title": "Google AI for Developers",
                  "snippet": "Build with Gemini models.",
                  "search_suggestions": "<div>Search suggestion widget</div>"
                }
              ]
            }
            """;

        Step resStep = mapper.readValue(resultStepJson, Step.class);
        GoogleSearchResultStep searchResultStep = assertInstanceOf(GoogleSearchResultStep.class, resStep);
        assertEquals(1, searchResultStep.result().size());
        GoogleSearchResult item = searchResultStep.result().get(0);
        assertEquals("https://ai.google.dev", item.url());
        assertEquals("Google AI for Developers", item.title());
        assertEquals("Build with Gemini models.", item.renderedContent());
        assertEquals("<div>Search suggestion widget</div>", item.searchSuggestions());

        // Delta for GoogleSearchCallDelta
        String deltaEventJson = """
            {
              "event_type": "step.delta",
              "event_id": "evt_1",
              "index": 0,
              "delta": {
                "type": "google_search_call",
                "id": "call_1",
                "arguments": {
                  "queries": ["test query"]
                },
                "search_type": "web_search",
                "signature": "delta_sig_1"
              }
            }
            """;
        Events event = mapper.readValue(deltaEventJson, Events.class);
        StepDelta stepDelta = assertInstanceOf(StepDelta.class, event);
        GoogleSearchCallDelta callDelta = assertInstanceOf(GoogleSearchCallDelta.class, stepDelta.delta());
        assertEquals(DeltaType.GOOGLE_SEARCH_CALL, callDelta.type());
        assertEquals("web_search", callDelta.searchType());
        assertEquals("delta_sig_1", callDelta.signature());
    }

    @Test
    @SuppressWarnings("deprecation")
    void testAudioContentAndAudioDeltaSampleRateAndChannels() {
        byte[] dummyAudio = "RIFF....WAVE".getBytes(StandardCharsets.UTF_8);
        AudioContent audio = new AudioContent(dummyAudio, "audio/wav", 24000, 2);

        assertEquals("audio", audio.type());
        assertArrayEquals(dummyAudio, audio.data());
        assertEquals("audio/wav", audio.mimeType());
        assertEquals(24000, audio.sampleRate());
        assertEquals(24000, audio.rate());
        assertEquals(2, audio.channels());

        String json = mapper.writeValueAsString(audio);
        JsonNode node = mapper.readTree(json);
        assertEquals(24000, node.get("sample_rate").asInt());
        assertEquals(2, node.get("channels").asInt());
        assertFalse(node.has("rate"));

        // Deserialization from legacy "rate" field
        String legacyAudioJson = """
            {
              "type": "audio",
              "uri": "https://example.com/audio.wav",
              "mime_type": "audio/wav",
              "rate": 16000,
              "channels": 1
            }
            """;
        AudioContent fromLegacy = assertInstanceOf(AudioContent.class, mapper.readValue(legacyAudioJson, Content.class));
        assertEquals(16000, fromLegacy.sampleRate());
        assertEquals(16000, fromLegacy.rate());
        assertEquals(1, fromLegacy.channels());

        // AudioDelta with sample_rate and channels
        String audioDeltaJson = """
            {
              "event_type": "step.delta",
              "event_id": "evt_audio_1",
              "index": 1,
              "delta": {
                "type": "audio",
                "data": "UklGRg==",
                "mime_type": "audio/l16",
                "sample_rate": 24000,
                "channels": 1
              }
            }
            """;
        Events event = mapper.readValue(audioDeltaJson, Events.class);
        StepDelta stepDelta = assertInstanceOf(StepDelta.class, event);
        AudioDelta audioDelta = assertInstanceOf(AudioDelta.class, stepDelta.delta());
        assertEquals(DeltaType.AUDIO, audioDelta.type());
        assertEquals("audio/l16", audioDelta.mimeType());
        assertEquals(24000, audioDelta.sampleRate());
        assertEquals(24000, audioDelta.rate());
        assertEquals(1, audioDelta.channels());

        // Backward-compatible AudioDelta constructor
        AudioDelta simpleDelta = new AudioDelta(DeltaType.AUDIO, "UklGRg==", null, "audio/wav");
        assertNull(simpleDelta.sampleRate());
        assertNull(simpleDelta.channels());
    }
}
