package io.github.glaforge.gemini.interactions;

import io.github.glaforge.gemini.interactions.model.Content.TextContent;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.Tool;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
public class GemmaTest {

    private final GeminiInteractionsClient client = GeminiInteractionsClient.builder()
        .apiKey(System.getenv("GEMINI_API_KEY"))
        .build();

    @ParameterizedTest
    @ValueSource(strings = {"models/gemma-4-26b-a4b-it", "models/gemma-4-31b-it"})
    public void testGemmaModels(String modelName) {
        System.out.println("Running test for model: " + modelName);
        ModelInteractionParams request = ModelInteractionParams.builder()
            .model(modelName)
            .input("What is the capital of France? Answer in one word.")
            .responseModalities(Interaction.Modality.TEXT)
            .build();

        Interaction interaction = client.create(request);

        assertNotNull(interaction);
        assertNotNull(interaction.outputs());
        assertTrue(interaction.outputs().stream().anyMatch(content -> content instanceof TextContent));

        interaction.outputs().stream()
            .filter(output -> output instanceof TextContent)
            .map(output -> (TextContent) output)
            .findFirst()
            .ifPresent(textContent -> {
                System.out.println(modelName + " answer: " + textContent.text());
                assertTrue(textContent.text().contains("Paris"), "The response should contain 'Paris'");
            });
    }

    @ParameterizedTest
    @ValueSource(strings = {"models/gemma-4-26b-a4b-it", "models/gemma-4-31b-it"})
    public void testGemmaModelsWithGoogleSearch(String modelName) {
        System.out.println("Running search test for model: " + modelName);

        ModelInteractionParams request = ModelInteractionParams.builder()
            .model(modelName)
            .input("Who is the actress who invented the MemPalace agent memory project?")
            .tools(new Tool.GoogleSearch())
            .responseModalities(Interaction.Modality.TEXT)
            .build();

        Interaction interaction = client.create(request);

        assertNotNull(interaction);
        assertNotNull(interaction.outputs());
        assertTrue(interaction.outputs().stream().anyMatch(content -> content instanceof TextContent));

        interaction.outputs().stream()
            .filter(output -> output instanceof TextContent)
            .map(output -> (TextContent) output)
            .findFirst()
            .ifPresent(textContent -> {
                System.out.println(modelName + " answer (with search): " + textContent.text());
                String answerLower = textContent.text().toLowerCase();
                assertTrue(answerLower.contains("milla"), "The response should contain 'Milla'");
                assertTrue(answerLower.contains("jovovich") || answerLower.contains("jovovitch"), "The response should contain 'Jovovich'");
            });
    }

}
