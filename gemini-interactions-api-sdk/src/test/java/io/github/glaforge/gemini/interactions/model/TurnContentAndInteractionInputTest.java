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

package io.github.glaforge.gemini.interactions.model;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import io.github.glaforge.gemini.interactions.model.Interaction.Role;
import io.github.glaforge.gemini.interactions.model.Interaction.Turn;
import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TurnContentAndInteractionInputTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();

    @Test
    void testTurnContentString() throws Exception {
        TurnContent content = TurnContent.of("Hello world");
        assertTrue(content.isText());
        assertFalse(content.isParts());
        assertEquals("Hello world", content.text());
        assertNull(content.parts());
        assertEquals("Hello world", content.value());

        // Serialization
        String json = mapper.writeValueAsString(content);
        assertEquals("\"Hello world\"", json);

        // Deserialization
        TurnContent deserialized = mapper.readValue(json, TurnContent.class);
        assertTrue(deserialized.isText());
        assertEquals("Hello world", deserialized.text());
    }

    @Test
    void testTurnContentParts() throws Exception {
        Content part = new Content.TextContent("Part text");
        TurnContent content = TurnContent.of(List.of(part));
        assertFalse(content.isText());
        assertTrue(content.isParts());
        assertNull(content.text());
        assertEquals(1, content.parts().size());

        // Serialization
        String json = mapper.writeValueAsString(content);
        assertTrue(json.startsWith("["));
        assertTrue(json.contains("\"text\":\"Part text\""));

        // Deserialization
        TurnContent deserialized = mapper.readValue(json, TurnContent.class);
        assertTrue(deserialized.isParts());
        assertEquals(1, deserialized.parts().size());
        assertEquals("Part text", ((Content.TextContent) deserialized.parts().get(0)).text());
    }

    @Test
    void testTurnWithTurnContent() throws Exception {
        Turn turnString = Turn.user("Hello from user");
        assertEquals(Role.USER, turnString.role());
        assertTrue(turnString.content().isText());
        assertEquals("Hello from user", turnString.text());
        assertNull(turnString.parts());

        String jsonString = mapper.writeValueAsString(turnString);
        assertTrue(jsonString.contains("\"role\":\"user\""));
        assertTrue(jsonString.contains("\"content\":\"Hello from user\""));

        Turn deserializedString = mapper.readValue(jsonString, Turn.class);
        assertEquals(Role.USER, deserializedString.role());
        assertEquals("Hello from user", deserializedString.text());

        // Turn with parts
        Turn turnParts = Turn.model(List.of(new Content.TextContent("Model reply")));
        assertEquals(Role.MODEL, turnParts.role());
        assertTrue(turnParts.content().isParts());
        assertEquals(1, turnParts.parts().size());
        assertNull(turnParts.text());

        String jsonParts = mapper.writeValueAsString(turnParts);
        assertTrue(jsonParts.contains("\"role\":\"model\""));
        assertTrue(jsonParts.contains("\"content\":["));

        Turn deserializedParts = mapper.readValue(jsonParts, Turn.class);
        assertEquals(Role.MODEL, deserializedParts.role());
        assertEquals(1, deserializedParts.parts().size());
        assertEquals("Model reply", ((Content.TextContent) deserializedParts.parts().get(0)).text());
    }

    @Test
    void testInteractionInputString() throws Exception {
        InteractionInput input = InteractionInput.of("Explain quantum computing");
        assertTrue(input.isText());
        assertFalse(input.isContents());
        assertFalse(input.isTurns());
        assertFalse(input.isSteps());
        assertEquals("Explain quantum computing", input.text());
        assertNull(input.contents());
        assertNull(input.turns());
        assertNull(input.steps());

        String json = mapper.writeValueAsString(input);
        assertEquals("\"Explain quantum computing\"", json);

        InteractionInput deserialized = mapper.readValue(json, InteractionInput.class);
        assertTrue(deserialized.isText());
        assertEquals("Explain quantum computing", deserialized.text());
    }

    @Test
    void testInteractionInputContents() throws Exception {
        Content content = new Content.TextContent("Hello as content");
        InteractionInput input = InteractionInput.ofContents(List.of(content));
        assertTrue(input.isContents());
        assertFalse(input.isText());
        assertEquals(1, input.contents().size());

        String json = mapper.writeValueAsString(input);
        assertTrue(json.startsWith("["));
        assertTrue(json.contains("\"type\":\"text\""));

        InteractionInput deserialized = mapper.readValue(json, InteractionInput.class);
        assertTrue(deserialized.isContents());
        assertEquals(1, deserialized.contents().size());
        assertEquals("Hello as content", ((Content.TextContent) deserialized.contents().get(0)).text());
    }

    @Test
    void testInteractionInputTurns() throws Exception {
        Turn turn = Turn.user("User message");
        InteractionInput input = InteractionInput.ofTurns(List.of(turn));
        assertTrue(input.isTurns());
        assertFalse(input.isText());
        assertEquals(1, input.turns().size());

        String json = mapper.writeValueAsString(input);
        assertTrue(json.startsWith("["));
        assertTrue(json.contains("\"role\":\"user\""));

        InteractionInput deserialized = mapper.readValue(json, InteractionInput.class);
        assertTrue(deserialized.isTurns());
        assertEquals(1, deserialized.turns().size());
        assertEquals("User message", deserialized.turns().get(0).text());
    }

    @Test
    void testInteractionInputSteps() throws Exception {
        Step step = new Step.UserInputStep("user_input", List.of(new Content.TextContent("User step input")));
        InteractionInput input = InteractionInput.ofSteps(List.of(step));
        assertTrue(input.isSteps());
        assertFalse(input.isText());
        assertEquals(1, input.steps().size());

        String json = mapper.writeValueAsString(input);
        assertTrue(json.startsWith("["));
        assertTrue(json.contains("\"type\":\"user_input\""));

        InteractionInput deserialized = mapper.readValue(json, InteractionInput.class);
        assertTrue(deserialized.isSteps());
        assertEquals(1, deserialized.steps().size());
    }

    @Test
    void testAgentInteractionParamsSerializationAndDeserialization() throws Exception {
        AgentInteractionParams params = AgentInteractionParams.builder()
                .agent("research-agent")
                .input("Research topics")
                .environment("remote")
                .build();

        assertNotNull(params.input());
        assertTrue(params.input().isText());
        assertEquals("Research topics", params.input().text());
        assertNotNull(params.environment());
        assertEquals("remote", params.environment().preset());

        String json = mapper.writeValueAsString(params);
        assertTrue(json.contains("\"agent\":\"research-agent\""));
        assertTrue(json.contains("\"input\":\"Research topics\""));
        assertTrue(json.contains("\"environment\":\"remote\""));

        AgentInteractionParams deserialized = mapper.readValue(json, AgentInteractionParams.class);
        assertEquals("research-agent", deserialized.agent());
        assertTrue(deserialized.input().isText());
        assertEquals("Research topics", deserialized.input().text());
        assertEquals("remote", deserialized.environment().preset());
    }

    @Test
    void testModelInteractionParamsSerializationAndDeserialization() throws Exception {
        ModelInteractionParams params = ModelInteractionParams.builder()
                .model("gemini-2.5-flash")
                .input("Model prompt")
                .build();

        assertTrue(params.input().isText());
        assertEquals("Model prompt", params.input().text());

        String json = mapper.writeValueAsString(params);
        assertTrue(json.contains("\"model\":\"gemini-2.5-flash\""));
        assertTrue(json.contains("\"input\":\"Model prompt\""));

        ModelInteractionParams deserialized = mapper.readValue(json, ModelInteractionParams.class);
        assertEquals("gemini-2.5-flash", deserialized.model());
        assertTrue(deserialized.input().isText());
        assertEquals("Model prompt", deserialized.input().text());
    }

    @Test
    void testBaseEnvironmentOf() {
        BaseEnvironment preset = BaseEnvironment.of("cloud");
        assertEquals("cloud", preset.preset());
        assertNull(preset.config());
        assertEquals("cloud", preset.value());

        EnvironmentConfig config = EnvironmentConfig.forExisting("env_456", "enabled");
        BaseEnvironment configEnv = BaseEnvironment.of(config);
        assertNull(configEnv.preset());
        assertEquals(config, configEnv.config());
        assertEquals(config, configEnv.value());

        BaseEnvironment identity = BaseEnvironment.of(configEnv);
        assertEquals(configEnv, identity);
    }
}
