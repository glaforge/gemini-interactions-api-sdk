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

import io.github.glaforge.gemini.interactions.model.Content.TextContent;
import io.github.glaforge.gemini.interactions.model.Events;
import io.github.glaforge.gemini.interactions.model.Events.StepDelta;
import io.github.glaforge.gemini.interactions.model.Events.ToolSearchCallDelta;
import io.github.glaforge.gemini.interactions.model.Events.ToolSearchResultDelta;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.ModelOption;
import io.github.glaforge.gemini.interactions.model.Step;
import io.github.glaforge.gemini.interactions.model.Step.FunctionCallStep;
import io.github.glaforge.gemini.interactions.model.Step.FunctionResultStep;
import io.github.glaforge.gemini.interactions.model.Step.ToolSearchCallStep;
import io.github.glaforge.gemini.interactions.model.Step.ToolSearchResultStep;
import io.github.glaforge.gemini.interactions.model.Step.UserInputStep;
import io.github.glaforge.gemini.interactions.model.Tool;
import io.github.glaforge.gemini.interactions.model.Tool.Function;
import io.github.glaforge.gemini.interactions.model.Tool.ToolSearch;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Live integration tests for Tool Retrieval (server-side, stateless replay, SSE streaming, and client-side).
 */
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class ToolRetrievalIT {

    private final GeminiInteractionsClient client = GeminiInteractionsClient.builder()
        .apiKey(System.getenv("GEMINI_API_KEY"))
        .build();

    private List<Tool> serverSideTools() {
        return List.of(
            ToolSearch.server(),
            Function.builder()
                .name("convert_currency")
                .description("Convert an amount from one currency to another using live exchange rates.")
                .shortDescription("Convert currency amounts.")
                .deferLoading(true)
                .parameters(Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "amount", Map.of("type", "number"),
                        "from", Map.of("type", "string"),
                        "to", Map.of("type", "string")
                    ),
                    "required", List.of("amount", "from", "to")
                ))
                .build(),
            Function.builder()
                .name("get_stock_price")
                .description("Get the current stock price for a ticker symbol.")
                .shortDescription("Get stock price.")
                .deferLoading(true)
                .parameters(Map.of(
                    "type", "object",
                    "properties", Map.of("ticker", Map.of("type", "string")),
                    "required", List.of("ticker")
                ))
                .build()
        );
    }

    @Test
    void testServerSideToolRetrievalStatelessMultiTurn() {
        List<Tool> tools = serverSideTools();
        List<Step> history = new ArrayList<>();
        history.add(new UserInputStep("user_input", List.of(new TextContent("Convert 100 USD to EUR."))));

        Interaction turn1 = client.create(ModelInteractionParams.builder()
            .model(ModelOption.GEMINI_4_HYDROGEN)
            .store(false)
            .inputSteps(history)
            .tools(tools)
            .build());

        assertNotNull(turn1.steps());
        assertTrue(turn1.steps().stream().anyMatch(s -> s instanceof ToolSearchCallStep));
        assertTrue(turn1.steps().stream().anyMatch(s -> s instanceof ToolSearchResultStep));

        FunctionCallStep fnCall = turn1.steps().stream()
            .filter(FunctionCallStep.class::isInstance)
            .map(FunctionCallStep.class::cast)
            .findFirst()
            .orElseThrow();
        assertEquals("convert_currency", fnCall.name());

        // Replay all steps unchanged (preserving signatures) + function_result
        history.addAll(turn1.steps());
        history.add(new FunctionResultStep(
            "function_result",
            fnCall.id(),
            fnCall.name(),
            null,
            List.of(new TextContent("{\"amount\": 92.4, \"currency\": \"EUR\"}"))
        ));

        Interaction turn2 = client.create(ModelInteractionParams.builder()
            .model(ModelOption.GEMINI_4_HYDROGEN)
            .store(false)
            .inputSteps(history)
            .tools(tools)
            .build());

        assertEquals(Interaction.Status.COMPLETED, turn2.status());
        assertNotNull(turn2.outputText());
        assertTrue(turn2.outputText().contains("92.4"));
    }

    @Test
    void testServerSideToolRetrievalStreaming() {
        List<Events> events = client.stream(ModelInteractionParams.builder()
            .model(ModelOption.GEMINI_4_HYDROGEN)
            .stream(true)
            .input("Convert 100 USD to EUR.")
            .tools(serverSideTools())
            .build()).toList();

        assertFalse(events.isEmpty());
        boolean sawCallDelta = events.stream()
            .filter(StepDelta.class::isInstance)
            .map(StepDelta.class::cast)
            .anyMatch(e -> e.delta() instanceof ToolSearchCallDelta);
        boolean sawResultDelta = events.stream()
            .filter(StepDelta.class::isInstance)
            .map(StepDelta.class::cast)
            .anyMatch(e -> e.delta() instanceof ToolSearchResultDelta);

        assertTrue(sawCallDelta, "Expected a ToolSearchCallDelta SSE event");
        assertTrue(sawResultDelta, "Expected a ToolSearchResultDelta SSE event");
    }

    @Test
    void testClientSideToolRetrieval() {
        Tool clientSearch = ToolSearch.client(
            "search_tool_registry",
            "Search the internal tool registry for functions.",
            Map.of(
                "type", "object",
                "properties", Map.of("query", Map.of("type", "string")),
                "required", List.of("query")
            )
        );

        Interaction turn1 = client.create(ModelInteractionParams.builder()
            .model(ModelOption.GEMINI_4_HYDROGEN)
            .input("Look up the billing history for user 123.")
            .tools(clientSearch)
            .build());

        FunctionCallStep searchCall = turn1.steps().stream()
            .filter(FunctionCallStep.class::isInstance)
            .map(FunctionCallStep.class::cast)
            .filter(fc -> "search_tool_registry".equals(fc.name()))
            .findFirst()
            .orElseThrow();

        Function billingFn = Function.builder()
            .name("get_billing_history")
            .description("Retrieve billing history for a given user.")
            .parameters(Map.of(
                "type", "object",
                "properties", Map.of("user_id", Map.of("type", "integer")),
                "required", List.of("user_id")
            ))
            .build();

        Interaction turn2 = client.create(ModelInteractionParams.builder()
            .model(ModelOption.GEMINI_4_HYDROGEN)
            .previousInteractionId(turn1.id())
            .tools(clientSearch)
            .inputSteps(List.of(new FunctionResultStep(
                "function_result",
                searchCall.id(),
                searchCall.name(),
                null,
                List.of(billingFn)
            )))
            .build());

        FunctionCallStep billingCall = turn2.steps().stream()
            .filter(FunctionCallStep.class::isInstance)
            .map(FunctionCallStep.class::cast)
            .filter(fc -> "get_billing_history".equals(fc.name()))
            .findFirst()
            .orElseThrow();
        assertEquals(123, ((Number) billingCall.arguments().get("user_id")).intValue());
    }
}
