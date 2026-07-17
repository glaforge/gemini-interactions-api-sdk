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

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventsTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();

    @Test
    void testInteractionCreatedDeserialization() throws JacksonException {
        String json = """
            {
              "event_type": "interaction.created",
              "event_id": "evt-123",
              "interaction": {
                "id": "interaction-123",
                "model": "gemini-pro",
                "status": "in_progress"
              }
            }
            """;

        Events event = mapper.readValue(json, Events.class);

        assertTrue(event instanceof Events.InteractionCreated);
        Events.InteractionCreated interactionCreated = (Events.InteractionCreated) event;
        assertEquals(Events.EventType.INTERACTION_CREATED, interactionCreated.eventType());
        assertEquals("evt-123", interactionCreated.eventId());
        assertEquals("interaction-123", interactionCreated.interaction().id());
    }

    @Test
    void testInteractionCompletedDeserialization() throws JacksonException {
        String json = """
            {
              "event_type": "interaction.completed",
              "event_id": "evt-456",
              "interaction": {
                "id": "interaction-123",
                "model": "gemini-pro",
                "status": "completed"
              }
            }
            """;

        Events event = mapper.readValue(json, Events.class);

        assertTrue(event instanceof Events.InteractionCompleted);
        Events.InteractionCompleted interactionCompleted = (Events.InteractionCompleted) event;
        assertEquals(Events.EventType.INTERACTION_COMPLETED, interactionCompleted.eventType());
    }

    @Test
    void testErrorEventDeserialization() throws JacksonException {
        String json = """
            {
              "event_type": "error",
              "event_id": "evt-err",
              "error": {
                "code": "500",
                "message": "Internal Server Error"
              }
            }
            """;

        Events event = mapper.readValue(json, Events.class);

        assertTrue(event instanceof Events.ErrorEvent);
        Events.ErrorEvent errorEvent = (Events.ErrorEvent) event;
        assertEquals(Events.EventType.ERROR, errorEvent.eventType());
        assertEquals("500", errorEvent.error().code());
    }

    @Test
    void testStepDeltaDeserialization() throws JacksonException {
        String json = """
            {
              "event_type": "step.delta",
              "event_id": "evt-789",
              "index": 0,
              "delta": {
                "type": "text",
                "text": "Hello"
              }
            }
            """;

        Events event = mapper.readValue(json, Events.class);

        assertTrue(event instanceof Events.StepDelta);
        Events.StepDelta stepDelta = (Events.StepDelta) event;
        assertEquals(Events.EventType.STEP_DELTA, stepDelta.eventType());
        assertEquals("evt-789", stepDelta.eventId());
        assertEquals(0, stepDelta.index());
        assertTrue(stepDelta.delta() instanceof Events.TextDelta);
        assertEquals("Hello", ((Events.TextDelta) stepDelta.delta()).text());
    }

    @Test
    void testStepStartDeserialization() throws JacksonException {
        String json = """
            {
              "event_type": "step.start",
              "event_id": "evt-abc",
              "index": 1,
              "step": {
                "type": "model_output",
                "content": []
              }
            }
            """;

        Events event = mapper.readValue(json, Events.class);

        assertTrue(event instanceof Events.StepStart);
        Events.StepStart stepStart = (Events.StepStart) event;
        assertEquals(Events.EventType.STEP_START, stepStart.eventType());
        assertEquals("evt-abc", stepStart.eventId());
        assertEquals(1, stepStart.index());
        assertTrue(stepStart.step() instanceof Step.ModelOutputStep);
    }

    @Test
    void testEventTypeEnumValues() {
        assertEquals("interaction.created", Events.EventType.INTERACTION_CREATED.getJsonValue());
        assertEquals("interaction.completed", Events.EventType.INTERACTION_COMPLETED.getJsonValue());
        assertEquals("interaction.status_update", Events.EventType.INTERACTION_STATUS_UPDATE.getJsonValue());
        assertEquals("step.start", Events.EventType.STEP_START.getJsonValue());
        assertEquals("step.delta", Events.EventType.STEP_DELTA.getJsonValue());
        assertEquals("step.stop", Events.EventType.STEP_STOP.getJsonValue());
        assertEquals("error", Events.EventType.ERROR.getJsonValue());
    }
}
