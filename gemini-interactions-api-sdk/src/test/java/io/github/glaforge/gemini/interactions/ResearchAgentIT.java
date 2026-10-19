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
import io.github.glaforge.gemini.interactions.model.Step;
import io.github.glaforge.gemini.interactions.model.Events;
import io.github.glaforge.gemini.interactions.model.Content;
import io.github.glaforge.gemini.interactions.model.Interaction;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
public class ResearchAgentIT {

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
                .agent("deep-research-preview-04-2026")
                .agentConfig(
                        DeepResearchAgentConfig.builder()
                                .thinkingSummaries(ThinkingSummaries.AUTO)
                                .visualization(Visualization.AUTO)
                                .collaborativePlanning(false)
                                .build())
                .input(researchGoal)
                .background(true)
                .store(true)
                .stream(true)
                .build();

        System.out.println("Starting streaming research...");

        StringBuilder researchTextBuilder = new StringBuilder();
        String[] capturedResearchId = new String[1];
        String[] lastEventId = new String[1];

        boolean[] completed = new boolean[1];

        Consumer<Events> eventHandler = event -> {
            if (event.eventId() != null) {
                lastEventId[0] = event.eventId();
            }
            if (event instanceof Events.StepDelta deltaEvent) {
                handleDelta(deltaEvent.delta(), researchTextBuilder);
            } else if (event instanceof Events.ContentDelta contentDeltaEvent) {
                handleDelta(contentDeltaEvent.delta(), researchTextBuilder);
            } else if (event instanceof Events.InteractionCreated interactionCreated) {
                if (interactionCreated.interaction() != null && interactionCreated.interaction().id() != null) {
                    capturedResearchId[0] = interactionCreated.interaction().id();
                }
                System.out.println("\n[Interaction Start: " + capturedResearchId[0] + "]");
            } else if (event instanceof Events.InteractionCompleted interactionCompleted) {
                if (interactionCompleted.interaction() != null && interactionCompleted.interaction().id() != null) {
                    capturedResearchId[0] = interactionCompleted.interaction().id();
                }
                completed[0] = true;
                System.out.println("\n[Interaction Complete: " + capturedResearchId[0] + "]");
                if (interactionCompleted.interaction() != null && interactionCompleted.interaction().steps() != null) {
                    interactionCompleted.interaction().steps().stream()
                            .filter(s -> s instanceof Step.ModelOutputStep)
                            .flatMap(s -> ((Step.ModelOutputStep) s).content().stream())
                            .filter(c -> c instanceof Content.TextContent)
                            .forEach(c -> {
                                Content.TextContent textContent = (Content.TextContent) c;
                                System.out.println(textContent.text());
                                researchTextBuilder.append(textContent.text());
                            });
                }
            } else if (event instanceof Events.InteractionStatusUpdate statusUpdate) {
                if (statusUpdate.interactionId() != null) {
                    capturedResearchId[0] = statusUpdate.interactionId();
                }
                System.out.println("\n[Interaction Status Update: " + statusUpdate.interactionId() + " -> "
                        + statusUpdate.status() + "]");
            } else if (event instanceof Events.StepStart stepStart) {
                System.out.println("\n[Step Start: index " + stepStart.index() + "]");
            } else if (event instanceof Events.StepStop stepStop) {
                System.out.println("\n[Step Stop: index " + stepStop.index() + "]");
            } else if (event instanceof Events.ContentStart contentStart) {
                System.out.println("\n[Content Start: index " + contentStart.index() + "]");
            } else if (event instanceof Events.ContentStop contentStop) {
                System.out.println("\n[Content Stop: index " + contentStop.index() + "]");
            } else if (event instanceof Events.ErrorEvent errorEvent) {
                System.err.println("\n[Error Event: " + errorEvent.error().code() + " - "
                        + errorEvent.error().message() + "]");
                TransientApiErrorExtension.abortIfRateLimitOrTransient(errorEvent);
            } else {
                System.out.println("\n[Other Event: " + event.getClass().getSimpleName() + "]");
            }
        };

        try (var eventStream = client.stream(researchParams)) {
            eventStream.forEach(eventHandler);
        } catch (GeminiInteractionsException e) {
            if (TransientApiErrorExtension.isRateLimitOrTransient(e) || capturedResearchId[0] == null) {
                throw e;
            }
            System.err.println("\nStream error: " + e.toString());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("\nStream error: " + e.toString());
            e.printStackTrace();
        }

        // If the stream dropped before completion (e.g. TCP connection reset during long thinking),
        // try resuming the stream first, and then fall back to polling the interaction until finished.
        if (!completed[0] && capturedResearchId[0] != null) {
            System.out.println("\n[Stream disconnected before completion. Attempting to resume stream for "
                    + capturedResearchId[0] + " from lastEventId=" + lastEventId[0] + "...]");
            try (var resumedStream = client.stream(capturedResearchId[0], lastEventId[0])) {
                resumedStream.forEach(eventHandler);
            } catch (Exception e) {
                System.err.println("\nResumed stream error: " + e.toString());
            }
        }

