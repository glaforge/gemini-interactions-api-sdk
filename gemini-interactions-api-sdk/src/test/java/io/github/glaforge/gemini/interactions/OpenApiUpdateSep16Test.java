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

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import io.github.glaforge.gemini.interactions.model.CreateEnvironmentRequest;
import io.github.glaforge.gemini.interactions.model.ModelOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for updates introduced in the September 16, 2026 OpenAPI specification sync.
 */
class OpenApiUpdateSep16Test {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder().build();
    }

    @Test
    void testGemini38FlashConstant() {
        assertEquals("gemini-3.8-flash", ModelOption.GEMINI_3_8_FLASH);
    }

    @Test
    void testCreateEnvironmentRequestFromEnvironmentSerialization() throws Exception {
        CreateEnvironmentRequest req = CreateEnvironmentRequest.builder()
            .fromEnvironment("environments/env_original_123")
            .build();

        String json = mapper.writeValueAsString(req);
        assertTrue(json.contains("\"from_environment\":\"environments/env_original_123\""));
        assertNull(req.network());
        assertNull(req.sources());

        CreateEnvironmentRequest deserialized = mapper.readValue(json, CreateEnvironmentRequest.class);
        assertEquals("environments/env_original_123", deserialized.fromEnvironment());
        assertNull(deserialized.network());
        assertNull(deserialized.sources());
    }

    @Test
    void testCreateEnvironmentRequestFromEnvironmentWithNetwork() throws Exception {
        CreateEnvironmentRequest req = CreateEnvironmentRequest.builder()
            .fromEnvironment("env_456")
            .network("disabled")
            .build();

        String json = mapper.writeValueAsString(req);
        assertTrue(json.contains("\"from_environment\":\"env_456\""));
        assertTrue(json.contains("\"network\":\"disabled\""));

        CreateEnvironmentRequest deserialized = mapper.readValue(json, CreateEnvironmentRequest.class);
        assertEquals("env_456", deserialized.fromEnvironment());
        assertEquals("disabled", deserialized.network().preset());
    }
}
