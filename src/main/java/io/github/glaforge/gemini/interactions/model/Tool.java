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
 * Represents a tool definition that the model can use.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Tool.Function.class, name = "function"),
    @JsonSubTypes.Type(value = Tool.GoogleSearch.class, name = "google_search"),
    @JsonSubTypes.Type(value = Tool.CodeExecution.class, name = "code_execution"),
    @JsonSubTypes.Type(value = Tool.UrlContext.class, name = "url_context"),
    @JsonSubTypes.Type(value = Tool.ComputerUse.class, name = "computer_use"),
    @JsonSubTypes.Type(value = Tool.McpServer.class, name = "mcp_server"),
    @JsonSubTypes.Type(value = Tool.FileSearch.class, name = "file_search")
})
public sealed interface Tool permits
    Tool.Function,
    Tool.GoogleSearch,
    Tool.CodeExecution,
    Tool.UrlContext,
    Tool.ComputerUse,
    Tool.McpServer,
    Tool.FileSearch {

    /**
     * Returns the type of the tool.
     *
     * @return The tool type.
     */
    String type();

    /**
     * Tool definition for a function.
     *
     * @param type        The type of tool (must be "function").
     * @param name        The name of the function.
     * @param description A description of the function.
     * @param parameters  The parameters of the function (JSON Schema).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Function(
        String type,
        String name,
        String description,
        // parameters is a JSON Schema object
        Map<String, Object> parameters
    ) implements Tool {
        /**
         * Creates a new Function tool.
         *
         * @param name        The name of the function.
         * @param description A description of the function.
         * @param parameters  The parameters of the function.
         */
        public Function(String name, String description, Map<String, Object> parameters) {
            this("function", name, description, parameters);
        }
    }

    /**
     * Tool definition for Google Search.
     *
     * @param type The type of tool (must be "google_search").
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleSearch(
        String type
    ) implements Tool {
        /**
         * Creates a new GoogleSearch tool.
         */
        public GoogleSearch() {
            this("google_search");
        }
    }

    /**
     * Tool definition for Code Execution.
     *
     * @param type The type of tool (must be "code_execution").
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record CodeExecution(
        String type
    ) implements Tool {
        /**
         * Creates a new CodeExecution tool.
         */
        public CodeExecution() {
            this("code_execution");
        }
    }

    /**
     * Tool definition for URL Context.
     *
     * @param type The type of tool (must be "url_context").
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record UrlContext(
        String type
    ) implements Tool {
        /**
         * Creates a new UrlContext tool.
         */
        public UrlContext() {
            this("url_context");
        }
    }

    /**
     * Tool definition for Computer Use.
     *
     * @param type                        The type of tool (must be "computer_use").
     * @param environment                 The environment (e.g., "browser").
     * @param excludedPredefinedFunctions List of excluded predefined functions.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ComputerUse(
        String type,
        String environment, // "browser"
        @JsonProperty("excludedPredefinedFunctions") List<String> excludedPredefinedFunctions
    ) implements Tool {
        /**
         * Creates a new ComputerUse tool (defaults to browser environment).
         */
        public ComputerUse() {
            this("computer_use", "browser", null);
        }
    }

    /**
     * Tool definition for an MCP Server.
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
        @JsonProperty("allowed_tools") List<AllowedTools> allowedTools
    ) implements Tool {
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

    /**
     * Tool definition for File Search.
     *
     * @param type                  The type of tool (must be "file_search").
     * @param fileSearchStoreNames List of file search store names.
     * @param topK                  Number of results to return.
     * @param metadataFilter        Filter for metadata.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record FileSearch(
        String type,
        @JsonProperty("file_search_store_names") List<String> fileSearchStoreNames,
        @JsonProperty("top_k") Integer topK,
        @JsonProperty("metadata_filter") String metadataFilter
    ) implements Tool {}

    // --- Tool Configuration ---

    // ToolChoice can be a String (enum) or a ToolChoiceConfig object.
    // We use a custom serializer/deserializer wrapper or just Object in requests.
    // For type safety, we can define a sealed interface, but Jackson serialization of "oneOf" string/object is manual without a common property.
    // The spec "oneOf" without discriminator implies we need to check types.
    // Usually handled by using Object or a wrapper. We'll define the records that CAN be used.

    /**
     * Configuration for tool choice.
     *
     * @param allowedTools Allowed tools configuration.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ToolChoiceConfig(
        @JsonProperty("allowed_tools") AllowedTools allowedTools
    ) {}

    /**
     * Allowed tools configuration.
     *
     * @param mode  The mode (AUTO, ANY, NONE).
     * @param tools List of tool names.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record AllowedTools(
        Mode mode,
        List<String> tools
    ) {}

    /**
     * Tool choice mode.
     */
    enum Mode {
        /** Auto mode. */
        @JsonProperty("auto") AUTO,
        /** Any mode. */
        @JsonProperty("any") ANY,
        /** None mode. */
        @JsonProperty("none") NONE,
        /** Validated mode. */
        @JsonProperty("validated") VALIDATED
    }
}
