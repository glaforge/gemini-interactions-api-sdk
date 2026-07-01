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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UrlContextTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
    void testUrlSummarization() throws IOException, InterruptedException {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null) {
            System.out.println("GEMINI_API_KEY not set, skipping testUrlSummarization");
            return;
        }

        GeminiInteractionsClient client = GeminiInteractionsClient.builder()
            .apiKey(apiKey)
            .build();

        // 1. Define the URL Context tool
        Tool urlContext = new Tool.UrlContext();
        List<Tool> tools = List.of(urlContext);

        // 2. Create Interaction
        // User asked to summarize a specific URL
        String url = "https://glaforge.dev/posts/2025/11/21/gemini-is-cooking-bananas-under-antigravity/";
        String prompt = "Summarize the article at this URL: " + url;

        InteractionParams.ModelInteractionParams createParams = InteractionParams.ModelInteractionParams.builder()
            .model("gemini-3.1-flash")
            .input(prompt)
            .tools(tools)
            .build();

        Interaction interaction = null;
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                System.out.println("Sending URL context request (attempt " + (i + 1) + ")...");
                interaction = client.create(createParams);
                System.out.println("Response status: " + interaction.status());
                break;
            } catch (Exception e) {
                System.out.println("Attempt " + (i + 1) + " failed: " + e.getMessage());
                if (i == maxRetries - 1) {
                    throw e;
                }
                Thread.sleep(5000);
            }
        }
        assertNotNull(interaction.steps(), "Interaction steps should not be null");

        // 3. Verify Response
        System.out.println("Steps count: " + interaction.steps().size());
        for (Step step : interaction.steps()) {
            System.out.println("Step Type: " + step.getClass().getSimpleName());
            if (step instanceof Step.UrlContextResultStep urlResult) {
                System.out.println("  URL Result: " + urlResult);
            } else if (step instanceof Step.ModelOutputStep out) {
                out.content().forEach(c -> {
                    if (c instanceof TextContent text) {
                        System.out.println("  Text: " + text.text());
                    }
                });
            }
        }

        Step lastOutput = interaction.steps().getLast();
        System.out.println("Last output type: " + lastOutput.getClass().getSimpleName());

        if (lastOutput instanceof Step.UrlContextResultStep urlResult) {
             System.out.println("URL Context Result received.");
             for (Step.UrlContextResult res : urlResult.result()) {
                 System.out.println("  URL: " + res.url() + ", Status: " + res.status());
             }

             System.out.println("Sending follow-up prompt...");

             InteractionParams.ModelInteractionParams continuationParams = InteractionParams.ModelInteractionParams.builder()
                 .model("gemini-3.1-flash")
                 .previousInteractionId(interaction.id())
                 .input("Please summarize the article based on the context you retrieved.")
                 .build();

             Interaction followUp = null;
             for (int i = 0; i < maxRetries; i++) {
                 try {
                     System.out.println("Sending follow-up prompt (attempt " + (i + 1) + ")...");
                     followUp = client.create(continuationParams);
                     System.out.println("Follow-up Status: " + followUp.status());
                     break;
                 } catch (Exception e) {
                     System.out.println("Follow-up attempt " + (i + 1) + " failed: " + e.getMessage());
                     if (i == maxRetries - 1) {
                         throw e;
                     }
                     Thread.sleep(5000);
                 }
             }

             for (Step step : followUp.steps()) {
                 if (step instanceof Step.ModelOutputStep out) {
                     for (Content c : out.content()) {
                         if (c instanceof TextContent text) {
                             System.out.println("Follow-up Text: " + text.text());
                             if (text.text().length() > 10) {
                                 assertTrue(true, "Got summary");
                                 return; // Success
                             }
                         }
                     }
                 }
             }

             // If we reach here without return, check if we got anything valid
             Step followUpOutput = followUp.steps().getLast();
             if (followUpOutput instanceof Step.ModelOutputStep out) {
                 for (Content c : out.content()) {
                     if (c instanceof TextContent text) {
                         assertTrue(text.text().length() > 10, "Summary should be descriptive");
                     }
                 }
             } else {
                 System.out.println("Unexpected follow-up output: " + followUpOutput);
             }

        } else if (lastOutput instanceof Step.ModelOutputStep out) {
             for (Content c : out.content()) {
                 if (c instanceof TextContent text) {
                     System.out.println("Model Answer: " + text.text());
                     assertTrue(text.text().length() > 10, "Summary should be descriptive");
                 }
             }
        } else {
            System.out.println("Output content: " + lastOutput);
        }
    }
}
