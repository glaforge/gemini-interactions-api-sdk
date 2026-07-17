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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;
import java.util.Map;

/**
 * Represents a tool definition that a custom Agent can use.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = AgentTool.GoogleSearch.class, name = "google_search"),
    @JsonSubTypes.Type(value = AgentTool.CodeExecution.class, name = "code_execution"),
    @JsonSubTypes.Type(value = AgentTool.UrlContext.class, name = "url_context"),
    @JsonSubTypes.Type(value = AgentTool.McpServer.class, name = "mcp_server")
})
public sealed interface AgentTool permits
    AgentTool.GoogleSearch,
    AgentTool.CodeExecution,
    AgentTool.UrlContext,
    AgentTool.McpServer {

    /**
     * Returns the type of the tool.
     *
     * @return The tool type.
     */
    String type();

    /**
     * Agent tool definition for Google Search.
     *
     * @param type        The type of tool (must be "google_search").
     * @param searchTypes The types of search grounding to enable (e.g. "enterprise_web_search").
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleSearch(
        String type,
        @JsonProperty("search_types") List<String> searchTypes
    ) implements AgentTool {
        /**
         * Creates a new GoogleSearch tool.
         */
        public GoogleSearch() {
            this("google_search", null);
        }
    }

    /**
     * Agent tool definition for Code Execution.
     *
     * @param type The type of tool (must be "code_execution").
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record CodeExecution(
        String type
    ) implements AgentTool {
        /**
         * Creates a new CodeExecution tool.
         */
        public CodeExecution() {
            this("code_execution");
        }
    }

    /**
     * Agent tool definition for URL Context.
     *
     * @param type The type of tool (must be "url_context").
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record UrlContext(
        String type
    ) implements AgentTool {
        /**
         * Creates a new UrlContext tool.
         */
        public UrlContext() {
            this("url_context");
        }
    }

    /**
     * Agent tool definition for an MCP Server.
     *
     * @param type         The type of tool (must be "mcp_server").
     * @param name         The name of the MCP server.
     * @param url          The URL of the MCP server.
     * @param headers      Headers for the MCP server connection.
     * @param allowedTools List of allowed tools on the server.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record McpServer(
        String type,
        String name,
        String url,
        Map<String, String> headers,
        @JsonProperty("allowed_tools") List<Tool.AllowedTools> allowedTools
    ) implements AgentTool {
        /**
         * Creates a new McpServer tool.
         *
         * @param name The name of the MCP server.
         * @param url  The URL of the MCP server.
         */
        public McpServer(String name, String url) {
            this("mcp_server", name, url, null, null);
        }
    }
}
