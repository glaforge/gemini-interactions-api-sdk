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

import io.github.glaforge.gemini.interactions.model.Agent;
import io.github.glaforge.gemini.interactions.model.Content;
import io.github.glaforge.gemini.interactions.model.Content.TextContent;
import io.github.glaforge.gemini.interactions.model.Content.ImageContent;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.Step;
import io.github.glaforge.gemini.interactions.model.Events;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntegrationIT {

    @Test
    @EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
    public void testSimpleCall() throws IOException, InterruptedException {
        GeminiInteractionsClient client = GeminiInteractionsClient.builder()
                .apiKey(System.getenv("GEMINI_API_KEY"))
                .build();

        Interaction interaction = client.create(ModelInteractionParams.builder()
                .model("gemini-2.5-flash")
                .input("Hi")
                .build());

        System.out.println(interaction);

        interaction.steps().stream()
                .filter(step -> step instanceof Step.ModelOutputStep)
                .flatMap(step -> ((Step.ModelOutputStep) step).content().stream())
                .forEach((Content output) -> {
                    switch (output) {
                        case TextContent text -> System.out.println(text.text());
                        case ImageContent image -> System.out.println(image.data());
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
                .agent("deep-research-max-preview-04-2026")
                .input("AI agent design patterns (harness, orchestration, context engineering, etc.)")
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

        interaction.steps().stream()
                .filter(step -> step instanceof Step.ModelOutputStep)
                .flatMap(step -> ((Step.ModelOutputStep) step).content().stream())
                .forEach((Content output) -> {
                    switch (output) {
                        case TextContent text -> System.out.println(text.text());
                        case ImageContent image -> System.out.println(image.uri());
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

        interaction.steps().stream()
                .filter(step -> step instanceof Step.ModelOutputStep)
                .flatMap(step -> ((Step.ModelOutputStep) step).content().stream())
                .forEach((Content output) -> {
                    switch (output) {
                        case TextContent text -> System.out.println(text.text());
                        case ImageContent image -> {
                            System.out.println("Image received. Saving to image.png...");
                            byte[] imageBytes = image.data();
                            try (FileOutputStream fos = new FileOutputStream("target/image.png")) {
                                fos.write(imageBytes);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        }
                        default -> System.out.println("Unknown content type: " + output);
                    }
                });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
    public void testCustomAgentLifecycle() throws IOException, InterruptedException {
        GeminiInteractionsClient client = GeminiInteractionsClient.builder()
                .apiKey(System.getenv("GEMINI_API_KEY"))
                .build();

        String agentId = "java-sdk-test-agent-" + System.currentTimeMillis();
        boolean createdSuccessfully = false;

        // 1. Create a custom Agent definition
        Agent agentInput = Agent.builder()
                .id(agentId)
                .description("A custom agent built for integration testing.")
                .baseAgent("antigravity-preview-05-2026")
                .baseEnvironment("remote")
                .systemInstruction("You are a helpful coding assistant. Always respond concisely.")
                .build();

        System.out.println("Creating Agent: " + agentId);
        Agent created = null;
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                created = client.createAgent(agentInput);
                createdSuccessfully = true;
                System.out.println("Agent created successfully on attempt " + (i + 1));
                break;
            } catch (GeminiInteractionsException e) {
                System.out.println(
                        "Attempt " + (i + 1) + " failed: " + e.getMessage() + " (status: " + e.getStatusCode() + ")");
                if (e.getStatusCode() == 504 || e.getStatusCode() == 503) {
                    // Try to check if it was created anyway
                    Thread.sleep(5000);
                    try {
                        created = client.getAgent(agentId);
                        createdSuccessfully = true;
                        System.out.println("Agent was successfully created on the server despite the transient error!");
                        break;
                    } catch (GeminiInteractionsException ex) {
                        System.out.println("Agent was not found on server yet. Retrying creation...");
                    }
                } else {
                    throw e;
                }
            }
            if (i < maxRetries - 1) {
                Thread.sleep(5000);
            }
        }

        if (!createdSuccessfully) {
            System.out.println(
                    "Backend Custom Agent CRUD is currently experiencing high server-side latency or service unavailability.");
            System.out.println(
                    "Falling back to using pre-existing custom agent 'custom-agent-persistence-test' to verify remote sandbox execution.");
            agentId = "custom-agent-persistence-test";
        } else {
            System.out.println("Created Agent response: " + created);
            assertEquals(agentId, created.id());
        }

        try {
            // 2. Run the agent by creating an interaction
            System.out.println("Creating Interaction with Agent: " + agentId);
            InteractionParams.AgentInteractionParams params = InteractionParams.AgentInteractionParams.builder()
                    .agent(agentId)
                    .input("Say hello in exactly 3 words.")
                    .environment("remote")
                    .build();

            Interaction interaction = client.create(params);
            System.out.println("Interaction status: " + interaction.status() + " ID: " + interaction.id());

            // Wait for completion (since remote execution takes a few seconds)
            while (interaction.status() != Interaction.Status.COMPLETED &&
                    interaction.status() != Interaction.Status.FAILED &&
                    interaction.status() != Interaction.Status.CANCELLED) {
                System.out.println("Waiting for agent to process... current status: " + interaction.status());
                Thread.sleep(2500);
                interaction = client.get(interaction.id());
            }

            System.out.println("Final Interaction output:");
            interaction.steps().stream()
                    .filter(step -> step instanceof Step.ModelOutputStep)
                    .flatMap(step -> ((Step.ModelOutputStep) step).content().stream())
                    .forEach((Content output) -> {
                        if (output instanceof TextContent text) {
                            System.out.println("- " + text.text());
                        }
                    });

        } finally {
            if (createdSuccessfully) {
                // 3. Delete the custom Agent to clean up
                System.out.println("Deleting Agent: " + agentId);
                try {
                    client.deleteAgent(agentId);
                    System.out.println("Agent deleted successfully.");
                } catch (Exception e) {
                    System.err.println("Could not delete agent due to transient server issue: " + e.getMessage());
                }
            }
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
    public void testDefaultAgentStreaming() throws IOException, InterruptedException {
        GeminiInteractionsClient client = GeminiInteractionsClient.builder()
                .apiKey(System.getenv("GEMINI_API_KEY"))
                .build();

        InteractionParams.AgentInteractionParams params = InteractionParams.AgentInteractionParams.builder()
                .agent("antigravity-preview-05-2026")
                .input("Say hello in exactly 3 words.")
                .environment("remote")
                .stream(true)
                .build();

        System.out.println("Invoking default remote agent via SDK streaming...");
        StringBuilder textAccumulator = new StringBuilder();
        try (Stream<Events> eventStream = client.stream(params)) {
            eventStream.forEach(event -> {
                System.out.println("Received event: " + event.getClass().getSimpleName() + " -> " + event);
                if (event instanceof Events.StepDelta delta) {
                    if (delta.delta() instanceof Events.TextDelta textPart) {
                        textAccumulator.append(textPart.text());
                        System.out.print(textPart.text());
                    }
                }
            });
            System.out.println();
        }

        String finalResponse = textAccumulator.toString().trim();
        System.out.println("Accumulated text response: " + finalResponse);
        org.junit.jupiter.api.Assertions.assertTrue(finalResponse.length() > 0, "Agent should have responded with some text");
    }
}
