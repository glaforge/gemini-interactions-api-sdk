/*
 * Copyright 2025 Google LLC
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

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AgentTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Test
    void deserializePredefinedBaseEnvironment() throws Exception {
        String json = """
            {
              "id": "agent-123",
              "description": "My custom helper agent",
              "base_agent": "deep-research-preview-04-2026",
              "base_environment": "default",
              "system_instruction": "Be helpful and concise.",
              "tools": [
                {
                  "type": "google_search",
                  "search_types": ["enterprise_web_search"]
                },
                {
                  "type": "code_execution"
                }
              ]
            }
            """;

        Agent agent = objectMapper.readValue(json, Agent.class);

        assertEquals("agent-123", agent.id());
        assertEquals("My custom helper agent", agent.description());
        assertEquals("deep-research-preview-04-2026", agent.baseAgent());
        assertEquals("default", agent.baseEnvironment().preset());
        assertEquals("Be helpful and concise.", agent.systemInstruction());
        assertEquals(2, agent.tools().size());

        assertTrue(agent.tools().get(0) instanceof AgentTool.GoogleSearch);
        AgentTool.GoogleSearch search = (AgentTool.GoogleSearch) agent.tools().get(0);
        assertEquals("google_search", search.type());
        assertEquals(List.of("enterprise_web_search"), search.searchTypes());

        assertTrue(agent.tools().get(1) instanceof AgentTool.CodeExecution);
        AgentTool.CodeExecution code = (AgentTool.CodeExecution) agent.tools().get(1);
        assertEquals("code_execution", code.type());
    }

    @Test
    void deserializeCustomEnvironmentWithDisabledNetwork() throws Exception {
        String json = """
            {
              "id": "agent-remote-env",
              "base_environment": {
                "type": "remote",
                "network": "disabled",
                "sources": [
                  {
                    "type": "inline",
                    "target": "src/utils.py",
                    "content": "def add(a, b): return a + b",
                    "encoding": "utf-8"
                  }
                ]
              }
            }
            """;

        Agent agent = objectMapper.readValue(json, Agent.class);

        assertEquals("agent-remote-env", agent.id());
        assertTrue(agent.baseEnvironment().isCustom());

        EnvironmentConfig env = agent.baseEnvironment().config();
        assertEquals("remote", env.type());
        assertEquals("disabled", env.network().preset());
        assertEquals(1, env.sources().size());

        Source source = env.sources().get(0);
        assertEquals(Source.Type.INLINE, source.type());
        assertEquals("src/utils.py", source.target());
        assertEquals("def add(a, b): return a + b", source.content());
        assertEquals("utf-8", source.encoding());
    }

    @Test
    void deserializeCustomEnvironmentWithEgressAllowlist() throws Exception {
        String json = """
            {
              "id": "agent-egress",
              "base_environment": {
                "type": "remote",
                "network": {
                  "allowlist": [
                    {
                      "domain": "*.googleapis.com",
                      "transform": [
                        {
                          "header": "Authorization",
                          "value": "Bearer 123"
                        }
                      ]
                    }
                  ]
                },
                "sources": []
              }
            }
            """;

        Agent agent = objectMapper.readValue(json, Agent.class);

        assertTrue(agent.baseEnvironment().isCustom());
        EnvironmentConfig env = agent.baseEnvironment().config();
        assertTrue(env.network().isCustom());

        EnvironmentNetworkEgressAllowlist network = env.network().config();
        assertEquals(1, network.allowlist().size());

        AllowlistEntry entry = network.allowlist().get(0);
        assertEquals("*.googleapis.com", entry.domain());
        assertEquals(1, entry.transform().size());
        assertEquals(Map.of("header", "Authorization", "value", "Bearer 123"), entry.transform().get(0));
    }

    @Test
    void serializeAgentBuilder() throws Exception {
        AgentTool searchTool = new AgentTool.GoogleSearch("google_search", List.of("web"));
        AgentTool mcpTool = new AgentTool.McpServer("mcp_server", "myserver", "https://mcp.example.com", null, null);

        Agent agent = Agent.builder()
            .id("agent-builder-test")
            .description("Fluently built agent")
            .baseAgent("gemini-3.5-flash")
            .baseEnvironment("default")
            .tools(searchTool, mcpTool)
            .build();

        String serializedJson = objectMapper.writeValueAsString(agent);

        Agent deserialized = objectMapper.readValue(serializedJson, Agent.class);
        assertEquals("agent-builder-test", deserialized.id());
        assertEquals("Fluently built agent", deserialized.description());
        assertEquals("default", deserialized.baseEnvironment().preset());
        assertEquals(2, deserialized.tools().size());

        assertTrue(deserialized.tools().get(0) instanceof AgentTool.GoogleSearch);
        assertTrue(deserialized.tools().get(1) instanceof AgentTool.McpServer);
        AgentTool.McpServer mcp = (AgentTool.McpServer) deserialized.tools().get(1);
        assertEquals("myserver", mcp.name());
        assertEquals("https://mcp.example.com", mcp.url());
    }

    @Test
    void deserializeListAgentsResponse() throws Exception {
        String json = """
            {
              "agents": [
                {
                  "id": "agent-1",
                  "base_agent": "gemini-3.5-flash",
                  "base_environment": "default"
                },
                {
                  "id": "agent-2",
                  "base_agent": "gemini-3.5-flash",
                  "base_environment": "default"
                }
              ],
              "next_page_token": "token-xyz"
            }
            """;

        ListAgentsResponse response = objectMapper.readValue(json, ListAgentsResponse.class);
        assertEquals(2, response.agents().size());
        assertEquals("token-xyz", response.nextPageToken());
        assertEquals("agent-1", response.agents().get(0).id());
        assertEquals("agent-2", response.agents().get(1).id());
    }

    @Test
    void sourceBuilder() {
        Source source = Source.builder()
            .type(Source.Type.INLINE)
            .target("src/utils.py")
            .content("def add(a, b): return a + b")
            .encoding("utf-8")
            .build();

        assertEquals(Source.Type.INLINE, source.type());
        assertEquals("src/utils.py", source.target());
        assertEquals("def add(a, b): return a + b", source.content());
        assertEquals("utf-8", source.encoding());
        assertNull(source.source());
    }

    @Test
    void sourceBuilderWithLocation() {
        Source source = Source.builder()
            .type(Source.Type.GCS)
            .target("data/")
            .source("gs://my-bucket/data/")
            .build();

        assertEquals(Source.Type.GCS, source.type());
        assertEquals("data/", source.target());
        assertEquals("gs://my-bucket/data/", source.source());
        assertNull(source.content());
        assertNull(source.encoding());
    }
}
