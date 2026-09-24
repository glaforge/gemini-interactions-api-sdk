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

import io.github.glaforge.gemini.interactions.model.AudioData;
import io.github.glaforge.gemini.interactions.model.Config;
import io.github.glaforge.gemini.interactions.model.Content;
import io.github.glaforge.gemini.interactions.model.CreateVoiceRequest;
import io.github.glaforge.gemini.interactions.model.ListVoicesResponse;
import io.github.glaforge.gemini.interactions.model.ModelOption;
import io.github.glaforge.gemini.interactions.model.Pitch;
import io.github.glaforge.gemini.interactions.model.PromptedVoice;
import io.github.glaforge.gemini.interactions.model.ReplicatedVoice;
import io.github.glaforge.gemini.interactions.model.SpeechConfiguration;
import io.github.glaforge.gemini.interactions.model.Voice;
import io.github.glaforge.gemini.interactions.model.VoiceListFilter;
import io.github.glaforge.gemini.interactions.model.VoiceType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VoiceTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();

    @Test
    void testAudioDataByteConversion() {
        byte[] original = "hello audio bytes".getBytes(StandardCharsets.UTF_8);
        AudioData audioData = AudioData.of("audio/wav", original);

        assertEquals("audio/wav", audioData.mimeType());
        assertNotNull(audioData.data());
        assertArrayEquals(original, audioData.decodedBytes());
    }

    @Test
    void testPromptedVoiceSerialization() throws Exception {
        Voice voice = Voice.prompted(
            ModelOption.GEMINI_3_8_FLASH_TTS,
            "Warm Astronomer",
            "A warm, thoughtful astronomer in his late 60s with a gentle British accent."
        );

        CreateVoiceRequest request = CreateVoiceRequest.of(voice, true);
        String json = mapper.writeValueAsString(request);

        assertTrue(json.contains("\"type\":\"prompted\""));
        assertTrue(json.contains("\"store\":true"));
        assertTrue(json.contains("\"gemini-3.8-flash-tts\""));
        assertTrue(json.contains("astronomer"));

        CreateVoiceRequest deserialized = mapper.readValue(json, CreateVoiceRequest.class);
        assertEquals(VoiceType.PROMPTED, deserialized.voice().type());
        assertEquals("Warm Astronomer", deserialized.voice().displayName());
        assertEquals(ModelOption.GEMINI_3_8_FLASH_TTS, deserialized.voice().model());
        assertEquals("A warm, thoughtful astronomer in his late 60s with a gentle British accent.", deserialized.voice().prompted().input());
        assertTrue(deserialized.store());
    }

    @Test
    void testReplicatedVoiceSerialization() throws Exception {
        AudioData source = AudioData.of("audio/wav", "sourceB64");
        AudioData consent = AudioData.of("audio/wav", "consentB64");

        Voice voice = Voice.replicated(
            ModelOption.GEMINI_3_8_FLASH_LITE_TTS,
            "Cloned Speaker",
            source,
            consent
        );

        CreateVoiceRequest request = CreateVoiceRequest.of(voice, false);
        String json = mapper.writeValueAsString(request);

        assertTrue(json.contains("\"type\":\"replicated\""));
        assertTrue(json.contains("\"store\":false"));
        assertTrue(json.contains("\"source_audio\""));
        assertTrue(json.contains("\"consent_audio\""));

        CreateVoiceRequest deserialized = mapper.readValue(json, CreateVoiceRequest.class);
        assertEquals(VoiceType.REPLICATED, deserialized.voice().type());
        assertEquals("sourceB64", deserialized.voice().replicated().sourceAudio().data());
        assertEquals("consentB64", deserialized.voice().replicated().consentAudio().data());
    }

    @Test
    void testVoiceListResponseDeserialization() throws Exception {
        String json = """
            {
              "voices": [
                {
                  "id": "voice_123",
                  "name": "voices/voice_123",
                  "type": "prompted",
                  "display_name": "Warm Astronomer",
                  "language_code": "en-GB",
                  "pitch": "medium",
                  "sample_audio": {
                    "mime_type": "audio/wav",
                    "data": "sampleB64"
                  }
                },
                {
                  "id": "Puck",
                  "type": "prebuilt",
                  "display_name": "Puck",
                  "language_code": "en-US",
                  "pitch": "low"
                }
              ],
              "next_page_token": "next_token_xyz"
            }
            """;

        ListVoicesResponse response = mapper.readValue(json, ListVoicesResponse.class);
        assertNotNull(response);
        assertEquals(2, response.voices().size());
        assertEquals("next_token_xyz", response.nextPageToken());

        Voice v1 = response.voices().get(0);
        assertEquals("voice_123", v1.id());
        assertEquals(VoiceType.PROMPTED, v1.type());
        assertEquals(Pitch.MEDIUM, v1.pitch());
        assertNotNull(v1.sampleAudio());
        assertEquals("audio/wav", v1.sampleAudio().mimeType());

        Voice v2 = response.voices().get(1);
        assertEquals("Puck", v2.id());
        assertEquals(VoiceType.PREBUILT, v2.type());
        assertEquals(Pitch.LOW, v2.pitch());
    }

    @Test
    void testVoiceListFilterQueryParams() {
        VoiceListFilter filter = VoiceListFilter.builder()
            .types(VoiceType.PROMPTED, VoiceType.PREBUILT)
            .languageCodes("en-US", "en-GB")
            .genders("female")
            .pitches(Pitch.MEDIUM)
            .contexts("Audiobook", "Conversational")
            .search("warm")
            .pageSize(50)
            .pageToken("page_1")
            .build();

        StringBuilder sb = new StringBuilder("https://api.example.com/v1beta/voices");
        filter.appendQueryParams(sb);
        String url = sb.toString();

        assertTrue(url.contains("type=prompted"));
        assertTrue(url.contains("type=prebuilt"));
        assertTrue(url.contains("language_code=en-US"));
        assertTrue(url.contains("language_code=en-GB"));
        assertTrue(url.contains("gender=female"));
        assertTrue(url.contains("pitch=medium"));
        assertTrue(url.contains("context=Audiobook"));
        assertTrue(url.contains("context=Conversational"));
        assertTrue(url.contains("search=warm"));
        assertTrue(url.contains("page_size=50"));
        assertTrue(url.contains("page_token=page_1"));
    }

    @Test
    void testSpeechAnnotationSerializationAndDeserialization() throws Exception {
        Content.TextContent content = Content.speech(
            "Look out past the rings of Saturn.",
            "reflective and awe-inspired"
        );

        String json = mapper.writeValueAsString(content);
        assertTrue(json.contains("\"type\":\"text\""));
        assertTrue(json.contains("\"speech_metadata\""));
        assertTrue(json.contains("\"reflective and awe-inspired\""));

        Content deserialized = mapper.readValue(json, Content.class);
        assertTrue(deserialized instanceof Content.TextContent);
        Content.TextContent textContent = (Content.TextContent) deserialized;
        assertEquals("Look out past the rings of Saturn.", textContent.text());
        assertNotNull(textContent.annotations());
        assertEquals(1, textContent.annotations().size());

        Content.Annotation annotation = textContent.annotations().get(0);
        assertTrue(annotation instanceof Content.SpeechAnnotation);
        Content.SpeechAnnotation speech = (Content.SpeechAnnotation) annotation;
        assertEquals("reflective and awe-inspired", speech.style());
    }

    @Test
    void testMultiSpeakerWithCadenceMode() throws Exception {
        Config.SpeechConfig joe = new Config.SpeechConfig("Puck", "en-US", "Joe");
        Config.SpeechConfig jane = new Config.SpeechConfig("voice_custom_jane", "en-US", "Jane");

        Config.SpeakerConfig speakerConfig = Config.SpeakerConfig.conversational(joe, jane);
        assertEquals("conversational", speakerConfig.mode());
        assertEquals(2, speakerConfig.speakers().size());

        SpeechConfiguration speechConfig = SpeechConfiguration.of(speakerConfig);
        assertTrue(speechConfig.isMultiSpeaker());

        String json = mapper.writeValueAsString(speechConfig);
        assertTrue(json.contains("\"mode\":\"conversational\""));
        assertTrue(json.contains("\"speakers\""));
        assertTrue(json.contains("voice_custom_jane"));

        SpeechConfiguration deserialized = mapper.readValue(json, SpeechConfiguration.class);
        assertTrue(deserialized.isMultiSpeaker());
        assertEquals("conversational", deserialized.multiSpeakerConfig().mode());
        assertEquals(2, deserialized.multiSpeakerConfig().speakers().size());
        assertEquals("voice_custom_jane", deserialized.multiSpeakerConfig().speakers().get(1).voice());
    }
}
