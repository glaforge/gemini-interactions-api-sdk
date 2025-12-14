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
import io.github.glaforge.gemini.interactions.model.Content.ImageContent;
import io.github.glaforge.gemini.interactions.model.Content.ThoughtContent;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

public class IntegrationTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
    public void testSimpleCall() throws IOException, InterruptedException {
        GeminiInteractionsClient client = GeminiInteractionsClient.builder()
                .apiKey(System.getenv("GEMINI_API_KEY"))
                .build();

        Interaction interaction = client.create(InteractionParams.ModelInteractionParams.builder()
                .model("gemini-2.5-flash")
                .input("Hi")
                .build());

        System.out.println(interaction);

        interaction.outputs().forEach((Content output) -> {
            switch (output) {
                case TextContent text -> System.out.println(text.text());
                case ImageContent image -> System.out.println(image.uri());
                case ThoughtContent thought -> System.out.println("Thought: " + thought.signature());
                default -> System.out.println("Unknown content type: " + output);
            }
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
    public void testDeepResearch() throws IOException, InterruptedException {
        GeminiInteractionsClient client = GeminiInteractionsClient.builder()
                .apiKey(System.getenv("GEMINI_API_KEY"))
                .build();

        Interaction interaction = client.create(InteractionParams.AgentInteractionParams.builder()
                .agent("deep-research-pro-preview-12-2025")
                .input("Research the history of the Google TPUs with a focus on 2025 and 2026")
                .background(true)
                .build());

        System.out.println(interaction);

        System.out.println("Waiting for interaction to complete... " + interaction.id());
        while (interaction.status() != Interaction.Status.COMPLETED) {
            System.out.println("Status: " + interaction.status());
            Thread.sleep(1000);
            interaction = client.get(interaction.id());
        }

        System.out.println(interaction);

        interaction.outputs().forEach((Content output) -> {
            switch (output) {
                case TextContent text -> System.out.println(text.text());
                case ImageContent image -> System.out.println(image.uri());
                case ThoughtContent thought -> System.out.println("Thought: " + thought.signature());
                default -> System.out.println("Unknown content type: " + output);
            }
        });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
    void testGemini3ProWithImage() throws IOException, InterruptedException {
        GeminiInteractionsClient client = GeminiInteractionsClient.builder()
                .apiKey(System.getenv("GEMINI_API_KEY"))
                .build();

        Interaction interaction = client.create(InteractionParams.ModelInteractionParams.builder()
                .model("gemini-3-pro-image-preview")
                .input("Create an infographic about blood, organs, and the circulatory system, for a 12 year old")
                .responseModalities(List.of(Interaction.Modality.IMAGE))
                .build());

        System.out.println(interaction);

        interaction.outputs().forEach((Content output) -> {
            switch (output) {
                case TextContent text -> System.out.println(text.text());
                case ImageContent image -> {
                    System.out.println("Image received. Saving to image.png...");
                    byte[] imageBytes = java.util.Base64.getDecoder().decode(image.data());
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream("image.png")) {
                        fos.write(imageBytes);
                    } catch (java.io.IOException e) {
                        throw new java.io.UncheckedIOException(e);
                    }
                }
                case ThoughtContent thought -> System.out.println("Thought: " + thought.signature());
                default -> System.out.println("Unknown content type: " + output);
            }
        });
    }
}
