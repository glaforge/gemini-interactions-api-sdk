package io.github.glaforge.gemini.interactions;

import io.github.glaforge.gemini.interactions.model.Config.SpeechConfig;
import io.github.glaforge.gemini.interactions.model.Events;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import org.junit.jupiter.api.Test;
import java.util.stream.Stream;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.util.Base64;

import javax.sound.sampled.*;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
public class Gemini31SpeechGenerationTest {

    @Test
    public void testSpeechGenerationWithTagsAndContext() {
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
            .speechConfig(new SpeechConfig("Algenib", "en-GB"))
            .stream(true)
            .build();

        try (Stream<Events> eventStream = client.stream(request)) {
            // Audio format: 24kHz, 16-bit, Mono, Signed, Little Endian
            AudioFormat format = new AudioFormat(24000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                line.open(format);
                line.start();
                System.out.println("Streaming audio playback in real-time...");

                eventStream.forEach(event -> {
                    if (event instanceof Events.ContentDelta cd && cd.delta() instanceof Events.AudioDelta audioDelta) {
                        byte[] audioData = Base64.getDecoder().decode(audioDelta.data());
                        line.write(audioData, 0, audioData.length);
                    }
                });

                line.drain();
                System.out.println("Playback complete.");
            }
        } catch (LineUnavailableException e) {
            fail("Failed to open audio line: " + e.getMessage());
        }
    }
}
