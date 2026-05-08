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

import io.github.glaforge.gemini.interactions.model.Content;
import io.github.glaforge.gemini.interactions.model.Content.TextContent;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams;
import io.github.glaforge.gemini.interactions.model.Step;
import io.github.glaforge.gemini.interactions.model.Tool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GoogleMapsTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
    void testGoogleMaps() throws IOException, InterruptedException {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null) {
            System.out.println("GEMINI_API_KEY not set, skipping testGoogleMaps");
            return;
        }

        GeminiInteractionsClient client = GeminiInteractionsClient.builder()
            .apiKey(apiKey)
            .build();

        // 1. Define the Google Maps tool
        Tool googleMaps = new Tool.GoogleMaps();

        // 2. Create Interaction
        InteractionParams.ModelInteractionParams createParams = InteractionParams.ModelInteractionParams.builder()
            .model("gemini-2.5-flash")
            .input("Can you recommend some good restaurants near the Eiffel tower in Paris?")
            .tools(googleMaps)
            .build();

        System.out.println("Sending search request with Google Maps tool...");
        Interaction interaction = client.create(createParams);
        System.out.println("Response status: " + interaction.status());
        assertNotNull(interaction.steps(), "Interaction steps should not be null");

        // 3. Verify Response
        var modelSteps = interaction.steps().stream()
            .filter(step -> step instanceof Step.ModelOutputStep)
            .toList();
        Step.ModelOutputStep lastStep = (Step.ModelOutputStep) modelSteps.getLast();
        Content lastOutput = lastStep.content().getLast();
        System.out.println("Last output type: " + lastOutput.getClass().getSimpleName());

        if (lastOutput instanceof TextContent text) {
            System.out.println("Model Answer: " + text.text());
            assertTrue(text.text().length() > 0, "Model should provide an answer");
            String answer = text.text().toLowerCase();
            assertTrue(answer.contains("paris") || answer.contains("eiffel"), "Answer should be relevant to the query");
            assertTrue(answer.contains("jules verne"), "Answer should mention the Jules Verne restaurant");
        } else {
            System.out.println("Output content: " + lastOutput);
        }
    }
}
