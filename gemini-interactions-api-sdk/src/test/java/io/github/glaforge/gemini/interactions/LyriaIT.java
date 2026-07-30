package io.github.glaforge.gemini.interactions;

import io.github.glaforge.gemini.interactions.model.Content;
import io.github.glaforge.gemini.interactions.model.Content.AudioContent;
import io.github.glaforge.gemini.interactions.model.Content.ImageContent;
import io.github.glaforge.gemini.interactions.model.Content.TextContent;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.Step;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.net.URI;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
public class LyriaIT {

    private final GeminiInteractionsClient client = GeminiInteractionsClient.builder()
            .apiKey(System.getenv("GEMINI_API_KEY"))
            .build();

    private void saveAudio(AudioContent audio, String filename) {
        assertNotNull(audio.data());
        assertTrue(audio.data().length > 0);
        assertEquals("audio", audio.type());
        System.out.println("Received audio data of length: " + audio.data().length + " for " + filename);

        try {
            Path targetPath = Paths.get("target", filename);
            Files.createDirectories(targetPath.getParent());

            // Lyria audio output natively encoded as MP3 byte stream
            byte[] audioData = audio.data();
            Files.write(targetPath, audioData);

            System.out.println("Saved Lyria generated audio to: " + targetPath.toAbsolutePath());
        } catch (IOException e) {
            fail("Failed to save audio file: " + e.getMessage());
        }
    }

    private Stream<Content> getContents(Interaction interaction) {
        if (interaction.steps() == null)
            return Stream.empty();
        return interaction.steps().stream()
                .filter(step -> step instanceof Step.ModelOutputStep)
                .flatMap(step -> ((Step.ModelOutputStep) step).content().stream());
    }

    private void printLyrics(Interaction interaction) {
        getContents(interaction)
                .filter(output -> output instanceof TextContent)
                .map(output -> (TextContent) output)
                .findFirst()
                .ifPresent(textContent -> System.out.println("Lyrics / Structure Generated:\n" + textContent.text()));
    }

    @Test
    public void testBasicMusicGeneration() {
        System.out.println("Running testBasicMusicGeneration");
        ModelInteractionParams request = ModelInteractionParams.builder()
                .model("models/lyria-3-clip-preview")
                .input("An epic song with opera voices about a quest. Deep synths and a speeding up tempo.")
                .responseModalities(
                        Interaction.Modality.AUDIO,
                        Interaction.Modality.TEXT)
                .build();

        Interaction interaction = client.create(request);

        assertNotNull(interaction);
        assertNotNull(interaction.steps());

        printLyrics(interaction);

        boolean hasAudio = getContents(interaction)
                .anyMatch(output -> output instanceof AudioContent);
        assertTrue(hasAudio, "Response should contain audio content");

        getContents(interaction)
                .filter(output -> output instanceof AudioContent)
                .map(output -> (AudioContent) output)
                .findFirst()
                .ifPresent(audio -> saveAudio(audio, "lyria-basic-quest.mp3"));
    }

    @Test
    public void testStructuredMusicGeneration() {
        System.out.println("Running testStructuredMusicGeneration");
        ModelInteractionParams request = ModelInteractionParams.builder()
                .model("models/lyria-3-clip-preview")
                .input("""
                            [Intro] Calm piano music setting a sunset scene on the beach
                            [Verse] Epic rock balade as the storm rages.
                            [Outro] Opera with choir as the sun reappears again through the black clouds.
                        """)
                .responseModalities(
                        Interaction.Modality.AUDIO,
                        Interaction.Modality.TEXT)
                .build();

        Interaction interaction = client.create(request);
        printLyrics(interaction);

        getContents(interaction)
                .filter(output -> output instanceof AudioContent)
                .map(output -> (AudioContent) output)
                .findFirst()
                .ifPresent(audio -> saveAudio(audio, "lyria-structured-storm.mp3"));
    }

    @Test
    public void testGenerationWithLyrics() {
        System.out.println("Running testGenerationWithLyrics");
        ModelInteractionParams request = ModelInteractionParams.builder()
                .model("models/lyria-3-clip-preview")
                .input("""
                            An uplifting song with guitar rifts about nano banana.
                            The lyrics should be:
                              Yellow peel, a tiny sweet, The Nano Banana, a tropical treat. But wait—it
                              hums, it starts to create, Switching into AI mode. Not a fruit, but a smart
                              machine, The bananiest model you've ever seen.
                        """)
                .responseModalities(
                        Interaction.Modality.AUDIO,
                        Interaction.Modality.TEXT)
                .build();

        Interaction interaction = client.create(request);
        printLyrics(interaction);

        getContents(interaction)
                .filter(output -> output instanceof AudioContent)
                .map(output -> (AudioContent) output)
                .findFirst()
                .ifPresent(audio -> saveAudio(audio, "lyria-lyrics-banana.mp3"));
    }

    @Test
    public void testInstrumentalMusicGeneration() {
        System.out.println("Running testInstrumentalMusicGeneration");
        ModelInteractionParams request = ModelInteractionParams.builder()
                .model("models/lyria-3-clip-preview")
                .input("Create a looping meditation music that feels like the wind. Instrumental only, no vocals.")
                .responseModalities(Interaction.Modality.AUDIO)
                .build();

        Interaction interaction = client.create(request);

        getContents(interaction)
                .filter(output -> output instanceof AudioContent)
                .map(output -> (AudioContent) output)
                .findFirst()
                .ifPresent(audio -> saveAudio(audio, "lyria-instrumental-wind.mp3"));
    }

    @Test
    public void testFullSongGeneration() {
        System.out.println("Running testFullSongGeneration");
        ModelInteractionParams request = ModelInteractionParams.builder()
                // Using the Lyria 3 Pro model for full-length song generation
                .model("models/lyria-3-pro-preview")
                .input("Write a full length epic power metal song about a brave knight fighting a dragon. It should have a guitar solo.")
                .responseModalities(List.of(Interaction.Modality.AUDIO, Interaction.Modality.TEXT))
                .build();

        Interaction interaction = client.create(request);
        printLyrics(interaction);

        getContents(interaction)
                .filter(output -> output instanceof AudioContent)
                .map(output -> (AudioContent) output)
                .findFirst()
                .ifPresent(audio -> saveAudio(audio, "lyria-full-knight-dragon.mp3"));
    }

    @Test
    public void testImageToMusicGeneration() {
        System.out.println("Running testImageToMusicGeneration");
        try {
            // Downloading a sample image directly from the Generative AI cookbook examples
            byte[] imageBytes = URI
                    .create("https://storage.googleapis.com/generativeai-downloads/images/groceries.jpeg")
                    .toURL()
                    .openStream()
                    .readAllBytes();

            ModelInteractionParams request = ModelInteractionParams.builder()
                    .model("models/lyria-3-clip-preview")
                    .input(
                            new TextContent(
                                    "An epic song with opera voices about this quest. Deep synths and a speeding up tempo."),
                            new ImageContent(imageBytes, "image/jpeg"))
                    .responseModalities(
                            Interaction.Modality.AUDIO,
                            Interaction.Modality.TEXT)
                    .build();

            Interaction interaction = client.create(request);
            printLyrics(interaction);

            getContents(interaction)
                    .filter(output -> output instanceof AudioContent)
                    .map(output -> (AudioContent) output)
                    .findFirst()
                    .ifPresent(audio -> saveAudio(audio, "lyria-image-groceries.mp3"));
        } catch (IOException e) {
            fail("Failed to download image or generate music: " + e.getMessage());
        }
    }
}
