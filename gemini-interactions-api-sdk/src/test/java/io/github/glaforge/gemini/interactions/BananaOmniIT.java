package io.github.glaforge.gemini.interactions;

import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
public class BananaOmniIT {

    @Test
    public void testCreateImageAndVideo() throws Exception {
        GeminiInteractionsClient client = GeminiInteractionsClient.builder()
            .apiKey(System.getenv("GEMINI_API_KEY"))
            .build();

        // Create an image with Nano Banana 2 Lite (gemini-3.1-flash-lite-image)
        ModelInteractionParams imageRequest = ModelInteractionParams.builder()
            .model("gemini-3.1-flash-lite-image")
            .input("A highly detailed realistic banana wearing sunglasses on a beach")
            .build();
            
        Interaction imageInteraction = client.create(imageRequest);
        assertNotNull(imageInteraction);
        System.out.println("Image interaction status: " + imageInteraction.status());
        
        if (imageInteraction.outputImage() != null) {
            System.out.println("Successfully generated image with gemini-3.1-flash-lite-image!");
        }

        // Create a video with Omni Lite (gemini-omni-flash-preview)
        ModelInteractionParams videoRequest = ModelInteractionParams.builder()
            .model("gemini-omni-flash-preview")
            .input("A banana jumping into a pool of water")
            .build();
            
        Interaction videoInteraction = client.create(videoRequest);
        assertNotNull(videoInteraction);
        System.out.println("Video interaction status: " + videoInteraction.status());
        
        if (videoInteraction.outputVideo() != null) {
            System.out.println("Successfully generated video with gemini-omni-flash-preview!");
        }
    }
}
