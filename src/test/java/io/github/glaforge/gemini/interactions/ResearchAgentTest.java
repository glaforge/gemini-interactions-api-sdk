package io.github.glaforge.gemini.interactions;

import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.Interaction.Modality;
import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.Tool.GoogleSearch;
import io.github.glaforge.gemini.interactions.model.Content;
import io.github.glaforge.gemini.interactions.model.Content.ImageContent;
import io.github.glaforge.gemini.interactions.model.Content.TextContent;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".*")
public class ResearchAgentTest {

    private static GeminiInteractionsClient client;

    @BeforeAll
    public static void setup() {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null) {
            throw new IllegalStateException("GEMINI_API_KEY environment variable not set");
        }
        client = GeminiInteractionsClient.builder()
                .apiKey(apiKey)
                .build();
    }

    @Test
    public void testResearchPlannerExecutor() throws IOException, InterruptedException {
        // --- Setup ---
        // Step 0: Define the research goal
        String researchGoal = """
            Research the current state of Quantum Computing in 2025,
            specifically looking for major breakthroughs in error correction.
            """;
        System.out.println("🔬 Research Goal: " + researchGoal);

        // --- Phase 1: Plan ---
        // Gemini 3 Flash Preview creates research tasks
        System.out.println("\n--- Phase 1: Planning ---");

        ModelInteractionParams planParams = ModelInteractionParams.builder()
                .model("gemini-3-flash-preview")
                .input(String.format("""
                    Create a numbered research plan for: %s
                    Format: 1. [Task] - [Details]
                    Include 3 specific tasks.
                    """, researchGoal))
                .tools(new GoogleSearch())
                .store(true)
                .build();

        Interaction planInteraction = client.create(planParams);
        String planText = getText(planInteraction);
        String planId = planInteraction.id();

        assertNotNull(planId, "Plan Interaction ID should not be null");
        assertFalse(planText.isEmpty(), "Plan text should not be empty");

        System.out.println("📋 Plan Generated (ID: " + planId + "):");
        System.out.println(planText);

        List<String> tasks = parseTasks(planText);
        assertFalse(tasks.isEmpty(), "Should have parsed at least one task");

        // --- Phase 2: Research ---
        // Select tasks and run Deep Research Agent
        // In this test, we select all tasks.
        System.out.println("\n--- Phase 2: Researching ---");

        String selectedTasks = String.join("\n\n", tasks);
        System.out.println("Selected Tasks:\n" + selectedTasks);

        AgentInteractionParams researchParams = AgentInteractionParams.builder()
                .agent("deep-research-pro-preview-12-2025")
                .input(String.format(
                    "Research these tasks thoroughly with sources:\n\n%s",
                    selectedTasks))
                .previousInteractionId(planId)
                .background(true)
                .store(true)
                .build();

        Interaction researchInteraction = client.create(researchParams);
        String researchId = researchInteraction.id();
        System.out.println("🚀 Started Deep Research (ID: " + researchId + ") - Status: " + researchInteraction.status());

        // Wait for completion (Background task) up to 10 mins as deep research can be slow
        researchInteraction = waitForCompletion(client, researchId, 600);

        String researchText = getText(researchInteraction);
        System.out.println("📄 Research Results (Status: " + researchInteraction.status() + "):");
        System.out.println(researchText); // Printing potentially large output

        assertEquals(Interaction.Status.COMPLETED, researchInteraction.status(), "Research interaction should complete successfully");
        assertNotNull(researchText, "Research text should not be null");

        // --- Phase 3: Synthesis ---
        System.out.println("\n--- Phase 3: Synthesis ---");

        ModelInteractionParams synthesisParams = ModelInteractionParams.builder()
                .model("gemini-3-pro-preview")
                .input(String.format(
                    "Create executive report with Summary, Findings, Recommendations, Risks based on the research:\n\n%s",
                    researchText))
                .previousInteractionId(researchId)
                .store(true)
                .build();

        Interaction synthesisInteraction = client.create(synthesisParams);
        String synthesisText = getText(synthesisInteraction);

        System.out.println("📊 Executive Report:");
        System.out.println(synthesisText);

        assertNotNull(synthesisText, "Synthesis text should not be null");
        assertFalse(synthesisText.isEmpty(), "Synthesis text should not be empty");

        // --- Phase 4: Infographic ---
        System.out.println("\n--- Phase 4: Infographic ---");
        ModelInteractionParams infographicParams = ModelInteractionParams.builder()
                .model("gemini-3-pro-image-preview")
                .input(String.format("""
                        Create a whiteboard summary infographic for the following:

                        %s""", synthesisText))
                .responseModalities(List.of(Modality.IMAGE))
                .build();

        Interaction infographicInteraction = client.create(infographicParams);

        System.out.println("📊 Infographic generated");
        saveInfographic(infographicInteraction);
    }

    // Helper to extract text from interaction outputs
    private String getText(Interaction interaction) {
        if (interaction.outputs() == null) return "";
        return interaction.outputs().stream()
                .filter(c -> c instanceof TextContent)
                .map(c -> ((TextContent) c).text())
                .collect(Collectors.joining("\n"));
    }

    // Helper to extract infographic from interaction outputs
    private void saveInfographic(Interaction interaction) {
        if (interaction.outputs() == null) return;

        List<Content> outputs = interaction.outputs();
        for (int i = 0; i < outputs.size(); i++) {
            Content output = outputs.get(i);
            if (output instanceof ImageContent image) {
                System.out.println("Image received. Saving to png...");
                byte[] imageBytes = Base64.getDecoder().decode(image.data());
                try (FileOutputStream fos = new FileOutputStream("target/image" + i + ".png")) {
                    fos.write(imageBytes);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
    }

    // Helper to parse tasks (simplified version of the Python regex)
    private List<String> parseTasks(String text) {
        Pattern pattern = Pattern.compile("^(\\d+)[\\.\\)\\-]\\s*(.+?)(?=\\n\\d+[\\.\\)\\-]|\\n\\n|$)", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);

        return matcher.results()
                .map(m -> m.group(1) + ". " + m.group(2).trim().replace('\n', ' '))
                .collect(Collectors.toList());
    }

    // Helper to wait for background completion
    private Interaction waitForCompletion(GeminiInteractionsClient client, String id, int timeoutSeconds) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeoutSeconds * 1000L;

        Interaction interaction = client.get(id);
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            System.out.print(".");
            if (interaction.status() != Interaction.Status.IN_PROGRESS) {
                System.out.println("\nFinished with status: " + interaction.status());
                return interaction;
            }
            Thread.sleep(5000); // Poll every 5 seconds
            interaction = client.get(id);
        }
        return interaction;
    }
}
