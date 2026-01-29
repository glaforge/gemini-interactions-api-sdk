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
import io.github.glaforge.gemini.interactions.model.Events;
import io.github.glaforge.gemini.interactions.model.Content;
import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;
import io.github.glaforge.gemini.interactions.model.Tool.GoogleSearch;

import java.util.stream.Stream;

public class ResearchAgentStreamingDemo {

    public static void main(String[] args) {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null) {
            System.err.println("GEMINI_API_KEY environment variable not set");
            System.exit(1);
        }

        GeminiInteractionsClient client = GeminiInteractionsClient.builder()
                .apiKey(apiKey)
                .build();

        System.out.println("🚀 Starting Research Agent Streaming Demo");

        AgentInteractionParams researchParams = AgentInteractionParams.builder()
                .agent("deep-research-pro-preview-12-2025")
                .input("Find information about the release date of Java 25")
                .agentConfig(new DeepResearchAgentConfig(ThinkingSummaries.AUTO))
                .tools(new GoogleSearch())
                .stream(true)
                .background(true)
                .build();

        try {
            System.out.println("Connecting to stream...");
            Stream<Events> eventStream = client.stream(researchParams);

            eventStream.forEach(event -> {
                System.out.println("Received event: " + event.eventType());

                if (event instanceof Events.ContentDelta deltaInfo) {
                    Events.Delta delta = deltaInfo.delta();
                    if (delta instanceof Events.TextDelta textDelta) {
                        System.out.print(textDelta.text());
                    } else if (delta instanceof Events.ThoughtSummaryDelta thought) {
                        System.out.println("\n[Thinking] ");
                        if (thought.content() instanceof Content.TextContent textContent) {
                            System.out.print(textContent.text());
                        } else if (thought.content() instanceof Content.ImageContent imageContent) {
                            System.out.print(imageContent.uri());
                        }
                    } else {
                        System.out.println("\n[Delta] " + delta.type());
                    }
                } else if (event instanceof Events.InteractionStatusUpdate status) {
                    System.out.println("\n[Status] " + status.status());
                } else if (event instanceof Events.ErrorEvent error) {
                    System.err.println("\n[Error] " + error.error().message());
                }
            });

            System.out.println("\nDone.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
