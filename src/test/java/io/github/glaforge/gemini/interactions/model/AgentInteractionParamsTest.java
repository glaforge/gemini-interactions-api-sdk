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

import io.github.glaforge.gemini.schema.StringSchema;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class AgentInteractionParamsTest {

    @Test
    void testResponseFormatWithMap() {
        Map<String, Object> formatMap = Map.of(
            "type", "string",
            "description", "A simple string"
        );

        InteractionParams.AgentInteractionParams params = InteractionParams.AgentInteractionParams.builder()
                .responseFormat(formatMap)
                .build();

        Object responseFormat = params.responseFormat();
        assertNotNull(responseFormat);
        assertTrue(responseFormat instanceof Map);
        assertEquals(formatMap, responseFormat);
    }

    @Test
    void testResponseFormatWithSchema() {
        StringSchema schema = new StringSchema();
        schema.desc("A simple string");
        schema.enumValues("A", "B");

        InteractionParams.AgentInteractionParams params = InteractionParams.AgentInteractionParams.builder()
                .responseFormat(schema)
                .build();

        Object responseFormat = params.responseFormat();
        assertNotNull(responseFormat);
        assertTrue(responseFormat instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) responseFormat;
        assertEquals("string", map.get("type"));
        assertEquals("A simple string", map.get("description"));
    }

    @Test
    void testResponseFormatWithString() {
        String jsonSchema = """
            {
              "type": "string",
              "description": "Parsed from JSON string"
            }
            """;

        InteractionParams.AgentInteractionParams params = InteractionParams.AgentInteractionParams.builder()
                .responseFormat(jsonSchema)
                .build();

        Object responseFormat = params.responseFormat();
        assertTrue(responseFormat instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) responseFormat;
        assertEquals("string", map.get("type"));
        assertEquals("Parsed from JSON string", map.get("description"));
    }
}
