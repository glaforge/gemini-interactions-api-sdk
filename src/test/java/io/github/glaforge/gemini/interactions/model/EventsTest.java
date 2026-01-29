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
    void testInteractionStartDeserialization() throws JacksonException {
        String json = """
            {
              "event_type": "interaction.start",
              "event_id": "evt-123",
              "interaction": {
                "id": "interaction-123",
                "model": "gemini-pro",
                "status": "in_progress"
              }
            }
            """;

        Events event = mapper.readValue(json, Events.class);

        assertTrue(event instanceof Events.InteractionEvent);
        Events.InteractionEvent interactionEvent = (Events.InteractionEvent) event;
        assertEquals(Events.EventType.INTERACTION_START, interactionEvent.eventType());
        assertEquals("evt-123", interactionEvent.eventId());
        assertEquals("interaction-123", interactionEvent.interaction().id());
    }

    @Test
    void testInteractionCompleteDeserialization() throws JacksonException {
        String json = """
            {
              "event_type": "interaction.complete",
              "event_id": "evt-456",
              "interaction": {
                "id": "interaction-123",
                "model": "gemini-pro",
                "status": "completed"
              }
            }
            """;

        Events event = mapper.readValue(json, Events.class);

        assertTrue(event instanceof Events.InteractionEvent);
        Events.InteractionEvent interactionEvent = (Events.InteractionEvent) event;
        assertEquals(Events.EventType.INTERACTION_COMPLETE, interactionEvent.eventType());
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
    void testEventTypeEnumValues() {
        assertEquals("interaction.start", Events.EventType.INTERACTION_START.getJsonValue());
        assertEquals("interaction.complete", Events.EventType.INTERACTION_COMPLETE.getJsonValue());
        assertEquals("interaction.status_update", Events.EventType.INTERACTION_STATUS_UPDATE.getJsonValue());
        assertEquals("content.start", Events.EventType.CONTENT_START.getJsonValue());
        assertEquals("content.delta", Events.EventType.CONTENT_DELTA.getJsonValue());
        assertEquals("content.stop", Events.EventType.CONTENT_STOP.getJsonValue());
        assertEquals("error", Events.EventType.ERROR.getJsonValue());
    }
}
