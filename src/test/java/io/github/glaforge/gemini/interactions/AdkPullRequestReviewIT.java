package io.github.glaforge.gemini.interactions;

import io.github.glaforge.gemini.interactions.model.*;
import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.github.glaforge.ansiren.Ansi.blue;
import static io.github.glaforge.ansiren.Ansi.cyan;
import io.github.glaforge.ansiren.MarkdownRenderer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".*")
public class AdkPullRequestReviewIT {

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
    public void testAdkPullRequestReview() throws IOException, InterruptedException {
        String agentId = "adk-pr-reviewer-" + System.currentTimeMillis();
        boolean createdSuccessfully = false;

        // 1. Create a custom Agent configuration
        Agent customAgent = Agent.builder()
                .id(agentId)
                .description("An agent that analyzes pull requests for the ADK Java project.")
                .baseAgent("antigravity-preview-05-2026")
                .baseEnvironment(new EnvironmentConfig(
                        new EnvironmentNetworkEgressAllowlist(List.of(
                                new AllowlistEntry("github.com"))),
                        List.of(
                                Source.builder()
                                        .type(Source.Type.REPOSITORY)
                                        .target("adk-java")
                                        .source("https://github.com/google/adk-java")
                                        .build())))
                .systemInstruction(
                        """
                                # Role & Objective
                                You are an expert, empathetic Senior Software Engineer and Code Reviewer.
                                Your mission is to review GitHub Pull Requests (PRs) to ensure high code quality, security, performance, and adherence to best practices.

                                Your goal is to provide actionable, clear, and constructive feedback that helps the author improve the code while maintaining a positive, collaborative engineering culture.

                                ---

                                # Review Focus Areas
                                Evaluate the diff carefully against the following priorities (ordered from highest to lowest importance):

                                1. **Correctness & Logic:** Does the code actually do what it claims? Look for edge cases, off-by-one errors, null pointer risks, or logical flaws.
                                2. **Security:** Identify vulnerabilities such as SQL injection, XSS, insecure dependencies, hardcoded secrets/API keys, or improper access control.
                                3. **Performance & Scalability:** Check for resource leaks, unoptimized queries (e.g., N+1 problems), blocking synchronous calls in async environments, or inefficient loops.
                                4. **Readability & Maintainability:** Is the code self-documenting? Are variable/function names descriptive? Is it overly complex or violating DRY (Don't Repeat Yourself) / SOLID principles?
                                5. **Testing:** Are there missing unit or integration tests for new features or critical bug fixes?

                                ---

                                # Tone & Communication Guidelines
                                * **Be a Peer, Not a Lecturer:** Use collaborative, encouraging language. Write "We could optimize this by..." rather than "You should fix this."
                                * **Assume Competence:** If a design choice looks strange, ask for clarification first ("What was the main driver behind using X over Y here?") instead of assuming it is wrong.
                                * **Acknowledge the Good:** Celebrate clean code, elegant solutions, and great documentation. Don't just comment on negatives.
                                * **Explain the "Why":** Never tell a user to change code without explaining the underlying reasoning or potential consequence of the current implementation.

                                ---

                                # Output Format
                                Structure your review cleanly using the following markdown format:

                                ### 🚀 Summary
                                A 1-2 sentence high-level overview of the PR and its general quality.

                                ### 🛠️ Critical Issues (if any)
                                *Showstopper bugs, security vulnerabilities, or severe architectural flaws that must be fixed before merging.*
                                * **File & Line:** `path/to/file.js` (Lines X-Y)
                                * **Issue:** [Description of the problem]
                                * **Suggestion:** [How to fix it, including code snippets where helpful]

                                ### 💡 Suggestions & Refactors
                                *Non-blocking improvements for readability, minor performance gains, or style consistency.*
                                * **File & Line:** `path/to/file.js` (Lines X-Y)
                                * **Context:** [Brief description]
                                * **Suggestion:** [Your actionable advice]

                                ### 👏 Praise
                                *Highlight 1-2 areas where the author did an exceptionally good job (e.g., thorough test coverage, great refactoring).*

                                ---

                                # CRITICAL FINAL STEP
                                Once you have completed your review, you MUST execute a Python script to write your ENTIRE review to a file named 'PR-REVIEW.md' in the current working directory.
                                You must do this using the CodeExecution tool. Example:
                                ```python
                                with open("PR-REVIEW.md", "w") as f:
                                    f.write('''[YOUR FULL MARKDOWN REVIEW HERE]''')
                                ```
                                This is an absolute requirement for the system to process your review.
                                """)
                .tools(List.of(
                        new AgentTool.CodeExecution(),
                        new AgentTool.GoogleSearch()))
                .build();

        System.out.println("Creating Custom Agent: " + agentId);
        Agent created = client.createAgent(customAgent);
        createdSuccessfully = true;
        System.out.println("Agent created successfully: " + created.id());

        try {
            // 2. Initiate the interaction with the custom agent
            AgentInteractionParams runParams = AgentInteractionParams.builder()
                    .agent(agentId)
                    .input("Please review this pull request adding a Spring Boot starter for the ADK Java project: https://github.com/google/adk-java/pull/653")
                    .environment("remote")
                    .build();

            System.out.println("Running interaction with agent...");
            Interaction interaction = client.create(runParams);
            System.out.println("Interaction created with ID: " + interaction.id());

            // 3. Poll for completion
            int maxPolls = 100;
            int polls = 0;
            while (!interaction.status().isFinished() &&
                    polls < maxPolls) {
                System.out.println("Waiting for agent... Current status: " + interaction.status());
                Thread.sleep(3000);
                interaction = client.get(interaction.id());
                polls++;
            }

            System.out.println(cyan("\nFinal interaction status: " + interaction.status()));
            assertEquals(Interaction.Status.COMPLETED, interaction.status());

            // 4. Download and verify the generated file
            System.out.println("Downloading agent environment...");
            try (AgentEnvironment env = client.getEnvironment(interaction.environmentId()).refresh()) {
                System.out.println("Files in environment: " + env.listFiles());
                assertTrue(env.fileExists("PR-REVIEW.md"), "PR-REVIEW.md should have been generated by the agent");
                String report = env.readTextFile("PR-REVIEW.md");
                assertFalse(report.isEmpty(), "PR-REVIEW.md should not be empty");

                Path outputPath = Path.of("target/PR-REVIEW.md");
                Files.createDirectories(outputPath.getParent());
                Files.writeString(outputPath, report);

                System.out.println("Successfully downloaded PR-REVIEW.md. Content length: " + report.length());
                System.out.println(blue("\n--- Rendered PR-REVIEW.md ---"));
                MarkdownRenderer renderer = new MarkdownRenderer();
                System.out.println(renderer.render(report));
                System.out.println(blue("-----------------------------\n"));
            }

        } finally {
            if (createdSuccessfully) {
                System.out.println("Deleting Custom Agent: " + agentId);
                client.deleteAgent(agentId);
                System.out.println("Agent deleted.");
            }
        }
    }
}
