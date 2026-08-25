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

import io.github.glaforge.gemini.interactions.model.Agent;
import io.github.glaforge.gemini.interactions.model.BaseEnvironment;
import io.github.glaforge.gemini.interactions.model.Content;
import io.github.glaforge.gemini.interactions.model.Environment;
import io.github.glaforge.gemini.interactions.model.MediaProcessingConfiguration;
import io.github.glaforge.gemini.interactions.model.NetworkConfiguration;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeSafeRefactoringTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();

    @Test
    void testNetworkConfigurationPreset() throws Exception {
        Environment env = Environment.builder()
            .network("allow_all")
            .build();

        String json = mapper.writeValueAsString(env);
        assertTrue(json.contains("\"network\":\"allow_all\""));

        Environment deserialized = mapper.readValue(json, Environment.class);
        assertNotNull(deserialized.network());
        assertTrue(deserialized.network().isPreset());
        assertFalse(deserialized.network().isCustom());
        assertEquals("allow_all", deserialized.network().preset());
    }

    @Test
    void testNetworkConfigurationCustom() throws Exception {
        io.github.glaforge.gemini.interactions.model.AllowlistEntry entry = new io.github.glaforge.gemini.interactions.model.AllowlistEntry("example.com", null);
        io.github.glaforge.gemini.interactions.model.EnvironmentNetworkEgressAllowlist customConfig = new io.github.glaforge.gemini.interactions.model.EnvironmentNetworkEgressAllowlist(List.of(entry));
        Environment env = Environment.builder()
            .network(customConfig)
            .build();

        String json = mapper.writeValueAsString(env);
        assertTrue(json.contains("\"network\":"));
        assertTrue(json.contains("\"allowlist\""));

        Environment deserialized = mapper.readValue(json, Environment.class);
        assertNotNull(deserialized.network());
        assertTrue(deserialized.network().isCustom());
        assertFalse(deserialized.network().isPreset());
        assertEquals(1, deserialized.network().config().allowlist().size());
        assertEquals("example.com", deserialized.network().config().allowlist().get(0).domain());
    }

    @Test
    void testBaseEnvironmentPreset() throws Exception {
        Agent agent = Agent.builder()
            .baseEnvironment("default")
            .build();

        String json = mapper.writeValueAsString(agent);
        assertTrue(json.contains("\"base_environment\":\"default\""));

        Agent deserialized = mapper.readValue(json, Agent.class);
        assertNotNull(deserialized.baseEnvironment());
        assertTrue(deserialized.baseEnvironment().isPreset());
        assertEquals("default", deserialized.baseEnvironment().preset());
    }

    @Test
    void testMediaProcessingConfigurationPreset() throws Exception {
        Content.VideoContent video = new Content.VideoContent("video", null, "https://example.com/v.mp4", "video/mp4", null, "agentic");

        String json = mapper.writeValueAsString(video);
        assertTrue(json.contains("\"processing\":\"agentic\""));

        Content.VideoContent deserialized = mapper.readValue(json, Content.VideoContent.class);
        assertNotNull(deserialized.processing());
        assertTrue(deserialized.processing().isPreset());
        assertEquals("agentic", deserialized.processing().preset());
    }
}
