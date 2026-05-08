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

import io.github.glaforge.gemini.interactions.model.Config.DeepResearchAgentConfig;
import io.github.glaforge.gemini.interactions.model.Config.ThinkingSummaries;
import io.github.glaforge.gemini.interactions.model.Config.Visualization;
import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;
import io.github.glaforge.gemini.interactions.model.Events;
import io.github.glaforge.gemini.interactions.model.Interaction;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".*")
public class ResearchAgentTest {

    private static GeminiInteractionsClient client;

    @BeforeAll
    public static void setup() {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null) {
            throw new IllegalStateException("GEMINI_API_KEY environment variable not set");
        }
        client = GeminiInteractionsClient.builder()
                .apiKey(apiKey)
                .build();
    }

    @Test
    public void testResearchPlannerExecutor() throws IOException, InterruptedException {
        String researchGoal = """
            Best pecan pie recipes.
            """;
        System.out.println("Research Goal: " + researchGoal);

        System.out.println("\nResearching...");

        AgentInteractionParams researchParams = AgentInteractionParams.builder()
                .agent("deep-research-max-preview-04-2026")
                // .agent("deep-research-pro-preview-12-2025")
                .agentConfig(new DeepResearchAgentConfig("deep-research", ThinkingSummaries.AUTO, Visualization.AUTO, false))
                .input(researchGoal)
                .background(true)
                .store(true)
                .stream(true)
                .build();

        System.out.println("Starting streaming research...");

        StringBuilder researchTextBuilder = new StringBuilder();
        String[] capturedResearchId = new String[1];

        try (var eventStream = client.stream(researchParams)) {
            eventStream.forEach(event -> {
                if (event instanceof Events.ContentDelta deltaEvent) {
                    var delta = deltaEvent.delta();
                    if (delta instanceof Events.TextDelta textDelta) {
                        System.out.print(textDelta.text());
                        System.out.flush();
                        researchTextBuilder.append(textDelta.text());
                    } else if (delta instanceof Events.ThoughtSummaryDelta thoughtDelta) {
                        System.out.print("\n[Thinking...] " + thoughtDelta.content() + "\n");
                    } else if (delta instanceof Events.ThoughtSignatureDelta signatureDelta) {
                        System.out.print("\n[Thought Signature: " + signatureDelta.signature() + "]\n");
                    } else if (delta instanceof Events.GoogleSearchCallDelta searchCall) {
                        System.out.print("\n[Google Search Call: " + searchCall.arguments() + "]\n");
                    } else if (delta instanceof Events.GoogleSearchResultDelta searchResult) {
                        System.out.print("\n[Google Search Result: " + (searchResult.result() != null ? searchResult.result().size() : 0) + " results]\n");
                    } else if (delta instanceof Events.UrlContextCallDelta urlCall) {
                        System.out.print("\n[URL Context Call: " + urlCall.arguments() + "]\n");
                    } else if (delta instanceof Events.UrlContextResultDelta urlResult) {
                        System.out.print("\n[URL Context Result: " + (urlResult.result() != null ? urlResult.result().size() : 0) + " results]\n");
                    } else if (delta instanceof Events.CodeExecutionCallDelta codeCall) {
                        System.out.print("\n[Code Execution Call: " + codeCall.arguments() + "]\n");
                    } else if (delta instanceof Events.CodeExecutionResultDelta codeResult) {
                        System.out.print("\n[Code Execution Result: " + codeResult.result() + "]\n");
                    } else if (delta instanceof Events.FunctionCallDelta functionCall) {
                        System.out.print("\n[Function Call: " + functionCall.name() + " with args " + functionCall.arguments() + "]\n");
                    } else if (delta instanceof Events.FunctionResultDelta functionResult) {
                        System.out.print("\n[Function Result: " + functionResult.name() + " -> " + functionResult.result() + "]\n");
                    } else if (delta instanceof Events.FileSearchCallDelta fileCall) {
                        System.out.print("\n[File Search Call: " + fileCall.signature() + "]\n");
                    } else if (delta instanceof Events.FileSearchResultDelta fileResult) {
                        System.out.print("\n[File Search Result: " + (fileResult.result() != null ? fileResult.result().size() : 0) + " results]\n");
                    } else if (delta instanceof Events.McpServerToolCallDelta mcpCall) {
                        System.out.print("\n[MCP Tool Call: " + mcpCall.serverName() + "/" + mcpCall.name() + "]\n");
                    } else if (delta instanceof Events.McpServerToolResultDelta mcpResult) {
                        System.out.print("\n[MCP Tool Result: " + mcpResult.serverName() + "/" + mcpResult.name() + "]\n");
                    } else if (delta instanceof Events.GoogleMapsCallDelta mapsCall) {
                        System.out.print("\n[Google Maps Call: " + mapsCall.arguments() + "]\n");
                    } else if (delta instanceof Events.GoogleMapsResultDelta mapsResult) {
                        System.out.print("\n[Google Maps Result: " + (mapsResult.result() != null ? mapsResult.result().size() : 0) + " results]\n");
                    } else if (delta instanceof Events.ImageDelta imageDelta) {
                        System.out.print("\n[Image Delta: " + imageDelta.mimeType() + "]\n");
                    } else if (delta instanceof Events.AudioDelta audioDelta) {
                        System.out.print("\n[Audio Delta: " + audioDelta.mimeType() + "]\n");
                    } else if (delta instanceof Events.VideoDelta videoDelta) {
                        System.out.print("\n[Video Delta: " + videoDelta.mimeType() + "]\n");
                    } else if (delta instanceof Events.DocumentDelta documentDelta) {
                        System.out.print("\n[Document Delta: " + documentDelta.mimeType() + "]\n");
                    } else if (delta instanceof Events.TextAnnotationDelta textAnnotation) {
                        System.out.print("\n[Text Annotation: " + (textAnnotation.annotations() != null ? textAnnotation.annotations().size() : 0) + " annotations]\n");
                    } else if (delta instanceof Events.UnknownDelta unknownDelta) {
                        System.out.print("\n[Unknown Delta: " + unknownDelta.raw() + "]\n");
                    } else {
                        System.out.print("\n[Other Delta: " + delta.getClass().getSimpleName() + "]\n");
                    }
                } else if (event instanceof Events.InteractionEvent interactionEvent) {
                    if (interactionEvent.eventType() == Events.EventType.INTERACTION_START) {
                        System.out.println("\n[Interaction Start: " + interactionEvent.interaction().id() + "]");
                    } else if (interactionEvent.eventType() == Events.EventType.INTERACTION_COMPLETE) {
                        capturedResearchId[0] = interactionEvent.interaction().id();
                        System.out.println("\n[Interaction Complete: " + capturedResearchId[0] + "]");
                        if (interactionEvent.interaction().outputs() != null) {
                            for (var content : interactionEvent.interaction().outputs()) {
                                if (content instanceof io.github.glaforge.gemini.interactions.model.Content.TextContent textContent) {
                                    System.out.println(textContent.text());
                                    researchTextBuilder.append(textContent.text());
                                }
                            }
                        }
                    }
                } else if (event instanceof Events.InteractionStatusUpdate statusUpdate) {
                    System.out.println("\n[Interaction Status Update: " + statusUpdate.interactionId() + " -> " + statusUpdate.status() + "]");
                } else if (event instanceof Events.ContentStart contentStart) {
                    System.out.println("\n[Content Start: index " + contentStart.index() + "]");
                } else if (event instanceof Events.ContentStop contentStop) {
                    System.out.println("\n[Content Stop: index " + contentStop.index() + "]");
                } else if (event instanceof Events.ErrorEvent errorEvent) {
                    System.err.println("\n[Error Event: " + errorEvent.error().code() + " - " + errorEvent.error().message() + "]");
                } else {
                    System.out.println("\n[Other Event: " + event.getClass().getSimpleName() + "]");
                }
            });
        } catch (Exception e) {
            System.err.println("\nStream error: " + e.toString());
            e.printStackTrace();
        }

        if (researchTextBuilder.isEmpty() && capturedResearchId[0] != null) {
            System.out.println("\n[Stream completed but no report received. Polling interaction for final result...]");
            Interaction finalInteraction = client.get(capturedResearchId[0]);
            while (finalInteraction.status() != Interaction.Status.COMPLETED && finalInteraction.status() != Interaction.Status.FAILED) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                finalInteraction = client.get(capturedResearchId[0]);
            }
            if (finalInteraction.outputs() != null) {
                for (var content : finalInteraction.outputs()) {
                    if (content instanceof io.github.glaforge.gemini.interactions.model.Content.TextContent textContent) {
                        researchTextBuilder.append(textContent.text());
                    }
                }
            }
        }

        String researchText = researchTextBuilder.toString();
        System.out.println("\nResearch Result:\n\n" + researchText);

        assertTrue(completed, "Research interaction stream should complete successfully");
        assertNotNull(researchText, "Research text should not be null");
        assertFalse(researchText.isEmpty(), "Research text should not be empty");

    }
}
