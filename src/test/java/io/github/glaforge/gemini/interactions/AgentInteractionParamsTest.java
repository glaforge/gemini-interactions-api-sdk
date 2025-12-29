package io.github.glaforge.gemini.interactions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.glaforge.gemini.interactions.model.Content;
import io.github.glaforge.gemini.interactions.model.InteractionParams.AgentInteractionParams;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.Interaction.Role;
import io.github.glaforge.gemini.interactions.model.Interaction.Turn;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentInteractionParamsTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testInputString() throws JsonProcessingException {
        AgentInteractionParams params = AgentInteractionParams.builder()
                .agent("test-agent")
                .input("Hello world")
                .build();

        assertEquals("Hello world", params.input());

        String json = mapper.writeValueAsString(params);
        assertTrue(json.contains("\"input\":\"Hello world\""));
    }

    @Test
    void testInputContentList() throws JsonProcessingException {
        Content content = new Content.TextContent("Hello");
        AgentInteractionParams params = AgentInteractionParams.builder()
                .agent("test-agent")
                .inputContents(List.of(content))
                .build();

        assertTrue(params.input() instanceof List);
        String json = mapper.writeValueAsString(params);
        assertTrue(json.contains("\"text\":\"Hello\""));
        assertTrue(json.contains("\"type\":\"text\""));
    }

    @Test
    void testInputTurnList() throws JsonProcessingException {
        Turn turn = new Turn(Role.USER, "Hello");
        AgentInteractionParams params = AgentInteractionParams.builder()
                .agent("test-agent")
                .inputTurns(List.of(turn))
                .build();

        assertTrue(params.input() instanceof List);
        String json = mapper.writeValueAsString(params);
        assertTrue(json.contains("\"role\":\"user\""));
        // Turn content is a string "Hello", so looking for property "content":"Hello"
        assertTrue(json.contains("\"content\":\"Hello\""));
    }

    @Test
    void testInputContentVarargs() throws JsonProcessingException {
        AgentInteractionParams params = AgentInteractionParams.builder()
                .agent("test-agent")
                .input(new Content.TextContent("Hi"))
                .build();

        assertTrue(params.input() instanceof List);
        assertEquals(1, ((List<?>) params.input()).size());

        String json = mapper.writeValueAsString(params);
        System.err.println("testInputContentVarargs JSON: " + json);
        assertTrue(json.contains("\"text\":\"Hi\""));
    }

    @Test
    void testInputTurnVarargs() throws JsonProcessingException {
        AgentInteractionParams params = AgentInteractionParams.builder()
                .agent("test-agent")
                .input(new Turn(Role.USER, "Hi"))
                .build();

        assertTrue(params.input() instanceof List);
        assertEquals(1, ((List<?>) params.input()).size());

        String json = mapper.writeValueAsString(params);
        assertTrue(json.contains("\"role\":\"user\""));
        assertTrue(json.contains("\"content\":\"Hi\""));
    }

    @Test
    void testModelInteractionParamsInputString() throws JsonProcessingException {
        ModelInteractionParams params = ModelInteractionParams.builder()
                .model("gemini-2.5-flash")
                .input("Hello model")
                .build();

        assertEquals("Hello model", params.input());
        String json = mapper.writeValueAsString(params);
        assertTrue(json.contains("\"input\":\"Hello model\""));
    }
}
