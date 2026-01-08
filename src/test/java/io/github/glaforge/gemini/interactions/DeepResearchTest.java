package io.github.glaforge.gemini.interactions;

import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.Interaction.Status;
import io.github.glaforge.gemini.interactions.model.Content.TextContent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
public class DeepResearchTest {
    @Test
    void testDeepResearch() throws IOException, InterruptedException {
        GeminiInteractionsClient client = GeminiInteractionsClient.builder()
            .apiKey(System.getenv("GEMINI_API_KEY"))
            .build();

        AgentInteractionParams request = AgentInteractionParams.builder()
            .agent("deep-research-pro-preview-12-2025")
            .input("""
                Comparatif des meilleurs becs pour saxophone alto
                afin de pouvoir jouer des notes basses, des notes justes,
                et de pouvoir jouer facilement pour les débutants.
                """)
            .background(true)
            .build();

        Interaction interaction = client.create(request);

        // Poll for completion
        while (interaction.status() != Status.COMPLETED) {
            System.out.print(".");
            Thread.sleep(1000);
            interaction = client.get(interaction.id());
        }

        if (interaction.outputs() != null && interaction.outputs().size() > 0) {
            if (interaction.outputs().get(0) instanceof TextContent text) {
                System.out.println(text.text());
                Files.write(Paths.get("target/output.md"), text.text().getBytes());
            } else {
                System.out.println("Output is not a text");
            }
        } else {
            System.out.println("No output");
        }
    }
}
