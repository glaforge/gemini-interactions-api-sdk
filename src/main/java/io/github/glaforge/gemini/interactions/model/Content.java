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
 * Represents the content of the response or input.
 * This is a sealed interface corresponding to the 'Content' schema in the OpenAPI spec.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Content.TextContent.class, name = "text"),
    @JsonSubTypes.Type(value = Content.ImageContent.class, name = "image"),
    @JsonSubTypes.Type(value = Content.AudioContent.class, name = "audio"),
    @JsonSubTypes.Type(value = Content.DocumentContent.class, name = "document"),
    @JsonSubTypes.Type(value = Content.VideoContent.class, name = "video"),
    @JsonSubTypes.Type(value = Content.ThoughtContent.class, name = "thought"),
    @JsonSubTypes.Type(value = Content.FunctionCallContent.class, name = "function_call"),
    @JsonSubTypes.Type(value = Content.FunctionResultContent.class, name = "function_result"),
    @JsonSubTypes.Type(value = Content.CodeExecutionCallContent.class, name = "code_execution_call"),
    @JsonSubTypes.Type(value = Content.CodeExecutionResultContent.class, name = "code_execution_result"),
    @JsonSubTypes.Type(value = Content.UrlContextCallContent.class, name = "url_context_call"),
    @JsonSubTypes.Type(value = Content.UrlContextResultContent.class, name = "url_context_result"),
    @JsonSubTypes.Type(value = Content.GoogleSearchCallContent.class, name = "google_search_call"),
    @JsonSubTypes.Type(value = Content.GoogleSearchResultContent.class, name = "google_search_result"),
    @JsonSubTypes.Type(value = Content.McpServerToolCallContent.class, name = "mcp_server_tool_call"),
    @JsonSubTypes.Type(value = Content.McpServerToolResultContent.class, name = "mcp_server_tool_result"),
    @JsonSubTypes.Type(value = Content.FileSearchResultContent.class, name = "file_search_result")
})
public sealed interface Content permits
    Content.TextContent,
    Content.ImageContent,
    Content.AudioContent,
    Content.DocumentContent,
    Content.VideoContent,
    Content.ThoughtContent,
    Content.FunctionCallContent,
    Content.FunctionResultContent,
    Content.CodeExecutionCallContent,
    Content.CodeExecutionResultContent,
    Content.UrlContextCallContent,
    Content.UrlContextResultContent,
    Content.GoogleSearchCallContent,
    Content.GoogleSearchResultContent,
    Content.McpServerToolCallContent,
    Content.McpServerToolResultContent,
    Content.FileSearchResultContent {

    /**
     * Returns the type of content.
     *
     * @return The content type.
     */
    String type();

    /**
     * Resolution of the media.
     */
    public enum Resolution {
        /** Low resolution. */
        @JsonProperty("low") LOW,
        /** Medium resolution. */
        @JsonProperty("medium") MEDIUM,
        /** High resolution. */
        @JsonProperty("high") HIGH
    }

    /**
     * Status of the URL context retrieval.
     */
    public enum UrlContextStatus {
        /** Successful retrieval. */
        @JsonProperty("success") SUCCESS,
        /** Error during retrieval. */
        @JsonProperty("error") ERROR,
        /** Content behind paywall. */
        @JsonProperty("paywall") PAYWALL,
        /** Content deemed unsafe. */
        @JsonProperty("unsafe") UNSAFE
    }

    // --- Basic Media Types ---

    /**
     * Content containing text.
     *
     * @param type        The type of content (must be "text").
     * @param text        The text content.
     * @param annotations List of annotations for the text.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record TextContent(
        String type,
        String text,
        List<Annotation> annotations
    ) implements Content {
        /**
         * Creates a new TextContent with default type "text".
         *
         * @param text The text content.
         */
        public TextContent(String text) {
            this("text", text, null);
        }
    }

    /**
     * Annotation for text content.
     *
     * @param startIndex Start index of the annotation.
     * @param endIndex   End index of the annotation.
     * @param source     Source of the annotation.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Annotation(
        @JsonProperty("start_index") Integer startIndex,
        @JsonProperty("end_index") Integer endIndex,
        String source
    ) {}

    /**
     * Content containing an image.
     *
     * @param type       The type of content (must be "image").
     * @param data       Base64 encoded image data.
     * @param uri        URI of the image.
     * @param mimeType   MIME type of the image.
     * @param resolution Resolution of the image.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ImageContent(
        String type,
        String data,
        String uri,
        @JsonProperty("mime_type") String mimeType,
        Resolution resolution
    ) implements Content {
        /**
         * Creates a new ImageContent with default type "image".
         *
         * @param data     The base64 encoded image data.
         * @param mimeType The MIME type of the image.
         */
        public ImageContent(String data, String mimeType) {
            this("image", data, null, mimeType, null);
        }
    }

    /**
     * Content containing audio.
     *
     * @param type     The type of content (must be "audio").
     * @param data     Base64 encoded audio data.
     * @param uri      URI of the audio.
     * @param mimeType MIME type of the audio.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record AudioContent(
        String type,
        String data,
        String uri,
        @JsonProperty("mime_type") String mimeType
    ) implements Content {}

    /**
     * Content containing a document.
     *
     * @param type     The type of content (must be "document").
     * @param data     Base64 encoded document data.
     * @param uri      URI of the document.
     * @param mimeType MIME type of the document.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record DocumentContent(
        String type,
        String data,
        String uri,
        @JsonProperty("mime_type") String mimeType
    ) implements Content {}

    /**
     * Content containing video.
     *
     * @param type       The type of content (must be "video").
     * @param data       Base64 encoded video data.
     * @param uri        URI of the video.
     * @param mimeType   MIME type of the video.
     * @param resolution Resolution of the video.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record VideoContent(
        String type,
        String data,
        String uri,
        @JsonProperty("mime_type") String mimeType,
        Resolution resolution
    ) implements Content {}

    // --- Thinking ---

    /**
     * Content containing thought process.
     *
     * @param type      The type of content (must be "thought").
     * @param signature The signature of the thought.
     * @param summary   The summary of the thought process.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ThoughtContent(
        String type,
        String signature,
        // The summary is a list of Content (Text or Image), defined as ThoughtSummary in spec.
        // Spec says ThoughtSummary items oneOf TextContent, ImageContent.
        // We can use List<Content> but better to be specific if we can, or just Content.
        // Spec: "items": { "oneOf": [TextContent, ImageContent] }
        List<Content> summary
    ) implements Content {}

    // --- Function Calling ---

    /**
     * Content representing a function call.
     *
     * @param type      The type of content (must be "function_call").
     * @param id        The unique identifier for the function call.
     * @param name      The name of the function to call.
     * @param arguments The arguments to pass to the function.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record FunctionCallContent(
        String type,
        String id,
        String name,
        Map<String, Object> arguments
    ) implements Content {}

    /**
     * Content representing the result of a function call.
     *
     * @param type    The type of content (must be "function_result").
     * @param callId  The ID of the function call this result is for.
     * @param name    The name of the function.
     * @param isError Whether the function call resulted in an error.
     * @param result  The result of the function call.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record FunctionResultContent(
        String type,
        @JsonProperty("call_id") String callId,
        String name,
        @JsonProperty("is_error") Boolean isError,
        Object result // oneOf object, object(items), string
    ) implements Content {}

    // --- Code Execution ---

    /**
     * Content representing a code execution call.
     *
     * @param type      The type of content (must be "code_execution_call").
     * @param id        The unique identifier for the code execution call.
     * @param arguments The arguments for the code execution (language and code).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record CodeExecutionCallContent(
        String type,
        String id,
        CodeExecutionCallArguments arguments
    ) implements Content {}

    /**
     * Arguments for a code execution call.
     *
     * @param language The programming language (e.g., "python").
     * @param code     The code to execute.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record CodeExecutionCallArguments(
        String language, // e.g. "python"
        String code
    ) {}

    /**
     * Content representing the result of a code execution.
     *
     * @param type      The type of content (must be "code_execution_result").
     * @param callId    The ID of the code execution call.
     * @param result    The result of the code execution (stdout/stderr).
     * @param isError   Whether the execution resulted in an error.
     * @param signature The signature of the result.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record CodeExecutionResultContent(
        String type,
        @JsonProperty("call_id") String callId,
        String result,
        @JsonProperty("is_error") Boolean isError,
        String signature
    ) implements Content {}

    // --- URL Context ---

    /**
     * Content representing a URL context call.
     *
     * @param type      The type of content (must be "url_context_call").
     * @param id        The unique identifier for the URL context call.
     * @param arguments The arguments for the URL context (list of URLs).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record UrlContextCallContent(
        String type,
        String id,
        UrlContextCallArguments arguments
    ) implements Content {}

    /**
     * Arguments for a URL context call.
     *
     * @param urls List of URLs to retrieve context for.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record UrlContextCallArguments(
        List<String> urls
    ) {}

    /**
     * Content representing the result of a URL context call.
     *
     * @param type      The type of content (must be "url_context_result").
     * @param callId    The ID of the URL context call.
     * @param signature The signature of the result.
     * @param result    The list of URL context results.
     * @param isError   Whether the call resulted in an error.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record UrlContextResultContent(
        String type,
        @JsonProperty("call_id") String callId,
        String signature,
        List<UrlContextResult> result,
        @JsonProperty("is_error") Boolean isError
    ) implements Content {}

    /**
     * Result of a single URL context retrieval.
     *
     * @param url    The URL.
     * @param status The status of the retrieval.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record UrlContextResult(
        String url,
        UrlContextStatus status
    ) {}

    // --- Google Search ---

    /**
     * Content representing a Google Search call.
     *
     * @param type      The type of content (must be "google_search_call").
     * @param id        The unique identifier for the Google Search call.
     * @param arguments The arguments for the Google Search (queries).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleSearchCallContent(
        String type,
        String id,
        GoogleSearchCallArguments arguments
    ) implements Content {}

    /**
     * Arguments for a Google Search call.
     *
     * @param queries List of search queries.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleSearchCallArguments(
        List<String> queries
    ) {}

    /**
     * Content representing the result of a Google Search call.
     *
     * @param type      The type of content (must be "google_search_result").
     * @param callId    The ID of the Google Search call.
     * @param signature The signature of the result.
     * @param result    The list of Google Search results.
     * @param isError   Whether the call resulted in an error.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleSearchResultContent(
        String type,
        @JsonProperty("call_id") String callId,
        String signature,
        List<GoogleSearchResult> result,
        @JsonProperty("is_error") Boolean isError
    ) implements Content {}

    /**
     * Result of a single Google Search.
     *
     * @param url             The URL of the result.
     * @param title           The title of the result.
     * @param renderedContent The rendered content of the result.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleSearchResult(
        String url,
        String title,
        @JsonProperty("rendered_content") String renderedContent
    ) {}

    // --- MCP Server ---

    /**
     * Content representing an MCP server tool call.
     *
     * @param type       The type of content (must be "mcp_server_tool_call").
     * @param id         The unique identifier for the tool call.
     * @param name       The name of the tool to call.
     * @param serverName The name of the MCP server.
     * @param arguments  The arguments to pass to the tool.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record McpServerToolCallContent(
        String type,
        String id,
        String name,
        @JsonProperty("server_name") String serverName,
        Map<String, Object> arguments
    ) implements Content {}

    /**
     * Content representing the result of an MCP server tool call.
     *
     * @param type       The type of content (must be "mcp_server_tool_result").
     * @param callId     The ID of the tool call this result is for.
     * @param name       The name of the tool.
     * @param serverName The name of the MCP server.
     * @param result     The result of the tool call.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record McpServerToolResultContent(
        String type,
        @JsonProperty("call_id") String callId,
        String name,
        @JsonProperty("server_name") String serverName,
        Object result // oneOf object, object(items), string
    ) implements Content {}

    // --- File Search ---

    /**
     * Content representing the result of a file search.
     *
     * @param type   The type of content (must be "file_search_result").
     * @param result The list of file search results.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record FileSearchResultContent(
        String type,
        List<FileSearchResult> result
    ) implements Content {}

    /**
     * Result of a single file search.
     *
     * @param title           The title of the file.
     * @param text            The text content of the file.
     * @param fileSearchStore The file search store used.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record FileSearchResult(
        String title,
        String text,
        @JsonProperty("file_search_store") String fileSearchStore
    ) {}
}
