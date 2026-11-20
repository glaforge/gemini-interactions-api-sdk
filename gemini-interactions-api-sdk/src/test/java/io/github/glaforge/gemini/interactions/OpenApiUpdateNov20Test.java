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
import io.github.glaforge.gemini.interactions.model.AudioProcessingConfiguration;
import io.github.glaforge.gemini.interactions.model.Config;
import io.github.glaforge.gemini.interactions.model.Config.GenerationConfig;
import io.github.glaforge.gemini.interactions.model.Config.ThinkingLevel;
import io.github.glaforge.gemini.interactions.model.Content;
import io.github.glaforge.gemini.interactions.model.Content.AudioContent;
import io.github.glaforge.gemini.interactions.model.Content.AudioStaticMediaProcessing;
import io.github.glaforge.gemini.interactions.model.Content.DocumentContent;
import io.github.glaforge.gemini.interactions.model.Content.DocumentStaticMediaProcessing;
import io.github.glaforge.gemini.interactions.model.Content.PageContentLayer;
import io.github.glaforge.gemini.interactions.model.Content.PageProcessingOptions;
import io.github.glaforge.gemini.interactions.model.Content.Resolution;
import io.github.glaforge.gemini.interactions.model.Content.SpreadsheetContentLayer;
import io.github.glaforge.gemini.interactions.model.Content.SpreadsheetProcessingOptions;
import io.github.glaforge.gemini.interactions.model.Content.StaticMediaProcessing;
import io.github.glaforge.gemini.interactions.model.Content.VideoContent;
import io.github.glaforge.gemini.interactions.model.Content.VideoContentLayer;
import io.github.glaforge.gemini.interactions.model.CreateEnvironmentRequest;
import io.github.glaforge.gemini.interactions.model.Credential;
import io.github.glaforge.gemini.interactions.model.CredentialUpdate;
import io.github.glaforge.gemini.interactions.model.DocumentProcessingConfiguration;
import io.github.glaforge.gemini.interactions.model.Environment;
import io.github.glaforge.gemini.interactions.model.EnvironmentConfig;
import io.github.glaforge.gemini.interactions.model.EnvironmentMode;
import io.github.glaforge.gemini.interactions.model.ListEnvironmentsResponse;
import io.github.glaforge.gemini.interactions.model.ModelOption;
import io.github.glaforge.gemini.interactions.model.Trigger;
import io.github.glaforge.gemini.interactions.model.TriggerUpdate;
import io.github.glaforge.gemini.interactions.model.Webhook;
import io.github.glaforge.gemini.interactions.model.WebhookUpdate;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests verifying OpenAPI contract updates: Gemini 4 Hydrogen, ThinkingLevel.XHIGH,
 * Audio/Document/Video media processing options, and self-hosted environments.
 */
class OpenApiUpdateNov20Test {

    private final ObjectMapper mapper = JsonMapper.builder().build();
    private MockWebServer mockServer;
    private GeminiInteractionsClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
        String baseUrl = mockServer.url("").toString();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        client = GeminiInteractionsClient.builder()
            .baseUrl(baseUrl)
            .apiKey("test-api-key")
            .build();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void testGemini4HydrogenAndNeonModelOptions() {
        assertEquals("gemini-4-hydrogen", ModelOption.GEMINI_4_HYDROGEN);
        assertEquals("gemini-4-neon", ModelOption.GEMINI_4_NEON);
        assertEquals("antigravity-preview-latest", ModelOption.ANTIGRAVITY_PREVIEW_LATEST);
        assertEquals("antigravity-preview-latest", AgentOption.ANTIGRAVITY_PREVIEW_LATEST);
    }

