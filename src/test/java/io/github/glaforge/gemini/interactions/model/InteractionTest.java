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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class InteractionTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();

    @Test
    void testOutputFieldsExtraction() throws JacksonException {
        String json = """
            {
                "id": "interaction-123",
                "status": "completed",
                "steps": [
                    {
                        "type": "model_output",
                        "content": [
                            {
                                "type": "image",
                                "data": "aGVsbG8="
                            },
                            {
                                "type": "audio",
                                "data": "aGVsbG8="
                            },
                            {
                                "type": "video",
                                "data": "aGVsbG8="
                            },
                            {
                                "type": "text",
                                "text": "Hello "
                            },
                            {
                                "type": "text",
                                "text": "world"
                            }
                        ]
                    }
                ]
            }
            """;

        Interaction interaction = mapper.readValue(json, Interaction.class);
        
        assertNotNull(interaction);
        assertEquals("interaction-123", interaction.id());
        assertEquals(Interaction.Status.COMPLETED, interaction.status());
        
        // Verify the dynamically extracted output fields
        assertEquals("Hello world", interaction.outputText());
        assertNotNull(interaction.outputImage());
        assertEquals("hello", new String(interaction.outputImage().data()));
        assertNotNull(interaction.outputAudio());
        assertEquals("hello", new String(interaction.outputAudio().data()));
        assertNotNull(interaction.outputVideo());
        assertEquals("hello", new String(interaction.outputVideo().data()));
    }

    @Test
    void testMissingOutputFieldsAreNull() throws JacksonException {
        String json = """
            {
                "id": "interaction-123",
                "status": "completed"
            }
            """;

        Interaction interaction = mapper.readValue(json, Interaction.class);
        
        assertNotNull(interaction);
        assertNull(interaction.outputText());
        assertNull(interaction.outputImage());
        assertNull(interaction.outputAudio());
        assertNull(interaction.outputVideo());
    }
}
