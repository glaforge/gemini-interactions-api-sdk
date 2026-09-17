package io.github.glaforge.gemini.interactions;

import io.github.glaforge.gemini.interactions.model.Config.GenerationConfig;
import io.github.glaforge.gemini.interactions.model.Config.VideoResponseFormat;
import io.github.glaforge.gemini.interactions.model.Content;
import io.github.glaforge.gemini.interactions.model.Content.UnknownContent;
import io.github.glaforge.gemini.interactions.model.Step;
import io.github.glaforge.gemini.interactions.model.Step.UnknownStep;
import io.github.glaforge.gemini.interactions.model.Tool;
import io.github.glaforge.gemini.interactions.model.Tool.AllowedTools;
import io.github.glaforge.gemini.interactions.model.Tool.ToolChoiceConfig;
import io.github.glaforge.gemini.interactions.model.Tool.UnknownTool;
import io.github.glaforge.gemini.interactions.model.Trigger;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelsParityTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();

    @Test
    void testToolChoiceStringMode() throws Exception {
        GenerationConfig config = GenerationConfig.builder()
                .toolChoice("auto")
                .build();

        String json = mapper.writeValueAsString(config);
        assertTrue(json.contains("\"tool_choice\":\"auto\""));

        GenerationConfig deserialized = mapper.readValue(json, GenerationConfig.class);
        assertNotNull(deserialized.toolChoice());
        assertTrue(deserialized.toolChoice().isMode());
        assertFalse(deserialized.toolChoice().isConfig());
        assertEquals("auto", deserialized.toolChoice().mode());
        assertNull(deserialized.toolChoiceConfig());
    }

    @Test
    void testToolChoiceConfigObject() throws Exception {
        ToolChoiceConfig toolChoiceConfig = ToolChoiceConfig.builder()
                .allowedTools(new AllowedTools(Tool.Mode.ANY, List.of("my_tool")))
                .build();

        GenerationConfig config = GenerationConfig.builder()
                .toolChoice(toolChoiceConfig)
                .build();

        String json = mapper.writeValueAsString(config);
        assertTrue(json.contains("\"tool_choice\":{\"allowed_tools\":"));

        GenerationConfig deserialized = mapper.readValue(json, GenerationConfig.class);
        assertNotNull(deserialized.toolChoice());
        assertTrue(deserialized.toolChoice().isConfig());
        assertFalse(deserialized.toolChoice().isMode());
        assertNotNull(deserialized.toolChoice().config());
        assertEquals(Tool.Mode.ANY, deserialized.toolChoice().config().allowedTools().mode());
        assertEquals(List.of("my_tool"), deserialized.toolChoice().config().allowedTools().tools());
    }

    @Test
    void testVideoResponseFormatEnrichment() throws Exception {
        VideoResponseFormat format = VideoResponseFormat.builder()
                .aspectRatio("16:9")
                .delivery("mp4")
                .duration("15s")
                .build();

        String json = mapper.writeValueAsString(format);
        assertTrue(json.contains("\"aspect_ratio\":\"16:9\""));
        assertTrue(json.contains("\"delivery\":\"mp4\""));
        assertTrue(json.contains("\"duration\":\"15s\""));

        VideoResponseFormat deserialized = mapper.readValue(json, VideoResponseFormat.class);
        assertEquals("16:9", deserialized.aspectRatio());
        assertEquals("mp4", deserialized.delivery());
        assertEquals("15s", deserialized.duration());
    }

    @Test
    void testUnknownStepFallback() throws Exception {
        String json = """
                {
                  "type": "quantum_thought",
                  "state": "superposition",
                  "qubits": 8
                }
                """;

        Step step = mapper.readValue(json, Step.class);
        assertInstanceOf(UnknownStep.class, step);
        UnknownStep unknownStep = (UnknownStep) step;
        assertEquals("quantum_thought", unknownStep.type());
        assertEquals("superposition", unknownStep.raw().get("state"));
        assertEquals(8, ((Number) unknownStep.raw().get("qubits")).intValue());
    }

    @Test
    void testUnknownToolFallback() throws Exception {
        String json = """
                {
                  "type": "quantum_computer",
                  "backend": "simulator"
                }
                """;

        Tool tool = mapper.readValue(json, Tool.class);
        assertInstanceOf(UnknownTool.class, tool);
        UnknownTool unknownTool = (UnknownTool) tool;
        assertEquals("quantum_computer", unknownTool.type());
        assertEquals("simulator", unknownTool.raw().get("backend"));
    }

    @Test
    void testUnknownContentFallback() throws Exception {
        String json = """
                {
                  "type": "hologram",
                  "resolution": "4k"
                }
                """;

        Content content = mapper.readValue(json, Content.class);
        assertInstanceOf(UnknownContent.class, content);
        UnknownContent unknownContent = (UnknownContent) content;
        assertEquals("hologram", unknownContent.type());
        assertEquals("4k", unknownContent.raw().get("resolution"));
    }

    @Test
    void testTriggerInteractionAsInteractionOrMap() throws Exception {
        String jsonWithInteractionObject = """
                {
                  "id": "trig-999",
                  "interaction": {
                    "id": "inter-111",
                    "status": "completed"
                  }
                }
                """;

        Trigger trigger = mapper.readValue(jsonWithInteractionObject, Trigger.class);
        assertEquals("trig-999", trigger.id());
        assertNotNull(trigger.interaction());
        assertNotNull(trigger.interactionAsInteraction());
        assertEquals("inter-111", trigger.interactionAsInteraction().id());
    }
}
