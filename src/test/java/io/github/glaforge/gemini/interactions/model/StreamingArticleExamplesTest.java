package io.github.glaforge.gemini.interactions.model;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StreamingArticleExamplesTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();

    @Test
    void testBasicStreamingInteraction() throws JacksonException {
        String json1 = """
            {"interaction":{"id":"v1_...","status":"in_progress","object":"interaction","model":"gemini-3-flash-preview"},"event_type":"interaction.created"}
            """;
        Events event1 = mapper.readValue(json1, Events.class);
        assertTrue(event1 instanceof Events.InteractionCreated);

        String json2 = """
            {"index":0,"step":{"type":"thought"},"event_type":"step.start"}
            """;
        Events event2 = mapper.readValue(json2, Events.class);
        assertTrue(event2 instanceof Events.StepStart);

        String json3 = """
            {"index":1,"delta":{"text":"1, 2, 3, 4, 5, 6, ","type":"text"},"event_type":"step.delta"}
            """;
        Events event3 = mapper.readValue(json3, Events.class);
        assertTrue(event3 instanceof Events.StepDelta);
        Events.StepDelta deltaEvent = (Events.StepDelta) event3;
        assertTrue(deltaEvent.delta() instanceof Events.TextDelta);
        assertEquals("1, 2, 3, 4, 5, 6, ", ((Events.TextDelta) deltaEvent.delta()).text());

        String json4 = """
            {"interaction":{"id":"v1_...","status":"completed","usage":{"total_tokens":346,"total_input_tokens":11,"input_tokens_by_modality":[{"modality":"text","tokens":11}],"total_cached_tokens":0,"total_output_tokens":90,"total_tool_use_tokens":0,"total_thought_tokens":245},"created":"2026-05-12T18:44:51Z","updated":"2026-05-12T18:44:51Z","service_tier":"standard","object":"interaction","model":"gemini-3-flash-preview"},"event_type":"interaction.completed"}
            """;
        Events event4 = mapper.readValue(json4, Events.class);
        assertTrue(event4 instanceof Events.InteractionCompleted);
    }

    @Test
    void testFunctionCallingStreaming() throws JacksonException {
        String json1 = """
            {"index":0,"step":{"type":"function_call", "id":"un6k8t18", "name": "get_weather", "arguments":{}}, "event_type": "step.start"}
            """;
        Events event1 = mapper.readValue(json1, Events.class);
        assertTrue(event1 instanceof Events.StepStart);
        Events.StepStart startEvent = (Events.StepStart) event1;
        assertTrue(startEvent.step() instanceof Step.FunctionCallStep);

        String json2 = """
            {"index":3,"delta":{"arguments":"{\\"location\\":\\"Mount Elbrus, Russia\\"}","type":"arguments_delta"},"event_type":"step.delta"}
            """;
        Events event2 = mapper.readValue(json2, Events.class);
        assertTrue(event2 instanceof Events.StepDelta);
        Events.StepDelta deltaEvent = (Events.StepDelta) event2;
        assertTrue(deltaEvent.delta() instanceof Events.ArgumentsDelta);
        assertEquals("{\"location\":\"Mount Elbrus, Russia\"}", ((Events.ArgumentsDelta) deltaEvent.delta()).arguments());
    }

    @Test
    void testMultipleToolsStreaming() throws JacksonException {
        String json1 = """
            {"index":0,"step":{"id":"mkutnkgn","signature":"","type":"google_search_call"},"event_type":"step.start"}
            """;
        Events event1 = mapper.readValue(json1, Events.class);
        assertTrue(event1 instanceof Events.StepStart);
        assertTrue(((Events.StepStart) event1).step() instanceof Step.GoogleSearchCallStep);

        String json2 = """
            {"index":0,"delta":{"signature":"...","type":"google_search_call","arguments":{"queries":["largest mountain in Europe"]}},"event_type":"step.delta"}
            """;
        Events event2 = mapper.readValue(json2, Events.class);
        assertTrue(event2 instanceof Events.StepDelta);
        assertTrue(((Events.StepDelta) event2).delta() instanceof Events.GoogleSearchCallDelta);

        String json3 = """
            {"index":1,"step":{"call_id":"mkutnkgn","signature":"","type":"google_search_result"},"event_type":"step.start"}
            """;
        Events event3 = mapper.readValue(json3, Events.class);
        assertTrue(event3 instanceof Events.StepStart);
        assertTrue(((Events.StepStart) event3).step() instanceof Step.GoogleSearchResultStep);

        String json4 = """
            {"index":1,"delta":{"signature":"...","type":"google_search_result","is_error":false},"event_type":"step.delta"}
            """;
        Events event4 = mapper.readValue(json4, Events.class);
        assertTrue(event4 instanceof Events.StepDelta);
        assertTrue(((Events.StepDelta) event4).delta() instanceof Events.GoogleSearchResultDelta);
        assertFalse(((Events.GoogleSearchResultDelta) ((Events.StepDelta) event4).delta()).isError());
    }

    @Test
    void testDeepResearchAgentStreaming() throws JacksonException {
        String json1 = """
            {"index":0,"delta":{"content":{"text":"***Generating research plan***\\n\\nTo best answer your request, I'm starting by constructing a comprehensive research plan. This will outline the key areas I need to investigate and the strategy I'll use to connect them.","type":"text"},"type":"thought_summary"},"event_type":"step.delta"}
            """;
        Events event1 = mapper.readValue(json1, Events.class);
        assertTrue(event1 instanceof Events.StepDelta);
        assertTrue(((Events.StepDelta) event1).delta() instanceof Events.ThoughtSummaryDelta);
        
        Events.ThoughtSummaryDelta thoughtDelta = (Events.ThoughtSummaryDelta) ((Events.StepDelta) event1).delta();
        assertNotNull(thoughtDelta.content());
    }

    @Test
    void testMultiModalStreaming() throws JacksonException {
        String json1 = """
            {"index":2,"delta":{"mime_type":"image/jpeg","data":"/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAoHBwgHBgoICAgLCg...","type":"image"},"event_type":"step.delta"}
            """;
        Events event1 = mapper.readValue(json1, Events.class);
        assertTrue(event1 instanceof Events.StepDelta);
        assertTrue(((Events.StepDelta) event1).delta() instanceof Events.ImageDelta);
        
        Events.ImageDelta imageDelta = (Events.ImageDelta) ((Events.StepDelta) event1).delta();
        assertEquals("image/jpeg", imageDelta.mimeType());
        assertEquals("/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAoHBwgHBgoICAgLCg...", imageDelta.data());
    }
}
