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

import io.github.glaforge.gemini.interactions.model.Events;
import io.github.glaforge.gemini.interactions.model.Events.DeltaType;
import io.github.glaforge.gemini.interactions.model.Events.StepDelta;
import io.github.glaforge.gemini.interactions.model.Events.StepStart;
import io.github.glaforge.gemini.interactions.model.Events.ToolSearchCallDelta;
import io.github.glaforge.gemini.interactions.model.Events.ToolSearchResultDelta;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.ModelOption;
import io.github.glaforge.gemini.interactions.model.Step;
import io.github.glaforge.gemini.interactions.model.Step.FunctionResultStep;
import io.github.glaforge.gemini.interactions.model.Step.ToolSearchCallArguments;
import io.github.glaforge.gemini.interactions.model.Step.ToolSearchCallStep;
import io.github.glaforge.gemini.interactions.model.Step.ToolSearchResultStep;
import io.github.glaforge.gemini.interactions.model.Tool;
import io.github.glaforge.gemini.interactions.model.Tool.Function;
import io.github.glaforge.gemini.interactions.model.Tool.McpServer;
import io.github.glaforge.gemini.interactions.model.Tool.ToolSearch;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for Tool Retrieval ({@link Tool.ToolSearch}, {@code defer_loading}, {@code short_description},
 * {@link Step.ToolSearchCallStep}, {@link Step.ToolSearchResultStep}, and SSE deltas).
 */
class ToolRetrievalTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();
    private MockWebServer mockServer;
    private GeminiInteractionsClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
        String baseUrl = mockServer.url("").toString();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        client = GeminiInteractionsClient.builder()
            .baseUrl(baseUrl)
            .apiKey("test-api-key")
            .build();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void testToolSearchServerAndClientSerialization() {
        ToolSearch serverSearch = ToolSearch.server();
        String serverJson = mapper.writeValueAsString(serverSearch);
        JsonNode serverNode = mapper.readTree(serverJson);
        assertEquals("tool_search", serverNode.get("type").asText());
        assertFalse(serverNode.has("execution"));
        assertFalse(serverNode.has("name"));
        assertFalse(serverNode.has("description"));
        assertFalse(serverNode.has("parameters"));

        Tool parsedServer = mapper.readValue(serverJson, Tool.class);
        assertInstanceOf(ToolSearch.class, parsedServer);

        Map<String, Object> searchSchema = Map.of(
            "type", "object",
            "properties", Map.of("query", Map.of("type", "string")),
            "required", List.of("query")
        );
        ToolSearch clientSearch = ToolSearch.client(
            "search_tool_registry",
            "Search the internal tool registry for functions.",
            searchSchema
        );
        String clientJson = mapper.writeValueAsString(clientSearch);
        JsonNode clientNode = mapper.readTree(clientJson);
        assertEquals("tool_search", clientNode.get("type").asText());
        assertEquals("client", clientNode.get("execution").asText());
        assertEquals("search_tool_registry", clientNode.get("name").asText());
        assertEquals("Search the internal tool registry for functions.", clientNode.get("description").asText());
        assertEquals("object", clientNode.get("parameters").get("type").asText());

        ToolSearch parsedClient = assertInstanceOf(ToolSearch.class, mapper.readValue(clientJson, Tool.class));
        assertEquals(ToolSearch.Execution.CLIENT, parsedClient.execution());
        assertEquals("search_tool_registry", parsedClient.name());
        assertEquals("Search the internal tool registry for functions.", parsedClient.description());
        assertNotNull(parsedClient.parameters());
    }

    @Test
    void testFunctionAndMcpServerDeferLoadingAndShortDescription() {
        Function deferredFn = Function.builder()
            .name("convert_currency")
            .description("Convert an amount between currencies using live mid-market exchange rates.")
            .shortDescription("Convert currency amounts.")
            .deferLoading(true)
            .parameters(Map.of(
                "type", "object",
                "properties", Map.of("amount", Map.of("type", "number")),
                "required", List.of("amount")
            ))
            .build();

        String fnJson = mapper.writeValueAsString(deferredFn);
        JsonNode fnNode = mapper.readTree(fnJson);
        assertEquals("function", fnNode.get("type").asText());
        assertEquals("convert_currency", fnNode.get("name").asText());
        assertTrue(fnNode.get("defer_loading").asBoolean());
        assertEquals("Convert currency amounts.", fnNode.get("short_description").asText());

        Function parsedFn = assertInstanceOf(Function.class, mapper.readValue(fnJson, Tool.class));
        assertEquals(Boolean.TRUE, parsedFn.deferLoading());
        assertEquals("Convert currency amounts.", parsedFn.shortDescription());

        // Backward-compatible Function constructor omits defer_loading and short_description
        Function legacyFn = new Function("ping", "Ping host", Map.of("type", "object"));
        JsonNode legacyFnNode = mapper.readTree(mapper.writeValueAsString(legacyFn));
        assertFalse(legacyFnNode.has("defer_loading"));
        assertFalse(legacyFnNode.has("short_description"));

        McpServer deferredMcp = McpServer.builder()
            .name("weather_service")
            .url("https://example.com/mcp")
            .deferLoading(true)
            .build();

        String mcpJson = mapper.writeValueAsString(deferredMcp);
        JsonNode mcpNode = mapper.readTree(mcpJson);
        assertEquals("mcp_server", mcpNode.get("type").asText());
        assertTrue(mcpNode.get("defer_loading").asBoolean());

        McpServer parsedMcp = assertInstanceOf(McpServer.class, mapper.readValue(mcpJson, Tool.class));
        assertEquals(Boolean.TRUE, parsedMcp.deferLoading());

        // Backward-compatible McpServer constructor omits defer_loading
        McpServer legacyMcp = new McpServer("docs", "https://example.com/docs-mcp");
        assertNull(legacyMcp.deferLoading());
        assertFalse(mapper.readTree(mapper.writeValueAsString(legacyMcp)).has("defer_loading"));
    }

    @Test
    void testToolSearchStepsRoundTripPreservesSignature() {
        ToolSearchCallStep callStep = new ToolSearchCallStep(
            "tool_search_call",
            "call_123",
            new ToolSearchCallArguments(List.of("convert_currency", "weather_service:get_weather")),
            "sig_call_opaque"
        );

        String callJson = mapper.writeValueAsString(callStep);
        JsonNode callNode = mapper.readTree(callJson);
        assertEquals("tool_search_call", callNode.get("type").asText());
        assertEquals("call_123", callNode.get("id").asText());
        assertEquals("sig_call_opaque", callNode.get("signature").asText());
        assertEquals(2, callNode.get("arguments").get("function_names").size());

        ToolSearchCallStep parsedCall = assertInstanceOf(ToolSearchCallStep.class, mapper.readValue(callJson, Step.class));
        assertEquals("call_123", parsedCall.id());
        assertEquals("sig_call_opaque", parsedCall.signature());
        assertEquals(List.of("convert_currency", "weather_service:get_weather"), parsedCall.arguments().functionNames());

        Function retrievedFn = Function.builder()
            .name("default_api:convert_currency")
            .description("Convert an amount between currencies.")
            .parameters(Map.of("type", "object"))
            .build();
        ToolSearchResultStep resultStep = new ToolSearchResultStep(
            "tool_search_result",
            "call_123",
            List.of(retrievedFn),
            "sig_result_opaque"
        );

        String resultJson = mapper.writeValueAsString(resultStep);
        JsonNode resultNode = mapper.readTree(resultJson);
        assertEquals("tool_search_result", resultNode.get("type").asText());
        assertEquals("call_123", resultNode.get("call_id").asText());
        assertEquals("sig_result_opaque", resultNode.get("signature").asText());
        assertEquals("default_api:convert_currency", resultNode.get("result").get(0).get("name").asText());

        ToolSearchResultStep parsedResult = assertInstanceOf(ToolSearchResultStep.class, mapper.readValue(resultJson, Step.class));
        assertEquals("call_123", parsedResult.callId());
        assertEquals("sig_result_opaque", parsedResult.signature());
        assertEquals(1, parsedResult.result().size());
        assertEquals("default_api:convert_currency", parsedResult.result().get(0).name());
    }

    @Test
    void testClientSideFunctionResultCarryingToolFunctions() {
        Function discovered = Function.builder()
            .name("get_billing_history")
            .description("Retrieve billing history for a user.")
            .parameters(Map.of(
                "type", "object",
                "properties", Map.of("user_id", Map.of("type", "integer")),
                "required", List.of("user_id")
            ))
            .build();

        FunctionResultStep fnResult = new FunctionResultStep(
            "function_result",
            "call_search_1",
            "search_tool_registry",
            null,
            List.of(discovered)
        );

        String json = mapper.writeValueAsString(fnResult);
        JsonNode node = mapper.readTree(json);
        assertEquals("function_result", node.get("type").asText());
        assertEquals("call_search_1", node.get("call_id").asText());
        assertTrue(node.get("result").isArray());
        assertEquals("function", node.get("result").get(0).get("type").asText());
        assertEquals("get_billing_history", node.get("result").get(0).get("name").asText());
    }

    @Test
    void testSseStreamingToolSearchCallAndResultDeltas() {
        String sseBody = """
            event: interaction.created
            data: {"interaction": {"id": "int_stream_1", "object": "interaction", "model": "gemini-4-hydrogen"}, "event_type": "interaction.created"}

            event: step.start
            data: {"index": 0, "step": {"id": "call_100", "signature": "", "type": "tool_search_call"}, "event_type": "step.start"}

            event: step.delta
            data: {"index": 0, "delta": {"signature": "sig_call_1", "type": "tool_search_call", "arguments": {"function_names": ["convert_currency"]}}, "event_type": "step.delta"}

            event: step.stop
            data: {"index": 0, "event_type": "step.stop"}

            event: step.start
            data: {"index": 1, "step": {"call_id": "call_100", "signature": "", "type": "tool_search_result"}, "event_type": "step.start"}

            event: step.delta
            data: {"index": 1, "delta": {"signature": "sig_res_1", "type": "tool_search_result", "result": [{"name": "default_api:convert_currency", "description": "Convert currency.", "parameters": {"type": "object"}, "type": "function"}]}, "event_type": "step.delta"}

            event: step.stop
            data: {"index": 1, "event_type": "step.stop"}

            event: done
            data: [DONE]
            """;

        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "text/event-stream")
            .setBody(sseBody));

        ModelInteractionParams params = ModelInteractionParams.builder()
            .model(ModelOption.GEMINI_4_HYDROGEN)
            .stream(true)
            .input("Convert 100 USD to EUR.")
            .tools(
                ToolSearch.server(),
                Function.builder()
                    .name("convert_currency")
                    .description("Convert currency.")
                    .deferLoading(true)
                    .parameters(Map.of("type", "object"))
                    .build()
            )
            .build();

        List<Events> events = client.stream(params).toList();
        assertEquals(7, events.size());

        StepStart callStart = assertInstanceOf(StepStart.class, events.get(1));
        ToolSearchCallStep startCallStep = assertInstanceOf(ToolSearchCallStep.class, callStart.step());
        assertEquals("call_100", startCallStep.id());

        StepDelta callDeltaEvent = assertInstanceOf(StepDelta.class, events.get(2));
        ToolSearchCallDelta callDelta = assertInstanceOf(ToolSearchCallDelta.class, callDeltaEvent.delta());
        assertEquals(DeltaType.TOOL_SEARCH_CALL, callDelta.type());
        assertEquals("sig_call_1", callDelta.signature());
        assertEquals(List.of("convert_currency"), callDelta.arguments().functionNames());

        StepStart resultStart = assertInstanceOf(StepStart.class, events.get(4));
        ToolSearchResultStep startResultStep = assertInstanceOf(ToolSearchResultStep.class, resultStart.step());
        assertEquals("call_100", startResultStep.callId());

        StepDelta resultDeltaEvent = assertInstanceOf(StepDelta.class, events.get(5));
        ToolSearchResultDelta resultDelta = assertInstanceOf(ToolSearchResultDelta.class, resultDeltaEvent.delta());
        assertEquals(DeltaType.TOOL_SEARCH_RESULT, resultDelta.type());
        assertEquals("sig_res_1", resultDelta.signature());
        assertEquals(1, resultDelta.result().size());
        assertEquals("default_api:convert_currency", resultDelta.result().get(0).name());
    }
}
