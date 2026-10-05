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

import io.github.glaforge.gemini.interactions.model.Config;
import io.github.glaforge.gemini.interactions.model.InteractionParams;
import io.github.glaforge.gemini.interactions.model.Trigger;
import io.github.glaforge.gemini.interactions.model.TriggerCreateParams;
import io.github.glaforge.gemini.interactions.model.TriggerInteraction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for updates introduced in the October 5, 2026 OpenAPI specification sync.
 */
class OpenApiUpdateOct05Test {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder().build();
    }

    @Test
    void testTriggerWithAgentInteractionTemplate() throws Exception {
        String triggerJson = """
            {
              "id": "trigger-oct-01",
              "display_name": "Scheduled News Digest",
              "schedule": "0 9 * * *",
              "time_zone": "America/New_York",
              "status": "active",
              "interaction": {
                "agent": "antigravity-preview-05-2026",
                "input": "Summarize top news stories.",
                "environment": "remote"
              }
            }
            """;

        Trigger trigger = mapper.readValue(triggerJson, Trigger.class);
        assertNotNull(trigger);
        assertEquals("trigger-oct-01", trigger.id());
        assertEquals("Scheduled News Digest", trigger.displayName());
        assertEquals("0 9 * * *", trigger.schedule());
        assertEquals("America/New_York", trigger.timeZone());
        assertEquals(Trigger.Status.ACTIVE, trigger.status());

        assertNotNull(trigger.interaction());
        assertTrue(trigger.interaction().isRequest());
        assertFalse(trigger.interaction().isResource());

        InteractionParams.AgentInteractionParams agentParams = trigger.interaction().agentInteraction();
        assertNotNull(agentParams);
        assertEquals("antigravity-preview-05-2026", agentParams.agent());
        assertEquals("Summarize top news stories.", agentParams.input().text());
        assertEquals("remote", agentParams.environment().preset());
    }

    @Test
    void testTriggerCreateParamsWithAgentInteractionOverload() throws Exception {
        InteractionParams.AgentInteractionParams agentParams = InteractionParams.AgentInteractionParams.builder()
            .agent("antigravity-preview-05-2026")
            .input("Run daily security scan.")
            .environment("remote")
            .build();

        TriggerCreateParams params = TriggerCreateParams.builder()
            .displayName("Daily Scan")
            .schedule("0 0 * * *")
            .timeZone("UTC")
            .interaction(agentParams)
            .build();

        String json = mapper.writeValueAsString(params);
        assertTrue(json.contains("\"display_name\":\"Daily Scan\""));
        assertTrue(json.contains("\"schedule\":\"0 0 * * *\""));
        assertTrue(json.contains("\"agent\":\"antigravity-preview-05-2026\""));

        TriggerCreateParams deserialized = mapper.readValue(json, TriggerCreateParams.class);
        assertEquals("Daily Scan", deserialized.displayName());
        assertEquals("0 0 * * *", deserialized.schedule());
        assertTrue(deserialized.interaction() instanceof InteractionParams.AgentInteractionParams);
        InteractionParams.AgentInteractionParams deserializedAgent =
            (InteractionParams.AgentInteractionParams) deserialized.interaction();
        assertEquals("antigravity-preview-05-2026", deserializedAgent.agent());
    }

    @Test
    void testTriggerBuilderWithAgentInteractionOverload() {
        InteractionParams.AgentInteractionParams agentParams = InteractionParams.AgentInteractionParams.builder()
            .agent("antigravity-preview-05-2026")
            .input("Periodic task")
            .build();

        Trigger trigger = Trigger.builder()
            .id("trigger-builder-1")
            .displayName("Builder Trigger")
            .interaction(agentParams)
            .build();

        assertNotNull(trigger.interaction());
        assertTrue(trigger.interaction().isRequest());
        assertNotNull(trigger.interaction().agentInteraction());
        assertEquals("antigravity-preview-05-2026", trigger.interaction().agentInteraction().agent());
    }

    @Test
    void testTriggerInteractionFactoryAndAccessors() {
        InteractionParams.AgentInteractionParams agentParams = InteractionParams.AgentInteractionParams.builder()
            .agent("agent-test")
            .input("hello")
            .build();

        TriggerInteraction ti = TriggerInteraction.of(agentParams);
        assertTrue(ti.isRequest());
        assertFalse(ti.isResource());
        assertEquals(agentParams, ti.agentInteraction());
        assertEquals(agentParams, ti.request());
        assertNull(ti.resource());
    }

    @Test
    void testGenerationConfigDeprecatedFieldsSerialization() throws Exception {
        Config.GenerationConfig config = Config.GenerationConfig.builder()
            .temperature(0.7)
            .topP(0.95)
            .seed(42)
            .build();

        String json = mapper.writeValueAsString(config);
        assertTrue(json.contains("\"temperature\":0.7"));
        assertTrue(json.contains("\"top_p\":0.95"));
        assertTrue(json.contains("\"seed\":42"));

        Config.GenerationConfig deserialized = mapper.readValue(json, Config.GenerationConfig.class);
        assertEquals(0.7, deserialized.temperature());
        assertEquals(0.95, deserialized.topP());
        assertEquals(42, deserialized.seed());
    }
}
