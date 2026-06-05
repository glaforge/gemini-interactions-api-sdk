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

import io.github.glaforge.gemini.interactions.model.*;
import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".*")
public class GithubAnalyzerAgentTest {

    private static GeminiInteractionsClient client;

    @BeforeAll
    public static void setup() {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("GEMINI_API_KEY environment variable not set");
        }
        client = GeminiInteractionsClient.builder()
                .apiKey(apiKey)
                .build();
    }

    @Test
    public void testGithubAnalyzerAgent() throws IOException, InterruptedException {
        String agentId = "github-analyzer-test-" + System.currentTimeMillis();
        boolean createdSuccessfully = false;

        // 1. Create a custom Agent configuration
        Agent customAgent = Agent.builder()
                .id(agentId)
                .description("An agent that clones public GitHub repositories, analyzes the codebase structure, and explains its architecture.")
                .baseAgent("antigravity-preview-05-2026")
                .baseEnvironment(new EnvironmentConfig(
                        new EnvironmentNetworkEgressAllowlist(List.of(
                                new AllowlistEntry("github.com")
                        )),
                        List.of()
                ))
                .systemInstruction(
                        "You are an expert software architect. Clone the specified repository, analyze its primary directories and code files, " +
                        "and provide a detailed, technical explanation of its components, architecture, and behavior."
                )
                .tools(List.of(
                        new AgentTool.CodeExecution(),
                        new AgentTool.GoogleSearch()
                ))
                .build();

        System.out.println("Creating Custom GitHub Analyzer Agent: " + agentId);
        Agent created = client.createAgent(customAgent);
        createdSuccessfully = true;
        System.out.println("Agent created successfully: " + created.id());

        try {
            // 2. Initiate the interaction with the custom agent
            AgentInteractionParams runParams = AgentInteractionParams.builder()
                    .agent(agentId)
                    .input("Clone the repository https://github.com/glaforge/gemini-interactions-api-sdk and explain its main classes.")
                    .environment("remote")
                    .build();

            System.out.println("Running interaction with agent...");
            Interaction interaction = client.create(runParams);
            System.out.println("Interaction created with ID: " + interaction.id());

            // 3. Poll for completion
            int maxPolls = 60;
            int polls = 0;
            while (interaction.status() != Interaction.Status.COMPLETED &&
                   interaction.status() != Interaction.Status.FAILED &&
                   interaction.status() != Interaction.Status.CANCELLED &&
                   polls < maxPolls) {
                System.out.println("Waiting for agent... Current status: " + interaction.status());
                Thread.sleep(3000);
                interaction = client.get(interaction.id());
                polls++;
            }

            System.out.println("Final interaction status: " + interaction.status());

            StringBuilder outputText = new StringBuilder();
            interaction.steps().stream()
                    .filter(step -> step instanceof Step.ModelOutputStep)
                    .flatMap(step -> ((Step.ModelOutputStep) step).content().stream())
                    .filter(content -> content instanceof Content.TextContent)
                    .forEach(content -> {
                        Content.TextContent text = (Content.TextContent) content;
                        System.out.println(text.text());
                        outputText.append(text.text());
                    });

            assertTrue(outputText.length() > 0, "Agent should have outputted architectural analysis");
            System.out.println("\n--- Analysis Output ---\n" + outputText);

        } finally {
            if (createdSuccessfully) {
                System.out.println("Deleting Custom Agent: " + agentId);
                client.deleteAgent(agentId);
                System.out.println("Agent deleted.");
            }
        }
    }
}
