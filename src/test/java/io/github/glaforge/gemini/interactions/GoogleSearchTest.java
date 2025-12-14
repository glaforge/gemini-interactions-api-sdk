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
import io.github.glaforge.gemini.interactions.model.Tool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GoogleSearchTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
    void testGoogleSearch() throws IOException, InterruptedException {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null) {
            System.out.println("GEMINI_API_KEY not set, skipping testGoogleSearch");
            return;
        }

        GeminiInteractionsClient client = GeminiInteractionsClient.builder()
            .apiKey(apiKey)
            .build();

        // 1. Define the Google Search tool
        // Tool is an interface, we can use the Tool.GoogleSearch record directly
        Tool googleSearch = new Tool.GoogleSearch();
        List<Tool> tools = List.of(googleSearch);

        // 2. Create Interaction
        InteractionParams.ModelInteractionParams createParams = InteractionParams.ModelInteractionParams.builder()
            .model("gemini-2.5-flash")
            .input("What is the latest news about the Ukraine / Russia war?")
            .tools(tools)
            .build();

        System.out.println("Sending search request...");
        Interaction interaction = client.create(createParams);
        System.out.println("Response status: " + interaction.status());
        assertNotNull(interaction.outputs(), "Interaction outputs should not be null");

        // 3. Verify Response
        // The response should contain text and potentially citation info
        // (though not explicitly checked here without inspecting metadata).
        // Since Google Search is a built-in tool, the model might just return the answer directly
        // using the tool in the background (hidden) or expose it.
        // For interactions API, grounded responses are often integrated into text.

        Content lastOutput = interaction.outputs().getLast();
        System.out.println("Last output type: " + lastOutput.getClass().getSimpleName());

        if (lastOutput instanceof TextContent text) {
            System.out.println("Model Answer: " + text.text());
            assertTrue(text.text().length() > 0, "Model should provide an answer");
            // We expect some relevant keywords in the answer
            String answer = text.text().toLowerCase();
            assertTrue(answer.contains("ukraine") || answer.contains("russia"), "Answer should be relevant to the query");
        } else {
            System.out.println("Output content: " + lastOutput);
             // It's possible we get other content types, but text is expected for a summary.
        }
    }
}
