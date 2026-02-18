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

import io.github.glaforge.gemini.interactions.model.Config.SpeechConfig;
import io.github.glaforge.gemini.interactions.model.Content.AudioContent;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
public class SpeechGenerationTest {

    @Test
    public void testSpeechGeneration() {
        GeminiInteractionsClient client = GeminiInteractionsClient.builder()
            .apiKey(System.getenv("GEMINI_API_KEY"))
            .build();

        ModelInteractionParams request = ModelInteractionParams.builder()
            .model("gemini-2.5-flash-preview-tts")
            .input("Say the following: WOOHOO This is so much fun!")
            .responseModalities(Interaction.Modality.AUDIO)
            .speechConfig(new SpeechConfig("kore", "en-us"))
            .build();

        Interaction interaction = client.create(request);

        assertNotNull(interaction);
        assertNotNull(interaction.outputs());

        boolean hasAudio = interaction.outputs().stream()
            .anyMatch(output -> output instanceof AudioContent);

        assertTrue(hasAudio, "Response should contain audio content");

        interaction.outputs().stream()
            .filter(output -> output instanceof AudioContent)
            .map(output -> (AudioContent) output)
            .forEach(audio -> {
                assertNotNull(audio.data());
                assertTrue(audio.data().length > 0);
                assertEquals("audio", audio.type());
                System.out.println("Received audio data of length: " + audio.data().length);

                try {
                    Path targetPath = Paths.get("target", "generated-audio.wav");
                    Files.createDirectories(targetPath.getParent());

                    // Audio format: 24kHz, 16-bit, Mono, Signed, Little Endian
                    AudioFormat format = new AudioFormat(24000, 16, 1, true, false);
                    byte[] audioData = audio.data();
                    try (AudioInputStream audioInputStream = new AudioInputStream(
                        new ByteArrayInputStream(audioData),
                        format,
                        audioData.length / format.getFrameSize())) {
                        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, targetPath.toFile());
                    }

                    System.out.println("Saved audio to: " + targetPath.toAbsolutePath());
                } catch (IOException e) {
                    fail("Failed to save audio file: " + e.getMessage());
                }
            });
    }
}
