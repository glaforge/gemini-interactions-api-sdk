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
 * Represents an event in the Server-Sent Events (SSE) stream.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "event_type",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Events.InteractionEvent.class, name = "interaction.start"),
    @JsonSubTypes.Type(value = Events.InteractionEvent.class, name = "interaction.complete"),
    @JsonSubTypes.Type(value = Events.InteractionStatusUpdate.class, name = "interaction.status_update"),
    @JsonSubTypes.Type(value = Events.ContentStart.class, name = "content.start"),
    @JsonSubTypes.Type(value = Events.ContentDelta.class, name = "content.delta"),
    @JsonSubTypes.Type(value = Events.ContentStop.class, name = "content.stop"),
    @JsonSubTypes.Type(value = Events.ErrorEvent.class, name = "error")
})
public sealed interface Events permits
    Events.InteractionEvent,
    Events.InteractionStatusUpdate,
    Events.ContentStart,
    Events.ContentDelta,
    Events.ContentStop,
    Events.ErrorEvent {

    /**
     * Enumeration of all supported event types.
     */
    enum EventType {
        /** Interaction has started. */
        @JsonProperty("interaction.start")
        INTERACTION_START("interaction.start"),
        /** Interaction has completed. */
        @JsonProperty("interaction.complete")
        INTERACTION_COMPLETE("interaction.complete"),
        /** Interaction status has been updated. */
        @JsonProperty("interaction.status_update")
        INTERACTION_STATUS_UPDATE("interaction.status_update"),
        /** Content generation has started. */
        @JsonProperty("content.start")
        CONTENT_START("content.start"),
        /** Content delta has been received. */
        @JsonProperty("content.delta")
        CONTENT_DELTA("content.delta"),
        /** Content generation has stopped. */
        @JsonProperty("content.stop")
        CONTENT_STOP("content.stop"),
        /** An error occurred. */
        @JsonProperty("error")
        ERROR("error");

        private final String jsonValue;

        EventType(String jsonValue) {
            this.jsonValue = jsonValue;
        }

        /**
         * Returns the JSON string value of the event type.
         * @return the JSON string value of the event type.
         */
        public String getJsonValue() {
            return jsonValue;
        }

        @Override
        public String toString() {
            return jsonValue;
        }
    }

    /**
     * Returns the type of the event.
     * @return the type of the event.
     */
    @JsonProperty("event_type")
    EventType eventType();

    /**
     * Returns the unique event identifier for resumption.
     * @return the unique event identifier for resumption.
     */
    @JsonProperty("event_id")
    String eventId(); // Common to all events for resumption

    /**
     * Event indicating the start or completion of an interaction.
     *
     * @param eventType   The type of event ("interaction.start" or "interaction.complete").
     * @param eventId     The unique identifier for the event.
     * @param interaction The interaction object.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record InteractionEvent(
        @JsonProperty("event_type") EventType eventType,
        @JsonProperty("event_id") String eventId,
        Interaction interaction
    ) implements Events {}

    /**
     * Event indicating a status update for an interaction.
     *
     * @param eventType     The type of event ("interaction.status_update").
     * @param eventId       The unique identifier for the event.
     * @param interactionId The ID of the interaction.
     * @param status        The new status of the interaction.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record InteractionStatusUpdate(
        @JsonProperty("event_type") EventType eventType,
        @JsonProperty("event_id") String eventId,
        @JsonProperty("interaction_id") String interactionId,
        String status
    ) implements Events {}

    /**
     * Event indicating the start of a content part.
     *
     * @param eventType The type of event ("content.start").
     * @param eventId   The unique identifier for the event.
     * @param index     The index of the content part.
     * @param content   The content that is starting.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ContentStart(
        @JsonProperty("event_type") EventType eventType,
        @JsonProperty("event_id") String eventId,
        Integer index,
        Content content
    ) implements Events {}

    /**
     * Event indicating the end of a content part.
     *
     * @param eventType The type of event ("content.stop").
     * @param eventId   The unique identifier for the event.
     * @param index     The index of the content part.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ContentStop(
        @JsonProperty("event_type") EventType eventType,
        @JsonProperty("event_id") String eventId,
        Integer index
    ) implements Events {}

    /**
     * Event indicating an error.
     *
     * @param eventType The type of event ("error").
     * @param eventId   The unique identifier for the event.
     * @param error     The error details.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ErrorEvent(
        @JsonProperty("event_type") EventType eventType,
        @JsonProperty("event_id") String eventId,
        Error error
    ) implements Events {}

    /**
     * Error details.
     *
     * @param code    The error code.
     * @param message The error message.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Error(
        String code,
        String message
    ) {}

    /**
     * Event indicating a delta update for a content part.
     *
     * @param eventType The type of event ("content.delta").
     * @param eventId   The unique identifier for the event.
     * @param index     The index of the content part.
     * @param delta     The delta update.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ContentDelta(
        @JsonProperty("event_type") EventType eventType,
        @JsonProperty("event_id") String eventId,
        Integer index,
        Delta delta
    ) implements Events {}

    /**
     * Represents a delta update for a content part.
     */
    /**
     * Enumeration of all supported delta types.
     */
    enum DeltaType {
        /** Text content delta. */
        @JsonProperty("text") TEXT("text"),
        /** Image content delta. */
        @JsonProperty("image") IMAGE("image"),
        /** Audio content delta. */
        @JsonProperty("audio") AUDIO("audio"),
        /** Document content delta. */
        @JsonProperty("document") DOCUMENT("document"),
        /** Video content delta. */
        @JsonProperty("video") VIDEO("video"),
        /** Thought summary delta. */
        @JsonProperty("thought_summary") THOUGHT_SUMMARY("thought_summary"),
        /** Thought signature delta. */
        @JsonProperty("thought_signature") THOUGHT_SIGNATURE("thought_signature"),
        /** Function call delta. */
        @JsonProperty("function_call") FUNCTION_CALL("function_call"),
        /** Function result delta. */
        @JsonProperty("function_result") FUNCTION_RESULT("function_result"),
        /** Code execution call delta. */
        @JsonProperty("code_execution_call") CODE_EXECUTION_CALL("code_execution_call"),
        /** Code execution result delta. */
        @JsonProperty("code_execution_result") CODE_EXECUTION_RESULT("code_execution_result"),
        /** URL context call delta. */
        @JsonProperty("url_context_call") URL_CONTEXT_CALL("url_context_call"),
        /** URL context result delta. */
        @JsonProperty("url_context_result") URL_CONTEXT_RESULT("url_context_result"),
        /** Google Search call delta. */
        @JsonProperty("google_search_call") GOOGLE_SEARCH_CALL("google_search_call"),
        /** Google Search result delta. */
        @JsonProperty("google_search_result") GOOGLE_SEARCH_RESULT("google_search_result"),
        /** MCP server tool call delta. */
        @JsonProperty("mcp_server_tool_call") MCP_SERVER_TOOL_CALL("mcp_server_tool_call"),
        /** MCP server tool result delta. */
        @JsonProperty("mcp_server_tool_result") MCP_SERVER_TOOL_RESULT("mcp_server_tool_result"),
        /** File search result delta. */
        @JsonProperty("file_search_result") FILE_SEARCH_RESULT("file_search_result");

        private final String jsonValue;

        DeltaType(String jsonValue) {
            this.jsonValue = jsonValue;
        }

        /**
         * Returns the JSON string value of the delta type.
         * @return the JSON string value of the delta type.
         */
        public String getJsonValue() {
            return jsonValue;
        }

        @Override
        public String toString() {
            return jsonValue;
        }
    }

    /**
     * Interface for all delta updates.
     */
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = TextDelta.class, name = "text"),
        @JsonSubTypes.Type(value = ImageDelta.class, name = "image"),
        @JsonSubTypes.Type(value = AudioDelta.class, name = "audio"),
        @JsonSubTypes.Type(value = DocumentDelta.class, name = "document"),
        @JsonSubTypes.Type(value = VideoDelta.class, name = "video"),
        @JsonSubTypes.Type(value = ThoughtSummaryDelta.class, name = "thought_summary"),
        @JsonSubTypes.Type(value = ThoughtSignatureDelta.class, name = "thought_signature"),
        @JsonSubTypes.Type(value = FunctionCallDelta.class, name = "function_call"),
        @JsonSubTypes.Type(value = FunctionResultDelta.class, name = "function_result"),
        @JsonSubTypes.Type(value = CodeExecutionCallDelta.class, name = "code_execution_call"),
        @JsonSubTypes.Type(value = CodeExecutionResultDelta.class, name = "code_execution_result"),
        @JsonSubTypes.Type(value = UrlContextCallDelta.class, name = "url_context_call"),
        @JsonSubTypes.Type(value = UrlContextResultDelta.class, name = "url_context_result"),
        @JsonSubTypes.Type(value = GoogleSearchCallDelta.class, name = "google_search_call"),
        @JsonSubTypes.Type(value = GoogleSearchResultDelta.class, name = "google_search_result"),
        @JsonSubTypes.Type(value = McpServerToolCallDelta.class, name = "mcp_server_tool_call"),
        @JsonSubTypes.Type(value = McpServerToolResultDelta.class, name = "mcp_server_tool_result"),
        @JsonSubTypes.Type(value = FileSearchResultDelta.class, name = "file_search_result")
    })
    sealed interface Delta permits
        TextDelta, ImageDelta, AudioDelta, DocumentDelta, VideoDelta,
        ThoughtSummaryDelta, ThoughtSignatureDelta,
        FunctionCallDelta, FunctionResultDelta,
        CodeExecutionCallDelta, CodeExecutionResultDelta,
        UrlContextCallDelta, UrlContextResultDelta,
        GoogleSearchCallDelta, GoogleSearchResultDelta,
        McpServerToolCallDelta, McpServerToolResultDelta,
        FileSearchResultDelta {
        /**
         * Returns the type of the delta.
         * @return the type of the delta.
         */
        DeltaType type();
    }

    /**
     * Delta for text content.
     *
     * @param type        The type of delta ("text").
     * @param text        The text content.
     * @param annotations List of annotations.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record TextDelta(DeltaType type, String text, List<Content.Annotation> annotations) implements Delta {}

    /**
     * Delta for image content.
     *
     * @param type       The type of delta ("image").
     * @param data       The base64 encoded image data.
     * @param uri        The URI of the image.
     * @param mimeType   The MIME type of the image.
     * @param resolution The resolution of the image.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ImageDelta(DeltaType type, String data, String uri, @JsonProperty("mime_type") String mimeType, String resolution) implements Delta {}

    /**
     * Delta for audio content.
     *
     * @param type     The type of delta ("audio").
     * @param data     The base64 encoded audio data.
     * @param uri      The URI of the audio.
     * @param mimeType The MIME type of the audio.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record AudioDelta(DeltaType type, String data, String uri, @JsonProperty("mime_type") String mimeType) implements Delta {}

    /**
     * Delta for document content.
     *
     * @param type     The type of delta ("document").
     * @param data     The base64 encoded document data.
     * @param uri      The URI of the document.
     * @param mimeType The MIME type of the document.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record DocumentDelta(DeltaType type, String data, String uri, @JsonProperty("mime_type") String mimeType) implements Delta {}

    /**
     * Delta for video content.
     *
     * @param type       The type of delta ("video").
     * @param data       The base64 encoded video data.
     * @param uri        The URI of the video.
     * @param mimeType   The MIME type of the video.
     * @param resolution The resolution of the video.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record VideoDelta(DeltaType type, String data, String uri, @JsonProperty("mime_type") String mimeType, String resolution) implements Delta {}

    /**
     * Delta for thought summary.
     *
     * @param type    The type of delta ("thought_summary").
     * @param content The thought summary content.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ThoughtSummaryDelta(DeltaType type, Content content) implements Delta {} // content is nested Content (Text/Image)

    /**
     * Delta for thought signature.
     *
     * @param type      The type of delta ("thought_signature").
     * @param signature The thought signature.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ThoughtSignatureDelta(DeltaType type, String signature) implements Delta {}

    /**
     * Delta for function call.
     *
     * @param type      The type of delta ("function_call").
     * @param id        The call ID.
     * @param name      The function name.
     * @param arguments The function arguments.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record FunctionCallDelta(DeltaType type, String id, String name, Map<String, Object> arguments) implements Delta {}

    /**
     * Delta for function result.
     *
     * @param type    The type of delta ("function_result").
     * @param callId  The call ID.
     * @param name    The function name.
     * @param isError Whether the result is an error.
     * @param result  The function result.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record FunctionResultDelta(DeltaType type, @JsonProperty("call_id") String callId, String name, @JsonProperty("is_error") Boolean isError, Object result) implements Delta {}

    /**
     * Delta for code execution call.
     *
     * @param type      The type of delta ("code_execution_call").
     * @param id        The call ID.
     * @param arguments The code execution arguments.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record CodeExecutionCallDelta(DeltaType type, String id, Content.CodeExecutionCallArguments arguments) implements Delta {}

    /**
     * Delta for code execution result.
     *
     * @param type      The type of delta ("code_execution_result").
     * @param callId    The call ID.
     * @param result    The code execution result.
     * @param isError   Whether the result is an error.
     * @param signature The signature.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record CodeExecutionResultDelta(DeltaType type, @JsonProperty("call_id") String callId, String result, @JsonProperty("is_error") Boolean isError, String signature) implements Delta {}

    /**
     * Delta for URL context call.
     *
     * @param type      The type of delta ("url_context_call").
     * @param id        The call ID.
     * @param arguments The URL context arguments.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record UrlContextCallDelta(DeltaType type, String id, Content.UrlContextCallArguments arguments) implements Delta {}

    /**
     * Delta for URL context result.
     *
     * @param type      The type of delta ("url_context_result").
     * @param callId    The call ID.
     * @param signature The signature.
     * @param result    The URL context result.
     * @param isError   Whether the result is an error.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record UrlContextResultDelta(DeltaType type, @JsonProperty("call_id") String callId, String signature, List<Content.UrlContextResult> result, @JsonProperty("is_error") Boolean isError) implements Delta {}

    /**
     * Delta for Google Search call.
     *
     * @param type      The type of delta ("google_search_call").
     * @param id        The call ID.
     * @param arguments The Google Search arguments.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleSearchCallDelta(DeltaType type, String id, Content.GoogleSearchCallArguments arguments) implements Delta {}

    /**
     * Delta for Google Search result.
     *
     * @param type      The type of delta ("google_search_result").
     * @param callId    The call ID.
     * @param signature The signature.
     * @param result    The Google Search result.
     * @param isError   Whether the result is an error.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleSearchResultDelta(DeltaType type, @JsonProperty("call_id") String callId, String signature, List<Content.GoogleSearchResult> result, @JsonProperty("is_error") Boolean isError) implements Delta {}

    /**
     * Delta for MCP server tool call.
     *
     * @param type       The type of delta ("mcp_server_tool_call").
     * @param id         The call ID.
     * @param name       The tool name.
     * @param serverName The server name.
     * @param arguments  The tool arguments.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record McpServerToolCallDelta(DeltaType type, String id, String name, @JsonProperty("server_name") String serverName, Map<String, Object> arguments) implements Delta {}

    /**
     * Delta for MCP server tool result.
     *
     * @param type       The type of delta ("mcp_server_tool_result").
     * @param callId     The call ID.
     * @param name       The tool name.
     * @param serverName The server name.
     * @param result     The tool result.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record McpServerToolResultDelta(DeltaType type, @JsonProperty("call_id") String callId, String name, @JsonProperty("server_name") String serverName, Object result) implements Delta {}

    /**
     * Delta for file search result.
     *
     * @param type   The type of delta ("file_search_result").
     * @param result The list of file search results.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record FileSearchResultDelta(DeltaType type, List<Content.FileSearchResult> result) implements Delta {}
}
