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

import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.Step;
import io.github.glaforge.gemini.interactions.model.Content.TextContent;
import com.google.auth.oauth2.GoogleCredentials;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for testing Vertex AI endpoints with the Gemini Interactions API.
 * This test uses the Google Cloud Application Default Credentials.
 * It connects to the "genai-java-demos" project.
 */
public class VertexAIIntegrationIT {

    @Test
    @EnabledIfEnvironmentVariable(named = "GOOGLE_APPLICATION_CREDENTIALS", matches = ".+")
    public void testVertexAIEndpoint() throws IOException, InterruptedException {
        // 1. Initialize Client with Google Cloud Project (Vertex AI mode)
        GeminiInteractionsClient client = GeminiInteractionsClient.builder()
            .project("genai-java-demos")
            .location("global") // "global" is the default, but specifying it explicitly for clarity
            .build();
            
        assertNotNull(client);

        // 2. Define a simple model request
        ModelInteractionParams request = ModelInteractionParams.builder()
            .model("lyria-3-clip-preview")
            .input("Create a 10s audio clip of a rock song")
            .build();

        // 3. Execute the interaction
        System.out.println("Sending Vertex AI request...");
        Interaction interaction = client.create(request);

        // 4. Verify the response
        assertNotNull(interaction);
        assertNotNull(interaction.id());

        // Print to console to easily verify
        System.out.println("Vertex AI Interaction created with ID: " + interaction.id());
        System.out.println("Interaction Status: " + interaction.status());

        // To fetch steps and see the final model response:
        boolean foundText = false;
        if (interaction.steps() != null) {
            for (Step step : interaction.steps()) {
                if (step instanceof Step.ModelOutputStep modelStep) {
                    for (var content : modelStep.content()) {
                        if (content instanceof TextContent textContent) {
                            System.out.println("Model response: " + textContent.text());
                            foundText = true;
                        }
                    }
                }
            }
        }
        
        // Assert we got some text back
        assertTrue(foundText, "Expected to receive a text response from the model");
    }
}
