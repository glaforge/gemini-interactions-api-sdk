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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".*")
public class GithubAnalyzerAgentIT {

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
                .description("""
                        An agent that clones public GitHub repositories,
                        analyzes the codebase structure, and explains its architecture.
                        """)
                .baseAgent("antigravity-preview-05-2026")
                .baseEnvironment(new EnvironmentConfig(
                        new EnvironmentNetworkEgressAllowlist(List.of(
                                new AllowlistEntry("github.com"))),
                        List.of()))
                .systemInstruction("""
                        You are an expert software architect.
                        Clone the specified repository, analyze its primary directories and code files,
                        and provide a detailed, technical explanation of its components, architecture, and behavior.
                        """)
                .tools(List.of(
                        new AgentTool.CodeExecution(),
                        new AgentTool.GoogleSearch()))
                .build();

        try {
            System.out.println("Creating Custom GitHub Analyzer Agent: " + agentId);
            Agent created = client.createAgent(customAgent);
            createdSuccessfully = true;
            System.out.println("Agent created successfully: " + created.id());

            // 2. Initiate the interaction with the custom agent
            AgentInteractionParams runParams = AgentInteractionParams.builder()
                    .agent(agentId)
                    .input("""
                            Clone the repository https://github.com/glaforge/gemini-interactions-api-sdk,
                            analyze its main classes, and write a detailed architecture report.
                            You MUST physically write this report to a file named 'report.md' in the current working directory
                            (using `open("report.md", "w")` in Python, NOT `/report.md` or any absolute path).
                            Make sure to NOT use a leading slash in the file path, so that it is saved in the current directory.
                            Do not just claim you wrote it; execute code to write the report to the file.
                            """)
                    .environment("remote")
                    .build();

            System.out.println("Running interaction with agent...");
            Interaction interaction = client.create(runParams);
            System.out.println("Interaction created with ID: " + interaction.id());

            // 3. Poll for completion
            int maxPolls = 60;
            int polls = 0;
            while (!interaction.status().isFinished() && polls < maxPolls) {
                System.out.println("Waiting for agent... Current status: " + interaction.status());
                Thread.sleep(3000);
                interaction = client.get(interaction.id());
                polls++;
            }

            System.out.println("Final interaction status: " + interaction.status());
            assertEquals(Interaction.Status.COMPLETED, interaction.status());
            System.out.println("Interaction ID: " + interaction.id());
            System.out.println("Interaction Environment ID: " + interaction.environmentId());

            System.out.println("\n--- All Interaction Steps ---");
            for (Step step : interaction.steps()) {
                System.out.println("Step Type: " + step.getClass().getSimpleName() + " -> " + step);
            }

            StringBuilder outputText = new StringBuilder();
            interaction.steps().stream()
                    .filter(step -> step instanceof Step.ModelOutputStep)
                    .flatMap(step -> ((Step.ModelOutputStep) step).content().stream())
                    .filter(content -> content instanceof Content.TextContent)
                    .forEach(content -> {
                        Content.TextContent text = (Content.TextContent) content;
                        outputText.append(text.text());
                    });

            assertTrue(outputText.length() > 0, "Agent should have outputted architectural analysis");
            System.out.println("\n--- Analysis Output ---\n" + outputText);

            // 4. Download and verify the generated file
            System.out.println("Downloading agent environment...");
            try (AgentEnvironment env = client.getEnvironment(interaction.environmentId()).refresh()) {
                System.out.println("Files in environment: " + env.listFiles());
                assertTrue(env.fileExists("report.md"), "report.md should have been generated by the agent");
                String report = env.readTextFile("report.md");
                assertFalse(report.isEmpty(), "report.md should not be empty");
                System.out.println("Successfully downloaded report.md. Content length: " + report.length());
            }
        } catch (GeminiInteractionsException e) {
            if (e.getStatusCode() == 403 || (e.getMessage() != null && e.getMessage().contains("permission_denied"))) {
                System.out.println("Skipping test: GEMINI_API_KEY does not have permission for antigravity-preview-05-2026 agent.");
                return;
            }
            throw e;
        } finally {
            if (createdSuccessfully) {
                System.out.println("Deleting Custom Agent: " + agentId);
                try {
                    client.deleteAgent(agentId);
                    System.out.println("Agent deleted.");
                } catch (Exception e) {
                    System.err.println("Could not delete agent: " + e.getMessage());
                }
            }
        }
    }
}
