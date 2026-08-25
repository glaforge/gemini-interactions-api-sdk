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

import io.github.glaforge.gemini.interactions.model.Config;
import io.github.glaforge.gemini.interactions.model.ModelOption;
import io.github.glaforge.gemini.interactions.model.SpeechConfiguration;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenApiUpdate37Test {

    private final ObjectMapper mapper = JsonMapper.builder().build();

    @Test
    void testModelOptionGemini37FlashConstant() {
        assertEquals("gemini-3.7-flash", ModelOption.GEMINI_3_7_FLASH);
    }

    @Test
    void testSpeakerConfigSerializationAndDeserialization() throws Exception {
        Config.SpeechConfig speech1 = new Config.SpeechConfig("Algenib", "en-US", "speaker1");
        Config.SpeechConfig speech2 = new Config.SpeechConfig("Kore", "en-US", "speaker2");
        Config.SpeakerConfig speakerConfig = new Config.SpeakerConfig(List.of(speech1, speech2));

        Config.GenerationConfig genConfig = Config.GenerationConfig.builder()
            .speechConfig(speakerConfig)
            .build();

        String json = mapper.writeValueAsString(genConfig);
        assertTrue(json.contains("\"speech_config\""));
        assertTrue(json.contains("\"speakers\""));
        assertTrue(json.contains("\"voice\":\"Algenib\""));

        Config.GenerationConfig deserialized = mapper.readValue(json, Config.GenerationConfig.class);
        assertNotNull(deserialized.speechConfig());
        SpeechConfiguration speechConfig = deserialized.speechConfig();
        assertTrue(speechConfig.isMultiSpeaker());
        assertFalse(speechConfig.isSingleSpeaker());
        assertEquals(2, speechConfig.multiSpeakerConfig().speakers().size());
        assertEquals("Algenib", speechConfig.multiSpeakerConfig().speakers().get(0).voice());
    }

    @Test
    void testListSpeechConfigDeserialization() throws Exception {
        String json = """
            {
                "speech_config": [
                    {"voice": "Algenib", "language": "en-GB"}
                ]
            }
            """;

        Config.GenerationConfig deserialized = mapper.readValue(json, Config.GenerationConfig.class);
        assertNotNull(deserialized.speechConfig());
        SpeechConfiguration speechConfig = deserialized.speechConfig();
        assertTrue(speechConfig.isSingleSpeaker());
        assertFalse(speechConfig.isMultiSpeaker());
        List<Config.SpeechConfig> list = speechConfig.singleSpeakerConfigs();
        assertEquals(1, list.size());
        assertEquals("Algenib", list.get(0).voice());
    }

    @Test
    void testVideoResponseFormatWithResolutionAndGcsUri() throws Exception {
        Config.VideoResponseFormat format = new Config.VideoResponseFormat("gs://my-bucket/output.mp4", "1080p");
        String json = mapper.writeValueAsString(format);
        assertTrue(json.contains("\"type\":\"video\""));
        assertTrue(json.contains("\"gcs_uri\":\"gs://my-bucket/output.mp4\""));
        assertTrue(json.contains("\"resolution\":\"1080p\""));

        Config.VideoResponseFormat deserialized = mapper.readValue(json, Config.VideoResponseFormat.class);
        assertEquals("video", deserialized.type());
        assertEquals("gs://my-bucket/output.mp4", deserialized.gcsUri());
        assertEquals("1080p", deserialized.resolution());
    }
}
