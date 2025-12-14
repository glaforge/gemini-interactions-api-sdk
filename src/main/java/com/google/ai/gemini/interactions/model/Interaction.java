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

package com.google.ai.gemini.interactions.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents the Interaction resource.
 *
 * @param id                    The unique identifier for the interaction.
 * @param model                 The model used for the interaction.
 * @param agent                 The agent used for the interaction.
 * @param object                The object type (always "interaction").
 * @param created               Creation timestamp.
 * @param updated               Last update timestamp.
 * @param role                  The role of the interaction participant.
 * @param status                The status of the interaction.
 * @param outputs               List of content outputs.
 * @param usage                 Token usage details.
 * @param previousInteractionId ID of the previous interaction in the conversation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Interaction(
    String id,
    String model,
    String agent,
    String object,
    String created,
    String updated,
    Role role,
    Status status,
    List<Content> outputs,
    Usage usage,
    @JsonProperty("previous_interaction_id") String previousInteractionId
) {

    /**
     * Represents a single turn in an interaction.
     *
     * @param role    The role of the participant.
     * @param content The content of the turn (String or List&lt;Content&gt;).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Turn(
        Role role,
        Object content // String or List<Content>
    ) {}

    /**
     * Token usage details.
     *
     * @param totalInputTokens        Total input tokens.
     * @param inputTokensByModality   Input tokens broken down by modality.
     * @param totalCachedTokens       Total cached tokens.
     * @param cachedTokensByModality  Cached tokens broken down by modality.
     * @param totalOutputTokens       Total output tokens.
     * @param outputTokensByModality  Output tokens broken down by modality.
     * @param totalToolUseTokens      Total tool use tokens.
     * @param toolUseTokensByModality Tool use tokens broken down by modality.
     * @param totalReasoningTokens    Total reasoning tokens.
     * @param totalTokens             Total tokens.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Usage(
        @JsonProperty("total_input_tokens") Integer totalInputTokens,
        @JsonProperty("input_tokens_by_modality") List<ModalityTokens> inputTokensByModality,
        @JsonProperty("total_cached_tokens") Integer totalCachedTokens,
        @JsonProperty("cached_tokens_by_modality") List<ModalityTokens> cachedTokensByModality,
        @JsonProperty("total_output_tokens") Integer totalOutputTokens,
        @JsonProperty("output_tokens_by_modality") List<ModalityTokens> outputTokensByModality,
        @JsonProperty("total_tool_use_tokens") Integer totalToolUseTokens,
        @JsonProperty("tool_use_tokens_by_modality") List<ModalityTokens> toolUseTokensByModality,
        @JsonProperty("total_reasoning_tokens") Integer totalReasoningTokens,
        @JsonProperty("total_tokens") Integer totalTokens
    ) {}

    /**
     * Tokens broken down by modality.
     *
     * @param modality The modality.
     * @param tokens   The number of tokens.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ModalityTokens(
        Modality modality,
        Integer tokens
    ) {}

    /**
     * Interaction modalities.
     */
    public enum Modality {
        /** Text modality. */
        @JsonProperty("text") TEXT,
        /** Image modality. */
        @JsonProperty("image") IMAGE,
        /** Audio modality. */
        @JsonProperty("audio") AUDIO
    }

    /**
     * Interaction status.
     */
    public enum Status {
        /** Interaction in progress. */
        @JsonProperty("in_progress") IN_PROGRESS,
        /** Interaction requires usage action. */
        @JsonProperty("requires_action") REQUIRES_ACTION,
        /** Interaction completed successfully. */
        @JsonProperty("completed") COMPLETED,
        /** Interaction failed. */
        @JsonProperty("failed") FAILED,
        /** Interaction cancelled. */
        @JsonProperty("cancelled") CANCELLED
    }

    /**
     * Interaction participant role.
     */
    public enum Role {
        /** User role. */
        @JsonProperty("user") USER,
        /** Model role. */
        @JsonProperty("model") MODEL,
        /** Agent role. */
        @JsonProperty("agent") AGENT
    }
}
