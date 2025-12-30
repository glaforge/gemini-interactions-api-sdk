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

import io.github.glaforge.gemini.interactions.model.Config.GenerationConfig;
import io.github.glaforge.gemini.interactions.model.Content.*;
import io.github.glaforge.gemini.interactions.model.Interaction.*;
import io.github.glaforge.gemini.interactions.model.Interaction.Role;
import io.github.glaforge.gemini.interactions.model.Tool.Function;
import io.github.glaforge.gemini.interactions.model.Config.*;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
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
                .apiKey(System.getenv("GEMINI_API_KEY"))
                .build();
            assertNotNull(client);

            // 2. Simple Text Interaction
            ModelInteractionParams simpleRequest = ModelInteractionParams.builder()
                .model("gemini-2.5-flash")
                .input("Hello, how are you?")
                .build();
            assertNotNull(simpleRequest);

            // 3. Multi-turn Interaction
            ModelInteractionParams multiTurnRequest = ModelInteractionParams.builder()
                .model("gemini-2.5-flash")
                .input(
                    new Turn(Role.USER, "Hello!"),
                    new Turn(Role.MODEL, "Hi there!"),
                    new Turn(Role.USER, "What is the capital of France?")
                )
                .build();
            assertNotNull(multiTurnRequest);

            // 4. Multimodal Interaction (Text + Image)
            ModelInteractionParams multimodalRequest = ModelInteractionParams.builder()
                .model("gemini-2.5-flash")
                .input(
                    new TextContent("What is in this picture?"),
                    new ImageContent("BASE64_DATA", "image/png")
                )
                .build();
            assertNotNull(multimodalRequest);

            // 5. Function Calling
            Function weatherTool = new Function(
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

            ModelInteractionParams toolRequest = ModelInteractionParams.builder()
                .model("gemini-2.5-flash")
                .input("Weather in London?")
                .tools(weatherTool)
                .build();
            assertNotNull(toolRequest);

            // 6. Config
            GenerationConfig config = new GenerationConfig(
                0.7, // temp
                0.95, // topP
                null, // seed
                List.of("STOP"),
                null, // tool_choice
                ThinkingLevel.LOW, // thinking_level
                ThinkingSummaries.AUTO, // thinking_summaries
                1000,
                null // speech
            );
            assertNotNull(config);

            // 7. YAML Output
            ModelInteractionParams yamlOutputRequest = ModelInteractionParams.builder()
                .model("gemini-3-flash-preview")
                .input("Create a YAML frontmapper for a static Hugo website about cats")
                .responseMimeType("application/yaml")
                .responseFormat(Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "title", Map.of("type", "string"),
                        "date", Map.of("type", "string"),
                        "draft", Map.of("type", "boolean"),
                        "tags", Map.of("type", "array", "items", Map.of("type", "string")),
                        "categories", Map.of("type", "array", "items", Map.of("type", "string")),
                        "author", Map.of("type", "string"),
                        "description", Map.of("type", "string")
                    ),
                    "required", List.of("title", "date", "draft", "tags", "categories", "author", "description"))
                )
                .build();
            assertNotNull(yamlOutputRequest);
            var interaction = client.create(yamlOutputRequest);
            assertNotNull("Outputs of YAML request: " + interaction.outputs());
            interaction.outputs().forEach(output -> {
                if (output instanceof TextContent textContent) {
                    System.out.println(textContent.text());
                }
            });

            System.out.println("All requests built successfully.");
        });
    }
}
