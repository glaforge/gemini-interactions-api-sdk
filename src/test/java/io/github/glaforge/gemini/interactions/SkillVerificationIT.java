
package io.github.glaforge.gemini.interactions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.util.List;
import static io.github.glaforge.gemini.schema.GSchema.*;
import io.github.glaforge.gemini.interactions.model.Content.*;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.Interaction.Modality;
import io.github.glaforge.gemini.interactions.model.Interaction.Status;
import io.github.glaforge.gemini.interactions.model.*;
import io.github.glaforge.gemini.interactions.GeminiInteractionsClient;
import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;

@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".*")
public class SkillVerificationIT {

    @Test
    public void testSnippet0() throws Exception {
        
        GeminiInteractionsClient client = GeminiInteractionsClient.builder()
            .apiKey(System.getenv("GEMINI_API_KEY"))
            .build();
    }

    @Test
    public void testSnippet1() throws Exception {
        
        GeminiInteractionsClient client = GeminiInteractionsClient.builder().apiKey(System.getenv("GEMINI_API_KEY")).build();
        
        ModelInteractionParams request = ModelInteractionParams.builder()
            .model("gemini-2.5-flash")
            .input("Why is the sky blue?")
            .build();
        
        Interaction response = client.create(request);
        Step.ModelOutputStep step = (Step.ModelOutputStep) response.steps().getLast();
        System.out.println(step.content().get(0));
    }

    @Test
    public void testSnippet2() throws Exception {
        
        GeminiInteractionsClient client = GeminiInteractionsClient.builder().apiKey(System.getenv("GEMINI_API_KEY")).build();
        
        ModelInteractionParams request = ModelInteractionParams.builder()
            .model("gemini-3-pro-image-preview")
            .input("Create an infographic about blood, organs, and the circulatory system")
            .responseModalities(Modality.IMAGE)
            .build();
        
        Interaction interaction = client.create(request);
        
        interaction.steps().forEach(step -> {
            if (step instanceof Step.ModelOutputStep modelOutputStep) {
                modelOutputStep.content().forEach(content -> {
                    if (content instanceof ImageContent image) {
                        byte[] imageBytes = image.data();
                        System.out.println("Image generated with " + imageBytes.length + " bytes.");
                    }
                });
            }
        });
    }

    @Test
    public void testSnippet3() throws Exception {
        
        GeminiInteractionsClient client = GeminiInteractionsClient.builder().apiKey(System.getenv("GEMINI_API_KEY")).build();
        
        // 1. First turn (must set store=true)
        ModelInteractionParams turn1 = ModelInteractionParams.builder()
            .model("gemini-2.5-flash")
            .input("Hello!")
            .store(true)
            .build();
        
        Interaction response1 = client.create(turn1);
        String id = response1.id();
        Step.ModelOutputStep step1 = (Step.ModelOutputStep) response1.steps().getLast();
        System.out.println(step1.content().get(0));
        
        // 2. Second turn (referencing previous ID)
        ModelInteractionParams turn2 = ModelInteractionParams.builder()
            .model("gemini-2.5-flash")
            .input("Tell me a joke")
            .previousInteractionId(id)
            .store(true)
            .build();
        
        Interaction response2 = client.create(turn2);
        Step.ModelOutputStep step2 = (Step.ModelOutputStep) response2.steps().getLast();
        System.out.println(step2.content().get(0));
    }

    @Test
    public void testSnippet4() throws Exception {
        
        GeminiInteractionsClient client = GeminiInteractionsClient.builder().apiKey(System.getenv("GEMINI_API_KEY")).build();
        
        ModelInteractionParams params = ModelInteractionParams.builder()
            .model("gemini-2.5-flash")
            .input("List 2 popular cookie recipes")
            .responseFormat(
                arr().items(
                    obj()
                        .prop("recipe_name", str())
                        .prop("ingredients", arr().items(str()))
                )
            )
            .build();
        
        Interaction response = client.create(params);
        Step.ModelOutputStep step = (Step.ModelOutputStep) response.steps().getLast();
        System.out.println(step.content().get(0));
    }

    @Test
    public void testSnippet5() throws Exception {
        
        GeminiInteractionsClient client = GeminiInteractionsClient.builder().apiKey(System.getenv("GEMINI_API_KEY")).build();
        
        AgentInteractionParams request = AgentInteractionParams.builder()
            .agent("deep-research-pro-preview-12-2025")
            .input("Research the history of the Google TPUs")
            .background(true)
            .build();
        
        Interaction interaction = client.create(request);
        
        // Poll for completion
        while (interaction.status() != Status.COMPLETED && interaction.status() != Status.FAILED) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            interaction = client.get(interaction.id());
        }
        
        System.out.println(interaction.steps());
    }

    @Test
    public void testSnippet6() throws Exception {
        
        GeminiInteractionsClient client = GeminiInteractionsClient.builder().apiKey(System.getenv("GEMINI_API_KEY")).build();
        
        Agent customAgent = Agent.builder()
            .id("my-concise-coder-agent-" + System.currentTimeMillis())
            .description("A custom agent built for secure coding tasks.")
            .baseAgent("antigravity-preview-05-2026")
            .baseEnvironment("remote")
            .systemInstruction("You are a helpful coding assistant. Always respond concisely.")
            .tools(List.of(
                new AgentTool.GoogleSearch(),
                new AgentTool.CodeExecution()
            ))
            .baseEnvironment(new EnvironmentConfig(
                new EnvironmentNetworkEgressAllowlist(List.of(
                    new AllowlistEntry("github.com")
                )),
                null
            ))
            .build();
        
        Agent provisioned = client.createAgent(customAgent);
        System.out.println("Created custom agent: " + provisioned.id());
        
        // You can interact with it using AgentInteractionParams...
        AgentInteractionParams params = AgentInteractionParams.builder()
            .agent(provisioned.id())
            .input("Explain the difference between HSL and RGB color systems.")
            .environment("remote")
            .build();
        
        Interaction interaction = client.create(params);
        
        // Delete custom agent when done
        client.deleteAgent(provisioned.id());
    }

}
