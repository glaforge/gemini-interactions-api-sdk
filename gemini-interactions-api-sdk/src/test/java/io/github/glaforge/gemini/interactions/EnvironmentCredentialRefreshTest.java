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

import io.github.glaforge.gemini.interactions.model.AllowlistEntry;
import io.github.glaforge.gemini.interactions.model.BaseEnvironment;
import io.github.glaforge.gemini.interactions.model.EnvironmentConfig;
import io.github.glaforge.gemini.interactions.model.EnvironmentNetworkEgressAllowlist;
import io.github.glaforge.gemini.interactions.model.InteractionParams;
import io.github.glaforge.gemini.interactions.model.NetworkConfiguration;
import io.github.glaforge.gemini.interactions.model.Source;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests verifying credential refresh on existing environments, single-map header transforms,
 * and environment configuration overloads.
 */
class EnvironmentCredentialRefreshTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder().build();
    }

    @Test
    void testEnvironmentConfigForExistingSerialization() throws Exception {
        AllowlistEntry entry = AllowlistEntry.of("storage.googleapis.com", "Authorization", "Bearer REFRESHED_TOKEN");
        EnvironmentNetworkEgressAllowlist allowlist = new EnvironmentNetworkEgressAllowlist(List.of(entry));

        EnvironmentConfig config = EnvironmentConfig.forExisting("env_abc123", allowlist);

        String json = mapper.writeValueAsString(config);
        assertTrue(json.contains("\"type\":\"remote\""));
        assertTrue(json.contains("\"environment_id\":\"env_abc123\""));
        assertTrue(json.contains("\"storage.googleapis.com\""));
        assertTrue(json.contains("\"Authorization\":\"Bearer REFRESHED_TOKEN\""));

        EnvironmentConfig deserialized = mapper.readValue(json, EnvironmentConfig.class);
        assertEquals("remote", deserialized.type());
        assertEquals("env_abc123", deserialized.environmentId());
        assertNotNull(deserialized.network());
        assertEquals(1, deserialized.network().config().allowlist().size());
        assertEquals("storage.googleapis.com", deserialized.network().config().allowlist().get(0).domain());
        assertEquals("Bearer REFRESHED_TOKEN", deserialized.network().config().allowlist().get(0).transform().get(0).get("Authorization"));
    }

    @Test
    void testEnvironmentConfigBuilder() throws Exception {
        EnvironmentConfig config = EnvironmentConfig.builder()
            .environmentId("env_xyz789")
            .network("disabled")
            .sources(Source.builder().type(Source.Type.INLINE).target(".agents/AGENTS.md").content("Instructions").build())
            .build();

        assertEquals("remote", config.type());
        assertEquals("env_xyz789", config.environmentId());
        assertEquals("disabled", config.network().preset());
        assertEquals(1, config.sources().size());

        String json = mapper.writeValueAsString(config);
        assertTrue(json.contains("\"environment_id\":\"env_xyz789\""));
        assertTrue(json.contains("\"network\":\"disabled\""));

        EnvironmentConfig deserialized = mapper.readValue(json, EnvironmentConfig.class);
        assertEquals("env_xyz789", deserialized.environmentId());
        assertEquals("disabled", deserialized.network().preset());
    }

    @Test
    void testAllowlistEntrySingleObjectDeserialization() throws Exception {
        String json = "{\"domain\":\"api.github.com\",\"transform\":{\"Authorization\":\"Bearer ghp_token\"}}";

        AllowlistEntry entry = mapper.readValue(json, AllowlistEntry.class);
        assertEquals("api.github.com", entry.domain());
        assertNotNull(entry.transform());
        assertEquals(1, entry.transform().size());
        assertEquals("Bearer ghp_token", entry.transform().get(0).get("Authorization"));
    }

    @Test
    void testAllowlistEntryArrayDeserialization() throws Exception {
        String json = "{\"domain\":\"api.github.com\",\"transform\":[{\"Authorization\":\"Bearer ghp_token\"},{\"X-Custom\":\"Value\"}]}";

        AllowlistEntry entry = mapper.readValue(json, AllowlistEntry.class);
        assertEquals("api.github.com", entry.domain());
        assertNotNull(entry.transform());
        assertEquals(2, entry.transform().size());
        assertEquals("Bearer ghp_token", entry.transform().get(0).get("Authorization"));
        assertEquals("Value", entry.transform().get(1).get("X-Custom"));
    }

    @Test
    void testAllowlistEntryWithoutTransform() throws Exception {
        AllowlistEntry entry = new AllowlistEntry("*.googleapis.com");
        String json = mapper.writeValueAsString(entry);
        assertTrue(json.contains("\"domain\":\"*.googleapis.com\""));
        assertNull(entry.transform());

        AllowlistEntry deserialized = mapper.readValue(json, AllowlistEntry.class);
        assertEquals("*.googleapis.com", deserialized.domain());
        assertNull(deserialized.transform());
    }

    @Test
    void testAgentInteractionParamsEnvironmentOverloads() throws Exception {
        // String overload
        InteractionParams.AgentInteractionParams params1 = InteractionParams.AgentInteractionParams.builder()
            .agent("antigravity-preview-05-2026")
            .input("Hello")
            .environment("remote")
            .build();
        assertEquals("remote", params1.environment());

        // EnvironmentConfig overload (credential refresh)
        EnvironmentConfig config = EnvironmentConfig.forExisting("env_123", "disabled");
        InteractionParams.AgentInteractionParams params2 = InteractionParams.AgentInteractionParams.builder()
            .agent("antigravity-preview-05-2026")
            .input("Run isolated task")
            .environment(config)
            .build();
        assertEquals(config, params2.environment());

        String json2 = mapper.writeValueAsString(params2);
        assertTrue(json2.contains("\"environment_id\":\"env_123\""));
        assertTrue(json2.contains("\"network\":\"disabled\""));

        // BaseEnvironment overload
        BaseEnvironment baseEnv = BaseEnvironment.of(config);
        InteractionParams.AgentInteractionParams params3 = InteractionParams.AgentInteractionParams.builder()
            .agent("antigravity-preview-05-2026")
            .input("Run with BaseEnvironment")
            .environment(baseEnv)
            .build();
        assertEquals(baseEnv, params3.environment());
    }
}
