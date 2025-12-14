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

import io.github.glaforge.gemini.interactions.model.*;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class UsageDemoTest {

    @Test
    public void testSdkCompilationAndUsage() {
        assertDoesNotThrow(() -> {
            // 1. Initialize Client
            GeminiInteractionsClient client = GeminiInteractionsClient.builder()
                .apiKey("YOUR_API_KEY")
                .build();
            assertNotNull(client);

            // 2. Simple Text Interaction
            InteractionParams.ModelInteractionParams simpleRequest = InteractionParams.ModelInteractionParams.builder()
                .model("gemini-2.5-flash")
                .input("Hello, how are you?")
                .build();
            assertNotNull(simpleRequest);

            // 3. Multi-turn Interaction
            InteractionParams.ModelInteractionParams multiTurnRequest = InteractionParams.ModelInteractionParams.builder()
                .model("gemini-2.5-flash")
                .input(List.of(
                    new Interaction.Turn(Interaction.Role.USER, "Hello!"),
                    new Interaction.Turn(Interaction.Role.MODEL, "Hi there!"),
                    new Interaction.Turn(Interaction.Role.USER, "What is the capital of France?")
                ))
                .build();
            assertNotNull(multiTurnRequest);

            // 4. Multimodal Interaction (Text + Image)
            InteractionParams.ModelInteractionParams multimodalRequest = InteractionParams.ModelInteractionParams.builder()
                .model("gemini-2.5-flash")
                .input(List.of(
                    new Content.TextContent("What is in this picture?"),
                    new Content.ImageContent("BASE64_DATA", "image/png")
                ))
                .build();
            assertNotNull(multimodalRequest);

            // 5. Function Calling
            Tool.Function weatherTool = new Tool.Function(
                "get_weather",
                "Get the current weather",
                Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "location", Map.of("type", "string")
                    ),
                    "required", List.of("location")
                )
            );

            InteractionParams.ModelInteractionParams toolRequest = InteractionParams.ModelInteractionParams.builder()
                .model("gemini-2.5-flash")
                .input("Weather in London?")
                .tools(List.of(weatherTool))
                .build();
            assertNotNull(toolRequest);

            // 6. Config
            Config.GenerationConfig config = new Config.GenerationConfig(
                0.7, // temp
                0.95, // topP
                null, // seed
                List.of("STOP"),
                null, // tool_choice
                Config.ThinkingLevel.LOW, // thinking_level
                Config.ThinkingSummaries.AUTO, // thinking_summaries
                1000,
                null // speech
            );
            assertNotNull(config);

            System.out.println("All requests built successfully.");
        });
    }
}
