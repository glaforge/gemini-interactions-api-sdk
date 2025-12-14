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
import io.github.glaforge.gemini.interactions.model.Content.FunctionCallContent;
import io.github.glaforge.gemini.interactions.model.Content.TextContent;
import io.github.glaforge.gemini.interactions.model.Content.FunctionResultContent;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams;
import io.github.glaforge.gemini.interactions.model.Tool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WeatherToolTest {

    // Local function implementation matching the schema
    private String getWeather(String location) {
        if (location == null) {
            return "Unknown location";
        }
        if (location.toLowerCase().contains("london")) {
            return "Rainy, 15°C";
        }
        if (location.toLowerCase().contains("mountain view")) {
            return "Sunny, 25°C";
        }
        return "Sunny, 20°C"; // Default
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
    void testWeatherFunctionCall() throws IOException, InterruptedException {
        GeminiInteractionsClient client = GeminiInteractionsClient.builder()
            .apiKey(System.getenv("GEMINI_API_KEY"))
            .build();

        // 1. Define the tool
        // Note: The schema format depends on what the model expects.
        // Usually it follows OpenAPI schema.
        Tool.Function weatherFunc = new Tool.Function(
            "get_weather",
            "Get the current weather in a given location",
            Map.of(
                "type", "object",
                "properties", Map.of(
                    "location", Map.of(
                        "type", "string",
                        "description", "The city and state, e.g. San Francisco, CA"
                    )
                ),
                "required", List.of("location")
            )
        );

        // Tool is an interface, we need to pass a list of Tool objects.
        List<Tool> tools = List.of(weatherFunc);

        // 2. Initial Request
        InteractionParams.ModelInteractionParams createParams = InteractionParams.ModelInteractionParams.builder()
            .model("gemini-2.5-flash")
            .input("What is the weather in Mountain View, CA?")
            .tools(tools)
            .build();

        System.out.println("Sending initial request...");
        Interaction interaction = client.create(createParams);
        System.out.println("Initial response status: " + interaction.status());
        assertNotNull(interaction.outputs(), "Interaction outputs should not be null");

        // 3. Handle Function Call
        Content lastOutput = interaction.outputs().getLast();
        assertTrue(lastOutput instanceof FunctionCallContent, "Expected function call but got: " + lastOutput);

        FunctionCallContent call = (FunctionCallContent) lastOutput;
        System.out.println("Function call received: " + call.name());
        if ("get_weather".equals(call.name())) {
             String location = (String) call.arguments().get("location");
             System.out.println("Calling local weather function for: " + location);

             // Execute local function
             String weatherResult = getWeather(location);

             Map<String, Object> resultData = Map.of("weather", weatherResult);

             // 4. Send Function Result
             // We create a new interaction (continuation) referring to the previous ID
             System.out.println("Sending function result...");

             Content resultPart = new FunctionResultContent(
                 "function_result",
                 call.id(),
                 call.name(),
                     false,
                     resultData
                 );

                 InteractionParams.ModelInteractionParams continuationParams = InteractionParams.ModelInteractionParams.builder()
                     .model("gemini-2.5-flash")
                     .previousInteractionId(interaction.id())
                     // We pass the function result as the input for the next turn
                     .input(List.of(resultPart))
                     .build();

                 Interaction followUpInteraction = client.create(continuationParams);
                 System.out.println("Follow-up response status: " + followUpInteraction.status());

                 // 5. Verify Model Response
                 Content finalOutput = followUpInteraction.outputs().getLast();
                 if (finalOutput instanceof TextContent text) {
                     System.out.println("Model Answer: " + text.text());
                     assertTrue(text.text().contains("Sunny") || text.text().contains("25"), "Response should reflect weather data");
                 } else {
                     System.out.println("Unexpected final output type: " + finalOutput);
                 }
        }
    }
}
