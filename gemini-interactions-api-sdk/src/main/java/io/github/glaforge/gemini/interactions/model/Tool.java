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
    @JsonSubTypes.Type(value = Tool.FileSearch.class, name = "file_search"),
    @JsonSubTypes.Type(value = Tool.GoogleMaps.class, name = "google_maps"),
    @JsonSubTypes.Type(value = Tool.Retrieval.class, name = "retrieval")
})
public sealed interface Tool permits
    Tool.Function,
    Tool.GoogleSearch,
    Tool.CodeExecution,
    Tool.UrlContext,
    Tool.ComputerUse,
    Tool.McpServer,
    Tool.FileSearch,
    Tool.GoogleMaps,
    Tool.Retrieval {

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
     * @param type        The type of tool (must be "google_search").
     * @param searchTypes The types of search grounding to enable (e.g. "enterprise_web_search").
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleSearch(
        String type,
        @JsonProperty("search_types") List<String> searchTypes
    ) implements Tool {
        /**
         * Creates a new GoogleSearch tool.
         */
        public GoogleSearch() {
            this("google_search", null);
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
     * @param enablePromptInjectionDetection Whether to enable prompt injection detection.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ComputerUse(
        String type,
        String environment, // "browser"
        @JsonProperty("excludedPredefinedFunctions") List<String> excludedPredefinedFunctions,
        @JsonProperty("enable_prompt_injection_detection") Boolean enablePromptInjectionDetection
    ) implements Tool {
        /**
         * Creates a new ComputerUse tool (defaults to browser environment).
         */
        public ComputerUse() {
            this("computer_use", "browser", null, null);
        }

        /**
         * Returns a new builder for ComputerUse.
         * @return a new builder for ComputerUse.
         */
        public static Builder builder() {
            return new Builder();
        }

        /** Builder for {@link ComputerUse}. */
        public static class Builder {
            private String type = "computer_use";
            private String environment = "browser";
            private List<String> excludedPredefinedFunctions;
            private Boolean enablePromptInjectionDetection;

            /** Creates a new Builder. */
            public Builder() {}

            /**
             * Sets the type.
             *
             * @param type The type.
             * @return This builder.
             */
            public Builder type(String type) { this.type = type; return this; }

            /**
             * Sets the environment.
             *
             * @param environment The environment.
             * @return This builder.
             */
            public Builder environment(String environment) { this.environment = environment; return this; }

            /**
             * Sets the excluded predefined functions.
             *
             * @param excludedPredefinedFunctions The excluded predefined functions.
             * @return This builder.
             */
            public Builder excludedPredefinedFunctions(List<String> excludedPredefinedFunctions) { this.excludedPredefinedFunctions = excludedPredefinedFunctions; return this; }

            /**
             * Sets whether to enable prompt injection detection.
             *
             * @param enablePromptInjectionDetection Whether to enable prompt injection detection.
             * @return This builder.
             */
            public Builder enablePromptInjectionDetection(Boolean enablePromptInjectionDetection) { this.enablePromptInjectionDetection = enablePromptInjectionDetection; return this; }

            /**
             * Builds the ComputerUse.
             *
             * @return A new ComputerUse instance.
             */
            public ComputerUse build() {
                return new ComputerUse(type, environment, excludedPredefinedFunctions, enablePromptInjectionDetection);
            }
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

        /**
         * Returns a new builder for McpServer.
         * @return a new builder for McpServer.
         */
        public static Builder builder() {
            return new Builder();
        }

        /** Builder for {@link McpServer}. */
        public static class Builder {
            private String type = "mcp_server";
            private String name;
            private String url;
            private Map<String, String> headers;
            private List<AllowedTools> allowedTools;

            /** Creates a new Builder. */
            public Builder() {}

            /**
             * Sets the type.
             *
             * @param type The type.
             * @return This builder.
             */
            public Builder type(String type) { this.type = type; return this; }

            /**
             * Sets the name.
             *
             * @param name The name.
             * @return This builder.
             */
            public Builder name(String name) { this.name = name; return this; }

            /**
             * Sets the url.
             *
             * @param url The url.
             * @return This builder.
             */
            public Builder url(String url) { this.url = url; return this; }

            /**
             * Sets the headers.
             *
             * @param headers The headers.
             * @return This builder.
             */
            public Builder headers(Map<String, String> headers) { this.headers = headers; return this; }

            /**
             * Sets the allowed tools.
             *
             * @param allowedTools The allowed tools.
             * @return This builder.
             */
            public Builder allowedTools(List<AllowedTools> allowedTools) { this.allowedTools = allowedTools; return this; }

            /**
             * Builds the McpServer.
             *
             * @return A new McpServer instance.
             */
            public McpServer build() {
                return new McpServer(type, name, url, headers, allowedTools);
            }
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

    /**
     * Tool definition for Google Maps.
     *
     * @param type The type of tool (must be "google_maps").
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleMaps(
        String type
    ) implements Tool {
        /**
         * Creates a new GoogleMaps tool.
         */
        public GoogleMaps() {
            this("google_maps");
        }
    }

    /**
     * Configuration for Vertex AI Search.
     * 
     * @param engine     The Vertex AI Search engine list.
     * @param datastores The Vertex AI Search datastores.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record VertexAISearchConfig(
        String engine,
        List<String> datastores
    ) {}

    /**
     * Configuration for Exa AI Search.
     *
     * @param apiKey       The API key for Exa AI Search.
     * @param customConfig Custom configs for Exa AI Search.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ExaAISearchConfig(
        @JsonProperty("api_key") String apiKey,
        @JsonProperty("custom_config") Map<String, Object> customConfig
    ) {}

    /**
     * Configuration for Parallel AI Search.
     *
     * @param apiKey       The API key for Parallel AI Search.
     * @param customConfig Custom configs for Parallel AI Search.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ParallelAISearchConfig(
        @JsonProperty("api_key") String apiKey,
        @JsonProperty("custom_config") Map<String, Object> customConfig
    ) {}

    /**
     * The definition of the Rag resource.
     *
     * @param ragCorpus   RagCorpora resource name.
     * @param ragFileIds  rag_file_id list.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record RagResource(
        @JsonProperty("rag_corpus") String ragCorpus,
        @JsonProperty("rag_file_ids") List<String> ragFileIds
    ) {}

    /**
     * Config for Hybrid Search.
     *
     * @param alpha Alpha value controls the weight between dense and sparse vector search results.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record HybridSearch(
        Float alpha
    ) {}

    /**
     * Config for filters.
     *
     * @param vectorDistanceThreshold Only returns contexts with vector distance smaller than threshold.
     * @param vectorSimilarityThreshold Only returns contexts with vector similarity larger than threshold.
     * @param metadataFilter String for metadata filtering.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Filter(
        @JsonProperty("vector_distance_threshold") Double vectorDistanceThreshold,
        @JsonProperty("vector_similarity_threshold") Double vectorSimilarityThreshold,
        @JsonProperty("metadata_filter") String metadataFilter
    ) {}

    /**
     * Config for ranking and reranking.
     *
     * @param rankingConfig The ranking config type (e.g. "rank_service").
     * @param modelName The model name of the rank service.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Ranking(
        @JsonProperty("ranking_config") String rankingConfig,
        @JsonProperty("model_name") String modelName
    ) {}

    /**
     * Specifies the context retrieval config.
     *
     * @param topK The number of contexts to retrieve.
     * @param hybridSearch Config for Hybrid Search.
     * @param filter Config for filters.
     * @param ranking Config for ranking and reranking.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record RagRetrievalConfig(
        @JsonProperty("top_k") Integer topK,
        @JsonProperty("hybrid_search") HybridSearch hybridSearch,
        Filter filter,
        Ranking ranking
    ) {}

    /**
     * Use to specify configuration for RAG Store.
     *
     * @param ragResources The representation of the rag source.
     * @param similarityTopK Number of top k results to return.
     * @param vectorDistanceThreshold Only return results with vector distance smaller than threshold.
     * @param ragRetrievalConfig The retrieval config for the Rag query.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record RagStoreConfig(
        @JsonProperty("rag_resources") List<RagResource> ragResources,
        @Deprecated @JsonProperty("similarity_top_k") Integer similarityTopK,
        @Deprecated @JsonProperty("vector_distance_threshold") Double vectorDistanceThreshold,
        @JsonProperty("rag_retrieval_config") RagRetrievalConfig ragRetrievalConfig
    ) {}

    /**
     * Tool definition for Retrieval.
     *
     * @param type                     The type of tool (must be "retrieval").
     * @param retrievalTypes           The types of file retrieval to enable.
     * @param vertexAiSearchConfig     Configuration for Vertex AI Search.
     * @param exaAiSearchConfig        Configuration for Exa AI Search.
     * @param parallelAiSearchConfig   Configuration for Parallel AI Search.
     * @param ragStoreConfig           Configuration for RAG Store.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Retrieval(
        String type,
        @JsonProperty("retrieval_types") List<String> retrievalTypes,
        @JsonProperty("vertex_ai_search_config") VertexAISearchConfig vertexAiSearchConfig,
        @JsonProperty("exa_ai_search_config") ExaAISearchConfig exaAiSearchConfig,
        @JsonProperty("parallel_ai_search_config") ParallelAISearchConfig parallelAiSearchConfig,
        @JsonProperty("rag_store_config") RagStoreConfig ragStoreConfig
    ) implements Tool {
        /**
         * Creates a new Retrieval tool with default type "retrieval".
         *
         * @param retrievalTypes           The retrieval types.
         * @param vertexAiSearchConfig     The vertex AI search config.
         * @param exaAiSearchConfig        The Exa AI search config.
         * @param parallelAiSearchConfig   The Parallel AI search config.
         * @param ragStoreConfig           The RAG store config.
         */
        public Retrieval(
            List<String> retrievalTypes,
            VertexAISearchConfig vertexAiSearchConfig,
            ExaAISearchConfig exaAiSearchConfig,
            ParallelAISearchConfig parallelAiSearchConfig,
            RagStoreConfig ragStoreConfig
        ) {
            this("retrieval", retrievalTypes, vertexAiSearchConfig, exaAiSearchConfig, parallelAiSearchConfig, ragStoreConfig);
        }

        /**
         * Returns a new builder for Retrieval.
         * @return a new builder for Retrieval.
         */
        public static Builder builder() {
            return new Builder();
        }

        /** Builder for {@link Retrieval}. */
        public static class Builder {
            private String type = "retrieval";
            private List<String> retrievalTypes;
            private VertexAISearchConfig vertexAiSearchConfig;
            private ExaAISearchConfig exaAiSearchConfig;
            private ParallelAISearchConfig parallelAiSearchConfig;
            private RagStoreConfig ragStoreConfig;

            /** Creates a new Builder. */
            public Builder() {}

            /**
             * Sets the type.
             *
             * @param type The type.
             * @return This builder.
             */
            public Builder type(String type) { this.type = type; return this; }

            /**
             * Sets the retrieval types.
             *
             * @param retrievalTypes The retrieval types.
             * @return This builder.
             */
            public Builder retrievalTypes(List<String> retrievalTypes) { this.retrievalTypes = retrievalTypes; return this; }

            /**
             * Sets Vertex AI Search config.
             *
             * @param vertexAiSearchConfig Vertex AI Search config.
             * @return This builder.
             */
            public Builder vertexAiSearchConfig(VertexAISearchConfig vertexAiSearchConfig) { this.vertexAiSearchConfig = vertexAiSearchConfig; return this; }

            /**
             * Sets Exa AI Search config.
             *
             * @param exaAiSearchConfig Exa AI Search config.
             * @return This builder.
             */
            public Builder exaAiSearchConfig(ExaAISearchConfig exaAiSearchConfig) { this.exaAiSearchConfig = exaAiSearchConfig; return this; }

            /**
             * Sets Parallel AI Search config.
             *
             * @param parallelAiSearchConfig Parallel AI Search config.
             * @return This builder.
             */
            public Builder parallelAiSearchConfig(ParallelAISearchConfig parallelAiSearchConfig) { this.parallelAiSearchConfig = parallelAiSearchConfig; return this; }

            /**
             * Sets RAG Store config.
             *
             * @param ragStoreConfig RAG Store config.
             * @return This builder.
             */
            public Builder ragStoreConfig(RagStoreConfig ragStoreConfig) { this.ragStoreConfig = ragStoreConfig; return this; }

            /**
             * Builds the Retrieval tool.
             *
             * @return The Retrieval tool.
             */
            public Retrieval build() {
                return new Retrieval(
                    type, retrievalTypes, vertexAiSearchConfig, exaAiSearchConfig,
                    parallelAiSearchConfig, ragStoreConfig
                );
            }
        }
    }

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
