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
 * Represents a step in an interaction.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Step.UserInputStep.class, name = "user_input"),
    @JsonSubTypes.Type(value = Step.ModelOutputStep.class, name = "model_output"),
    @JsonSubTypes.Type(value = Step.ThoughtStep.class, name = "thought"),
    @JsonSubTypes.Type(value = Step.FunctionCallStep.class, name = "function_call"),
    @JsonSubTypes.Type(value = Step.FunctionResultStep.class, name = "function_result"),
    @JsonSubTypes.Type(value = Step.CodeExecutionCallStep.class, name = "code_execution_call"),
    @JsonSubTypes.Type(value = Step.CodeExecutionResultStep.class, name = "code_execution_result"),
    @JsonSubTypes.Type(value = Step.UrlContextCallStep.class, name = "url_context_call"),
    @JsonSubTypes.Type(value = Step.UrlContextResultStep.class, name = "url_context_result"),
    @JsonSubTypes.Type(value = Step.GoogleSearchCallStep.class, name = "google_search_call"),
    @JsonSubTypes.Type(value = Step.GoogleSearchResultStep.class, name = "google_search_result"),
    @JsonSubTypes.Type(value = Step.McpServerToolCallStep.class, name = "mcp_server_tool_call"),
    @JsonSubTypes.Type(value = Step.McpServerToolResultStep.class, name = "mcp_server_tool_result"),
    @JsonSubTypes.Type(value = Step.FileSearchCallStep.class, name = "file_search_call"),
    @JsonSubTypes.Type(value = Step.FileSearchResultStep.class, name = "file_search_result"),
    @JsonSubTypes.Type(value = Step.GoogleMapsCallStep.class, name = "google_maps_call"),
    @JsonSubTypes.Type(value = Step.GoogleMapsResultStep.class, name = "google_maps_result")
})
public sealed interface Step permits
    Step.UserInputStep,
    Step.ModelOutputStep,
    Step.ThoughtStep,
    Step.FunctionCallStep,
    Step.FunctionResultStep,
    Step.CodeExecutionCallStep,
    Step.CodeExecutionResultStep,
    Step.UrlContextCallStep,
    Step.UrlContextResultStep,
    Step.GoogleSearchCallStep,
    Step.GoogleSearchResultStep,
    Step.McpServerToolCallStep,
    Step.McpServerToolResultStep,
    Step.FileSearchCallStep,
    Step.FileSearchResultStep,
    Step.GoogleMapsCallStep,
    Step.GoogleMapsResultStep {

    /**
     * Gets the type.
     * @return the type.
     */
    String type();

    // --- Input / Output Steps ---

    /**
     * UserInputStep.
     *
     * @param type type parameter.
     * @param content content parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record UserInputStep(
        String type,
        List<Content> content
    ) implements Step {}

    /**
     * ModelOutputStep.
     *
     * @param type type parameter.
     * @param content content parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ModelOutputStep(
        String type,
        List<Content> content
    ) implements Step {}

    // --- Thinking ---

    /**
     * ThoughtStep.
     *
     * @param type type parameter.
     * @param signature signature parameter.
     * @param summary summary parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ThoughtStep(
        String type,
        String signature,
        List<Content> summary
    ) implements Step {}

    // --- Function Calling ---

    /**
     * FunctionCallStep.
     *
     * @param type type parameter.
     * @param id id parameter.
     * @param name name parameter.
     * @param arguments arguments parameter.
     * @param signature signature parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record FunctionCallStep(
        String type,
        String id,
        String name,
        Map<String, Object> arguments,
        String signature
    ) implements Step {}

    /**
     * FunctionResultStep.
     *
     * @param type type parameter.
     * @param callId callId parameter.
     * @param name name parameter.
     * @param isError isError parameter.
     * @param result result parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record FunctionResultStep(
        String type,
        @JsonProperty("call_id") String callId,
        String name,
        @JsonProperty("is_error") Boolean isError,
        Object result // string, object, or ToolResult with List<Content> items
    ) implements Step {}

    // --- Code Execution ---

    /**
     * CodeExecutionCallStep.
     *
     * @param type type parameter.
     * @param id id parameter.
     * @param arguments arguments parameter.
     * @param signature signature parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record CodeExecutionCallStep(
        String type,
        String id,
        CodeExecutionCallArguments arguments,
        String signature
    ) implements Step {}

    /**
     * CodeExecutionCallArguments.
     *
     * @param language language parameter.
     * @param code code parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record CodeExecutionCallArguments(
        String language, // e.g. "python"
        String code
    ) {}

    /**
     * CodeExecutionResultStep.
     *
     * @param type type parameter.
     * @param callId callId parameter.
     * @param result result parameter.
     * @param isError isError parameter.
     * @param signature signature parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record CodeExecutionResultStep(
        String type,
        @JsonProperty("call_id") String callId,
        String result,
        @JsonProperty("is_error") Boolean isError,
        String signature
    ) implements Step {}

    // --- URL Context ---

    /**
     * UrlContextCallStep.
     *
     * @param type type parameter.
     * @param id id parameter.
     * @param arguments arguments parameter.
     * @param signature signature parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record UrlContextCallStep(
        String type,
        String id,
        UrlContextCallArguments arguments,
        String signature
    ) implements Step {}

    /**
     * UrlContextCallArguments.
     *
     * @param urls urls parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record UrlContextCallArguments(
        List<String> urls
    ) {}

    /**
     * UrlContextResultStep.
     *
     * @param type type parameter.
     * @param callId callId parameter.
     * @param signature signature parameter.
     * @param result result parameter.
     * @param isError isError parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record UrlContextResultStep(
        String type,
        @JsonProperty("call_id") String callId,
        String signature,
        List<UrlContextResult> result,
        @JsonProperty("is_error") Boolean isError
    ) implements Step {}

    /**
     * UrlContextResult.
     *
     * @param url url parameter.
     * @param status status parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record UrlContextResult(
        String url,
        Content.UrlContextStatus status
    ) {}

    // --- Google Search ---

    /**
     * GoogleSearchCallStep.
     *
     * @param type type parameter.
     * @param id id parameter.
     * @param arguments arguments parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleSearchCallStep(
        String type,
        String id,
        GoogleSearchCallArguments arguments
    ) implements Step {}

    /**
     * GoogleSearchCallArguments.
     *
     * @param queries queries parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleSearchCallArguments(
        List<String> queries
    ) {}

    /**
     * GoogleSearchResultStep.
     *
     * @param type type parameter.
     * @param callId callId parameter.
     * @param signature signature parameter.
     * @param result result parameter.
     * @param isError isError parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleSearchResultStep(
        String type,
        @JsonProperty("call_id") String callId,
        String signature,
        List<GoogleSearchResult> result,
        @JsonProperty("is_error") Boolean isError
    ) implements Step {}

    /**
     * GoogleSearchResult.
     *
     * @param url url parameter.
     * @param title title parameter.
     * @param renderedContent renderedContent parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleSearchResult(
        String url,
        String title,
        @JsonProperty("rendered_content") String renderedContent
    ) {}

    // --- MCP Server ---

    /**
     * McpServerToolCallStep.
     *
     * @param type type parameter.
     * @param id id parameter.
     * @param name name parameter.
     * @param serverName serverName parameter.
     * @param arguments arguments parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record McpServerToolCallStep(
        String type,
        String id,
        String name,
        @JsonProperty("server_name") String serverName,
        Map<String, Object> arguments
    ) implements Step {}

    /**
     * McpServerToolResultStep.
     *
     * @param type type parameter.
     * @param callId callId parameter.
     * @param name name parameter.
     * @param serverName serverName parameter.
     * @param result result parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record McpServerToolResultStep(
        String type,
        @JsonProperty("call_id") String callId,
        String name,
        @JsonProperty("server_name") String serverName,
        Object result 
    ) implements Step {}

    // --- File Search ---

    /**
     * FileSearchCallStep.
     *
     * @param type type parameter.
     * @param id id parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record FileSearchCallStep(
        String type,
        String id
    ) implements Step {}

    /**
     * FileSearchResultStep.
     *
     * @param type type parameter.
     * @param result result parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record FileSearchResultStep(
        String type,
        List<FileSearchResult> result
    ) implements Step {}

    /**
     * FileSearchResult.
     *
     * @param title title parameter.
     * @param text text parameter.
     * @param fileSearchStore fileSearchStore parameter.
     * @param customMetadata customMetadata parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record FileSearchResult(
        String title,
        String text,
        @JsonProperty("file_search_store") String fileSearchStore,
        @JsonProperty("custom_metadata") List<Map<String, Object>> customMetadata
    ) {}

    // --- Google Maps ---

    /**
     * GoogleMapsCallStep.
     *
     * @param type type parameter.
     * @param id id parameter.
     * @param arguments arguments parameter.
     * @param signature signature parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleMapsCallStep(
        String type,
        String id,
        GoogleMapsCallArguments arguments,
        String signature
    ) implements Step {}

    /**
     * GoogleMapsCallArguments.
     *
     * @param queries queries parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleMapsCallArguments(
        List<String> queries
    ) {}

    /**
     * GoogleMapsResultStep.
     *
     * @param type type parameter.
     * @param callId callId parameter.
     * @param signature signature parameter.
     * @param result result parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleMapsResultStep(
        String type,
        @JsonProperty("call_id") String callId,
        String signature,
        List<GoogleMapsResult> result
    ) implements Step {}

    /**
     * GoogleMapsResult.
     *
     * @param places places parameter.
     * @param widgetContextToken widgetContextToken parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleMapsResult(
        List<Places> places,
        @JsonProperty("widget_context_token") String widgetContextToken
    ) {}

    /**
     * Places.
     *
     * @param placeId placeId parameter.
     * @param name name parameter.
     * @param url url parameter.
     * @param reviewSnippets reviewSnippets parameter.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Places(
        @JsonProperty("place_id") String placeId,
        String name,
        String url,
        @JsonProperty("review_snippets") List<Content.ReviewSnippet> reviewSnippets
    ) {}
}
