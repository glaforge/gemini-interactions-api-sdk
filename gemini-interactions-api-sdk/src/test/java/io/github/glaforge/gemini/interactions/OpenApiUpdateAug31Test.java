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

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import io.github.glaforge.gemini.interactions.model.*;
import io.github.glaforge.gemini.interactions.model.Config.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiUpdateAug31Test {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder().build();
    }

    @Test
    void testModelOptionConstants() {
        assertEquals("lyria-3-clip-preview", ModelOption.LYRIA_3_CLIP_PREVIEW);
        assertEquals("lyria-3-pro-preview", ModelOption.LYRIA_3_PRO_PREVIEW);
        assertEquals("gemini-robotics-er-1.6-preview", ModelOption.GEMINI_ROBOTICS_ER_1_6_PREVIEW);
        assertEquals("gemini-robotics-er-2-preview", ModelOption.GEMINI_ROBOTICS_ER_2_PREVIEW);
    }

    @Test
    void testTranscriptionModePresetSerializationAndDeserialization() throws Exception {
        TranscriptionConfig config = TranscriptionConfig.builder()
            .mode("verbatim")
            .build();

        String json = mapper.writeValueAsString(config);
        assertTrue(json.contains("\"mode\":\"verbatim\""));

        TranscriptionConfig deserialized = mapper.readValue(json, TranscriptionConfig.class);
        assertNotNull(deserialized.mode());
        assertTrue(deserialized.mode().isPreset());
        assertFalse(deserialized.mode().isCustom());
        assertEquals("verbatim", deserialized.mode().preset());
    }

    @Test
    void testTranscriptionModeSmartObjectSerializationAndDeserialization() throws Exception {
        TranscriptionConfig config = TranscriptionConfig.builder()
            .mode(new SmartTranscriptionMode())
            .build();

        String json = mapper.writeValueAsString(config);
        assertTrue(json.contains("\"mode\":{\"type\":\"smart\"}"));

        TranscriptionConfig deserialized = mapper.readValue(json, TranscriptionConfig.class);
        assertNotNull(deserialized.mode());
        assertTrue(deserialized.mode().isCustom());
        assertFalse(deserialized.mode().isPreset());
        assertTrue(deserialized.mode().config() instanceof SmartTranscriptionMode);
        assertEquals("smart", ((SmartTranscriptionMode) deserialized.mode().config()).type());
    }

    @Test
    void testTranscriptionModeVerbatimObjectSerializationAndDeserialization() throws Exception {
        TranscriptionConfig config = TranscriptionConfig.builder()
            .mode(new VerbatimTranscriptionMode("speaker", List.of("word")))
            .build();

        String json = mapper.writeValueAsString(config);
        assertTrue(json.contains("\"diarization_mode\":\"speaker\""));
        assertTrue(json.contains("\"timestamp_granularities\":[\"word\"]"));

        TranscriptionConfig deserialized = mapper.readValue(json, TranscriptionConfig.class);
        assertNotNull(deserialized.mode());
        assertTrue(deserialized.mode().isCustom());
        VerbatimTranscriptionMode verbatim = (VerbatimTranscriptionMode) deserialized.mode().config();
        assertEquals("speaker", verbatim.diarizationMode());
        assertEquals(List.of("word"), verbatim.timestampGranularities());
        assertEquals("verbatim", verbatim.type());
    }

    @Test
    void testGetEnvironmentFilesResponseDeserialization() throws Exception {
        String json = """
            {
              "files": [
                {
                  "name": "src",
                  "path": "workspace/src",
                  "type": "directory",
                  "size_bytes": 4096,
                  "mime_type": ""
                },
                {
                  "name": "main.py",
                  "path": "workspace/src/main.py",
                  "type": "file",
                  "size_bytes": 1024,
                  "mime_type": "text/python"
                }
              ],
              "next_page_token": "token-123"
            }
            """;

        GetEnvironmentFilesResponse response = mapper.readValue(json, GetEnvironmentFilesResponse.class);
        assertNotNull(response);
        assertEquals(2, response.files().size());
        assertEquals("token-123", response.nextPageToken());

        EnvironmentFile dir = response.files().get(0);
        assertTrue(dir.isDirectory());
        assertFalse(dir.isFile());
        assertEquals("src", dir.name());

        EnvironmentFile file = response.files().get(1);
        assertTrue(file.isFile());
        assertFalse(file.isDirectory());
        assertEquals("main.py", file.name());
        assertEquals(1024L, file.sizeBytes());
        assertEquals("text/python", file.mimeType());
    }
}
