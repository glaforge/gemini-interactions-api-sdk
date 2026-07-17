package io.github.glaforge.gemini.interactions;

import io.github.glaforge.gemini.interactions.model.Config.GenerationConfig;
import io.github.glaforge.gemini.interactions.model.Config.SpeechConfig;
import java.util.List;
import io.github.glaforge.gemini.interactions.model.Content;
import io.github.glaforge.gemini.interactions.model.Events;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.Step;

import org.junit.jupiter.api.Test;
import java.util.stream.Stream;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import java.util.Base64;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.fail;

@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
@DisabledIfEnvironmentVariable(named = "GITHUB_ACTIONS", matches = "true")
public class Gemini31SpeechGenerationTest {

    @Test
    public void testSpeechGenerationWithTagsAndContext() throws Exception {
        GeminiInteractionsClient client = GeminiInteractionsClient.builder()
                .apiKey(System.getenv("GEMINI_API_KEY"))
                .build();

        String prompt = """
                # AUDIO PROFILE: Jaz R.
                ## THE SCENE: The London Studio
                It is 10:00 PM in a glass-walled studio overlooking the moonlit London skyline, but inside, it is blindingly bright. The red "ON AIR" tally light is blazing. Jaz is standing up, not sitting, bouncing on the balls of their heels to the rhythm of a thumping backing track. Their hands fly across the faders on a massive mixing desk. It is a chaotic, caffeine-fueled cockpit designed to wake up an entire nation.

                ### DIRECTOR'S NOTES
                Style:
                * The "Vocal Smile": You must hear the grin in the audio. The soft palate is always raised to keep the tone bright, sunny, and explicitly inviting.
                * Dynamics: High projection without shouting. Punchy consonants and elongated vowels on excitement words.
                Accent: Jaz is a DJ from Brixton, London
                Pace: Speaks at an energetic pace, keeping up with the fast music. Speaks with a "bouncing" cadence. High-speed delivery with fluid transitions—no dead air, no gaps.

                ### SAMPLE CONTEXT
                Jaz is the industry standard for Top 40 radio, high-octane event promos, or any script that requires a charismatic Estuary accent and 11/10 infectious energy.

                #### TRANSCRIPT
                [excitedly] Yes, massive vibes in the studio! You are locked in and it is absolutely popping off in London right now. If you're stuck on the tube, or just sat there pretending to work... stop it. Seriously, I see you. [shouting] Turn this up! We've got the project roadmap landing in three, two... let's go!
                """;

        ModelInteractionParams request = ModelInteractionParams.builder()
                .model("gemini-3.1-flash-tts-preview")
                .input(prompt)
                .responseModalities(Interaction.Modality.AUDIO)
                .generationConfig(GenerationConfig.builder().speechConfig(List.of(new SpeechConfig("Algenib", "en-GB"))).build())
                .stream(true)
                .build();

        try (Stream<Events> eventStream = client.stream(request)) {
            // Audio format: 24kHz, 16-bit, Mono, Signed, Little Endian
            AudioFormat format = new AudioFormat(24000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                line.open(format);
                line.start();
                System.out.println("Streaming audio playback in real-time...");

                eventStream.forEach(event -> {
                    System.out.println("Received event: " + event.getClass().getSimpleName());

                    // Handle streaming deltas
                    if (event instanceof Events.StepDelta cd && cd.delta() instanceof Events.AudioDelta audioDelta) {
                        byte[] audioData = Base64.getDecoder().decode(audioDelta.data());
                        line.write(audioData, 0, audioData.length);
                        try {
                            outputStream.write(audioData);
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to write audio data to buffer", e);
                        }
                    }

                    // Check StepStart
                    if (event instanceof Events.StepStart startEvent) {
                        if (startEvent.step() instanceof Step.ModelOutputStep out) {
                            if (out.content() != null) {
                                out.content().forEach(content -> {
                                    if (content instanceof Content.AudioContent audioContent) {
                                        line.write(audioContent.data(), 0, audioContent.data().length);
                                        try {
                                            outputStream.write(audioContent.data());
                                        } catch (IOException e) {
                                            throw new RuntimeException("Failed to write audio data to buffer", e);
                                        }
                                    }
                                });
                            }
                        }
                    }

                    // Check StepStop - actually wait, StepStop doesn't have Step object, just index
                    // Events.StepStop only has index according to Events.java
                });

                line.drain();
                System.out.println("Playback complete.");

                try {
                    Path targetPath = Paths.get("target", "gemini-3.1-streaming-audio.wav");
                    Files.createDirectories(targetPath.getParent());
                    byte[] fullAudioData = outputStream.toByteArray();

                    try (AudioInputStream audioInputStream = new AudioInputStream(
                            new ByteArrayInputStream(fullAudioData),
                            format,
                            fullAudioData.length / format.getFrameSize())) {
                        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, targetPath.toFile());
                    }
                    System.out.println("Saved audio stream to: " + targetPath.toAbsolutePath());
                } catch (IOException e) {
                    fail("Failed to save audio file: " + e.getMessage());
                }
            }
        } catch (LineUnavailableException e) {
            fail("Failed to open audio line: " + e.getMessage());
        }
    }
}