    @Test
    void testUpdateMaskOverloadsForWebhookTriggerAndCredential() throws Exception {
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody("{\"id\":\"wh-1\",\"name\":\"Renamed\"}"));
        Webhook webhook = client.updateWebhook("wh-1", new WebhookUpdate("Renamed", null, null, null), "name");
        assertEquals("wh-1", webhook.id());
        RecordedRequest whReq = mockServer.takeRequest();
        assertEquals("PATCH", whReq.getMethod());
        assertEquals("/v1beta/webhooks/wh-1?update_mask=name", whReq.getPath());

        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody("{\"id\":\"tr-1\",\"status\":\"paused\"}"));
        Trigger trigger = client.updateTrigger("tr-1", TriggerUpdate.builder().status(Trigger.Status.PAUSED).build(), "status");
        assertEquals("tr-1", trigger.id());
        RecordedRequest trReq = mockServer.takeRequest();
        assertEquals("PATCH", trReq.getMethod());
        assertEquals("/v1beta/triggers/tr-1?update_mask=status", trReq.getPath());

        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody("{\"id\":\"cred-1\",\"type\":\"bearer_token\",\"status\":\"active\"}"));
        Credential cred = client.updateCredential("cred-1", CredentialUpdate.ofBearerToken("new-secret"), "bearer_token");
        assertEquals("cred-1", cred.id());
        RecordedRequest credReq = mockServer.takeRequest();
        assertEquals("PATCH", credReq.getMethod());
        assertEquals("/v1beta/credentials/cred-1?update_mask=bearer_token", credReq.getPath());
    }

    @Test
    void testThinkingLevelXhighSerializationAndDeserialization() {
        GenerationConfig config = GenerationConfig.builder()
            .thinkingLevel(ThinkingLevel.XHIGH)
            .build();

        String json = mapper.writeValueAsString(config);
        JsonNode node = mapper.readTree(json);
        assertEquals("xhigh", node.get("thinking_level").asText());

        GenerationConfig parsed = mapper.readValue(json, GenerationConfig.class);
        assertEquals(ThinkingLevel.XHIGH, parsed.thinkingLevel());
    }

    @Test
    void testVideoStaticMediaProcessingContentLayers() {
        StaticMediaProcessing processing = new StaticMediaProcessing(
            "10.5s",
            "30s",
            2.0,
            List.of(VideoContentLayer.FRAMES, VideoContentLayer.AUDIO, VideoContentLayer.TRANSCRIPT)
        );
        VideoContent video = new VideoContent(
            "video",
            null,
            "https://example.com/demo.mp4",
            "video/mp4",
            Resolution.HIGH,
            processing
        );

        String json = mapper.writeValueAsString(video);
        JsonNode node = mapper.readTree(json);
        JsonNode procNode = node.get("processing");
        assertEquals("static", procNode.get("type").asText());
        assertEquals("10.5s", procNode.get("start_offset").asText());
        assertEquals("30s", procNode.get("end_offset").asText());
        assertEquals(2.0, procNode.get("fps").asDouble());
        assertEquals(3, procNode.get("content_layers").size());
        assertEquals("frames", procNode.get("content_layers").get(0).asText());
        assertEquals("audio", procNode.get("content_layers").get(1).asText());
        assertEquals("transcript", procNode.get("content_layers").get(2).asText());

        Content deserialized = mapper.readValue(json, Content.class);
        VideoContent parsedVideo = assertInstanceOf(VideoContent.class, deserialized);
        assertNotNull(parsedVideo.processing());
        assertTrue(parsedVideo.processing().isCustom());
        StaticMediaProcessing parsedProc = assertInstanceOf(StaticMediaProcessing.class, parsedVideo.processing().config());
        assertEquals(
            List.of(VideoContentLayer.FRAMES, VideoContentLayer.AUDIO, VideoContentLayer.TRANSCRIPT),
            parsedProc.contentLayers()
        );

        // Backward-compatible 3-arg constructor omits content_layers
        StaticMediaProcessing legacy = new StaticMediaProcessing("0s", "10s", 1.0);
        JsonNode legacyNode = mapper.readTree(mapper.writeValueAsString(legacy));
        assertFalse(legacyNode.has("content_layers"));
    }

    @Test
    void testAudioContentProcessingCustomAndPreset() {
        AudioStaticMediaProcessing audioProc = new AudioStaticMediaProcessing("5.5s", "45s");
        AudioContent audio = new AudioContent(
            "https://example.com/clip.mp3",
            "audio/mpeg",
            audioProc
        );

        String json = mapper.writeValueAsString(audio);
        JsonNode node = mapper.readTree(json);
        assertEquals("audio", node.get("type").asText());
        assertEquals("https://example.com/clip.mp3", node.get("uri").asText());
        JsonNode procNode = node.get("processing");
        assertEquals("static", procNode.get("type").asText());
        assertEquals("5.5s", procNode.get("start_offset").asText());
        assertEquals("45s", procNode.get("end_offset").asText());

        AudioContent parsed = assertInstanceOf(AudioContent.class, mapper.readValue(json, Content.class));
        assertNotNull(parsed.processing());
        assertTrue(parsed.processing().isCustom());
        AudioStaticMediaProcessing parsedProc = assertInstanceOf(AudioStaticMediaProcessing.class, parsed.processing().config());
        assertEquals("static", parsedProc.type());
        assertEquals("5.5s", parsedProc.startOffset());
        assertEquals("45s", parsedProc.endOffset());

        // String preset "static"
        byte[] sampleBytes = "RIFF".getBytes(StandardCharsets.UTF_8);
        AudioContent presetAudio = new AudioContent(sampleBytes, "audio/wav", AudioProcessingConfiguration.staticPreset());
        String presetJson = mapper.writeValueAsString(presetAudio);
        JsonNode presetNode = mapper.readTree(presetJson);
        assertEquals("static", presetNode.get("processing").asText());

        AudioContent parsedPreset = assertInstanceOf(AudioContent.class, mapper.readValue(presetJson, Content.class));
        assertTrue(parsedPreset.processing().isPreset());
        assertEquals("static", parsedPreset.processing().preset());

        // Backward-compatible constructor omits processing
        AudioContent legacy = new AudioContent(sampleBytes, "audio/wav");
        assertNull(legacy.processing());
        assertFalse(mapper.readTree(mapper.writeValueAsString(legacy)).has("processing"));
    }

    @Test
    void testDocumentContentPageAndSpreadsheetProcessing() {
        PageProcessingOptions pageOptions = PageProcessingOptions.builder()
            .startPage(1)
            .endPage(5)
            .contentLayers(PageContentLayer.PAGE_IMAGE, PageContentLayer.PAGE_TEXT)
            .build();
        DocumentContent pdfDoc = new DocumentContent(
            "https://example.com/report.pdf",
            DocumentContent.APPLICATION_PDF,
            Resolution.HIGH,
            DocumentStaticMediaProcessing.forPages(pageOptions)
        );

        String pdfJson = mapper.writeValueAsString(pdfDoc);
        JsonNode pdfNode = mapper.readTree(pdfJson);
        assertEquals("document", pdfNode.get("type").asText());
        assertEquals("high", pdfNode.get("resolution").asText());
        JsonNode pageNode = pdfNode.get("processing").get("page_options");
        assertEquals("static", pdfNode.get("processing").get("type").asText());
        assertEquals(1, pageNode.get("start_page").asInt());
        assertEquals(5, pageNode.get("end_page").asInt());
        assertEquals("page_image", pageNode.get("content_layers").get(0).asText());
        assertEquals("page_text", pageNode.get("content_layers").get(1).asText());

        DocumentContent parsedPdf = assertInstanceOf(DocumentContent.class, mapper.readValue(pdfJson, Content.class));
        assertEquals(Resolution.HIGH, parsedPdf.resolution());
        assertTrue(parsedPdf.processing().isCustom());
        DocumentStaticMediaProcessing parsedPdfProc = assertInstanceOf(
            DocumentStaticMediaProcessing.class,
            parsedPdf.processing().config()
        );
        assertEquals(1, parsedPdfProc.pageOptions().startPage());
        assertEquals(5, parsedPdfProc.pageOptions().endPage());
        assertEquals(
            List.of(PageContentLayer.PAGE_IMAGE, PageContentLayer.PAGE_TEXT),
            parsedPdfProc.pageOptions().contentLayers()
        );

        // Spreadsheet processing options
        SpreadsheetProcessingOptions sheetOptions = SpreadsheetProcessingOptions.builder()
            .visibleCells("A1:D10")
            .sheetIndex(2)
            .contentLayers(SpreadsheetContentLayer.VISUAL, SpreadsheetContentLayer.DATA, SpreadsheetContentLayer.FORMULAS)
            .build();
        byte[] xlsxBytes = "PK".getBytes(StandardCharsets.UTF_8);
        DocumentContent sheetDoc = new DocumentContent(
            xlsxBytes,
            DocumentContent.APPLICATION_XLSX,
            Resolution.MEDIUM,
            DocumentStaticMediaProcessing.forSpreadsheet(sheetOptions)
        );

        String sheetJson = mapper.writeValueAsString(sheetDoc);
        JsonNode sheetNode = mapper.readTree(sheetJson);
        JsonNode sheetOptNode = sheetNode.get("processing").get("spreadsheet_options");
        assertEquals("A1:D10", sheetOptNode.get("visible_cells").asText());
        assertEquals(2, sheetOptNode.get("sheet_index").asInt());
        assertEquals("visual", sheetOptNode.get("content_layers").get(0).asText());
        assertEquals("data", sheetOptNode.get("content_layers").get(1).asText());
        assertEquals("formulas", sheetOptNode.get("content_layers").get(2).asText());

        DocumentContent parsedSheet = assertInstanceOf(DocumentContent.class, mapper.readValue(sheetJson, Content.class));
        DocumentStaticMediaProcessing parsedSheetProc = assertInstanceOf(
            DocumentStaticMediaProcessing.class,
            parsedSheet.processing().config()
        );
        assertEquals("A1:D10", parsedSheetProc.spreadsheetOptions().visibleCells());
        assertEquals(2, parsedSheetProc.spreadsheetOptions().sheetIndex());
        assertEquals(
            List.of(SpreadsheetContentLayer.VISUAL, SpreadsheetContentLayer.DATA, SpreadsheetContentLayer.FORMULAS),
            parsedSheetProc.spreadsheetOptions().contentLayers()
        );

        // Preset string "static"
        DocumentContent presetDoc = new DocumentContent(
            "https://example.com/notes.pdf",
            DocumentContent.APPLICATION_PDF,
            null,
            DocumentProcessingConfiguration.staticPreset()
        );
        String presetJson = mapper.writeValueAsString(presetDoc);
        assertEquals("static", mapper.readTree(presetJson).get("processing").asText());
        DocumentContent parsedPreset = assertInstanceOf(DocumentContent.class, mapper.readValue(presetJson, Content.class));
        assertTrue(parsedPreset.processing().isPreset());
        assertEquals("static", parsedPreset.processing().preset());

        // Backward-compatible constructor omits resolution and processing
        DocumentContent legacy = new DocumentContent("https://example.com/legacy.pdf", DocumentContent.APPLICATION_PDF);
        JsonNode legacyNode = mapper.readTree(mapper.writeValueAsString(legacy));
        assertFalse(legacyNode.has("resolution"));
        assertFalse(legacyNode.has("processing"));
    }

    @Test
    void testSelfHostedEnvironmentModelsAndEnum() {
        assertEquals("on_demand", EnvironmentMode.ON_DEMAND.value());
        assertEquals("self_hosted", EnvironmentMode.SELF_HOSTED.value());
        assertEquals(EnvironmentMode.ON_DEMAND, EnvironmentMode.fromValue("on_demand"));
        assertEquals(EnvironmentMode.SELF_HOSTED, EnvironmentMode.fromValue("self_hosted"));
        assertNull(EnvironmentMode.fromValue(null));
        assertThrows(IllegalArgumentException.class, () -> EnvironmentMode.fromValue("invalid_mode"));

        CreateEnvironmentRequest req = CreateEnvironmentRequest.builder()
            .mode(EnvironmentMode.SELF_HOSTED)
            .customConfig("runner", "k8s-pool-1")
            .customConfig("maxConcurrency", 4)
            .systemInstruction("Use the internal tooling in /opt/corp.")
            .build();

        String reqJson = mapper.writeValueAsString(req);
        JsonNode reqNode = mapper.readTree(reqJson);
        assertEquals("self_hosted", reqNode.get("mode").asText());
        assertEquals("k8s-pool-1", reqNode.get("custom_config").get("runner").asText());
        assertEquals(4, reqNode.get("custom_config").get("maxConcurrency").asInt());
        assertEquals("Use the internal tooling in /opt/corp.", reqNode.get("system_instruction").asText());

        CreateEnvironmentRequest parsedReq = mapper.readValue(reqJson, CreateEnvironmentRequest.class);
        assertEquals(EnvironmentMode.SELF_HOSTED, parsedReq.mode());
        assertEquals("k8s-pool-1", parsedReq.customConfig().get("runner"));
        assertEquals("Use the internal tooling in /opt/corp.", parsedReq.systemInstruction());

        EnvironmentConfig envConfig = EnvironmentConfig.builder()
            .mode(EnvironmentMode.SELF_HOSTED)
            .customConfig("region", "eu-west-1")
            .build();

        String cfgJson = mapper.writeValueAsString(envConfig);
        JsonNode cfgNode = mapper.readTree(cfgJson);
        assertEquals("remote", cfgNode.get("type").asText());
        assertEquals("self_hosted", cfgNode.get("mode").asText());
        assertEquals("eu-west-1", cfgNode.get("custom_config").get("region").asText());

        EnvironmentConfig parsedCfg = mapper.readValue(cfgJson, EnvironmentConfig.class);
        assertEquals(EnvironmentMode.SELF_HOSTED, parsedCfg.mode());
        assertEquals("eu-west-1", parsedCfg.customConfig().get("region"));

        Environment env = Environment.builder()
            .id("env_self_123")
            .status(Environment.Status.ACTIVE)
            .mode(EnvironmentMode.SELF_HOSTED)
            .customConfig(Map.of("cluster", "prod"))
            .environmentKey("secret-daemon-key")
            .build();

        String envJson = mapper.writeValueAsString(env);
        JsonNode envNode = mapper.readTree(envJson);
        assertEquals("self_hosted", envNode.get("mode").asText());
        assertEquals("prod", envNode.get("custom_config").get("cluster").asText());
        assertEquals("secret-daemon-key", envNode.get("environment_key").asText());

        Environment parsedEnv = mapper.readValue(envJson, Environment.class);
        assertEquals(EnvironmentMode.SELF_HOSTED, parsedEnv.mode());
        assertEquals("prod", parsedEnv.customConfig().get("cluster"));
        assertEquals("secret-daemon-key", parsedEnv.environmentKey());
    }

    @Test
    void testClientListEnvironmentsWithModeFilterAndCreateSelfHosted() throws Exception {
        // Test createEnvironment returning environment_key
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody("""
                {
                  "id": "env_sh_01",
                  "status": "active",
                  "mode": "self_hosted",
                  "custom_config": {"pool": "gpu"},
                  "environment_key": "ek_secret_token"
                }
                """));

        Environment created = client.createEnvironment(CreateEnvironmentRequest.builder()
            .mode(EnvironmentMode.SELF_HOSTED)
            .customConfig("pool", "gpu")
            .systemInstruction("Run on self-hosted runner.")
            .build());

        assertEquals("env_sh_01", created.id());
        assertEquals(EnvironmentMode.SELF_HOSTED, created.mode());
        assertEquals("ek_secret_token", created.environmentKey());
        assertEquals("gpu", created.customConfig().get("pool"));

        RecordedRequest createReq = mockServer.takeRequest();
        assertEquals("POST", createReq.getMethod());
        assertEquals("/v1beta/environments", createReq.getPath());
        JsonNode sentBody = mapper.readTree(createReq.getBody().readUtf8());
        assertEquals("self_hosted", sentBody.get("mode").asText());
        assertEquals("Run on self-hosted runner.", sentBody.get("system_instruction").asText());

        // Test listEnvironments(EnvironmentMode)
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody("""
                {
                  "environments": [
                    {
                      "id": "env_sh_01",
                      "status": "active",
                      "mode": "self_hosted"
                    }
                  ]
                }
                """));

        ListEnvironmentsResponse filtered = client.listEnvironments(EnvironmentMode.SELF_HOSTED);
        assertEquals(1, filtered.environments().size());
        assertEquals(EnvironmentMode.SELF_HOSTED, filtered.environments().get(0).mode());

        RecordedRequest listReq1 = mockServer.takeRequest();
        assertEquals("GET", listReq1.getMethod());
        assertEquals("/v1beta/environments?mode=self_hosted", listReq1.getPath());

        // Test listEnvironments(pageSize, pageToken, mode)
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody("""
                {
                  "environments": []
                }
                """));

        client.listEnvironments(25, "next_tok", EnvironmentMode.ON_DEMAND);
        RecordedRequest listReq2 = mockServer.takeRequest();
        assertEquals("GET", listReq2.getMethod());
        assertEquals("/v1beta/environments?page_size=25&page_token=next_tok&mode=on_demand", listReq2.getPath());
    }
}