        if ((!completed[0] || researchTextBuilder.isEmpty()) && capturedResearchId[0] != null) {
            System.out.println("\n[Polling interaction " + capturedResearchId[0] + " for final result...]");
            Interaction finalInteraction = client.get(capturedResearchId[0]);
            while (!finalInteraction.status().isFinished()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                finalInteraction = client.get(capturedResearchId[0]);
            }
            if (finalInteraction.status() == Interaction.Status.COMPLETED) {
                completed[0] = true;
            }
            if (researchTextBuilder.isEmpty() && finalInteraction.steps() != null) {
                finalInteraction.steps().stream()
                        .filter(s -> s instanceof Step.ModelOutputStep)
                        .flatMap(s -> ((Step.ModelOutputStep) s).content().stream())
                        .filter(c -> c instanceof Content.TextContent)
                        .forEach(c -> {
                            Content.TextContent textContent = (Content.TextContent) c;
                            researchTextBuilder.append(textContent.text());
                        });
            }
        }

        String researchText = researchTextBuilder.toString();
        System.out.println("\nResearch Result:\n\n" + researchText);

        assertTrue(completed[0], "Research interaction stream should complete successfully");
        assertNotNull(researchText, "Research text should not be null");
        assertFalse(researchText.isEmpty(), "Research text should not be empty");

    }

    private void handleDelta(Events.Delta delta, StringBuilder researchTextBuilder) {
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
            System.out.print("\n[Google Search Result: "
                    + (searchResult.result() != null ? searchResult.result().size() : 0) + " results]\n");
        } else if (delta instanceof Events.UrlContextCallDelta urlCall) {
            System.out.print("\n[URL Context Call: " + urlCall.arguments() + "]\n");
        } else if (delta instanceof Events.UrlContextResultDelta urlResult) {
            System.out.print("\n[URL Context Result: "
                    + (urlResult.result() != null ? urlResult.result().size() : 0) + " results]\n");
        } else if (delta instanceof Events.CodeExecutionCallDelta codeCall) {
            System.out.print("\n[Code Execution Call: " + codeCall.arguments() + "]\n");
        } else if (delta instanceof Events.CodeExecutionResultDelta codeResult) {
            System.out.print("\n[Code Execution Result: " + codeResult.result() + "]\n");
        } else if (delta instanceof Events.FunctionCallDelta functionCall) {
            System.out.print("\n[Function Call: " + functionCall.name() + " with args "
                    + functionCall.arguments() + "]\n");
        } else if (delta instanceof Events.FunctionResultDelta functionResult) {
            System.out.print("\n[Function Result: " + functionResult.name() + " -> "
                    + functionResult.result() + "]\n");
        } else if (delta instanceof Events.ArgumentsDelta argumentsDelta) {
            System.out.print("\n[Arguments Delta: " + argumentsDelta.arguments() + "]\n");
        } else if (delta instanceof Events.FileSearchCallDelta fileCall) {
            System.out.print("\n[File Search Call: " + fileCall.signature() + "]\n");
        } else if (delta instanceof Events.FileSearchResultDelta fileResult) {
            System.out.print("\n[File Search Result: "
                    + (fileResult.result() != null ? fileResult.result().size() : 0) + " results]\n");
        } else if (delta instanceof Events.McpServerToolCallDelta mcpCall) {
            System.out.print("\n[MCP Tool Call: " + mcpCall.serverName() + "/" + mcpCall.name() + "]\n");
        } else if (delta instanceof Events.McpServerToolResultDelta mcpResult) {
            System.out.print(
                    "\n[MCP Tool Result: " + mcpResult.serverName() + "/" + mcpResult.name() + "]\n");
        } else if (delta instanceof Events.GoogleMapsCallDelta mapsCall) {
            System.out.print("\n[Google Maps Call: " + mapsCall.arguments() + "]\n");
        } else if (delta instanceof Events.GoogleMapsResultDelta mapsResult) {
            System.out.print("\n[Google Maps Result: "
                    + (mapsResult.result() != null ? mapsResult.result().size() : 0) + " results]\n");
        } else if (delta instanceof Events.ImageDelta imageDelta) {
            System.out.print("\n[Image Delta: " + imageDelta.mimeType() + "]\n");
        } else if (delta instanceof Events.AudioDelta audioDelta) {
            System.out.print("\n[Audio Delta: " + audioDelta.mimeType() + "]\n");
        } else if (delta instanceof Events.VideoDelta videoDelta) {
            System.out.print("\n[Video Delta: " + videoDelta.mimeType() + "]\n");
        } else if (delta instanceof Events.DocumentDelta documentDelta) {
            System.out.print("\n[Document Delta: " + documentDelta.mimeType() + "]\n");
        } else if (delta instanceof Events.TextAnnotationDelta textAnnotation) {
            System.out.print("\n[Text Annotation: "
                    + (textAnnotation.annotations() != null ? textAnnotation.annotations().size() : 0)
                    + " annotations]\n");
        } else if (delta instanceof Events.UnknownDelta unknownDelta) {
            System.out.print("\n[Unknown Delta: " + unknownDelta.raw() + "]\n");
        } else {
            System.out.print("\n[Other Delta: " + delta.getClass().getSimpleName() + "]\n");
        }
    }
}
