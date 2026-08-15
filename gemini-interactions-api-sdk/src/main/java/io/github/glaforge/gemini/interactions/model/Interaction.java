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
import java.util.List;
import java.time.Instant;

/**
 * Represents the Interaction resource.
 *
 * @param id                    The unique identifier for the interaction.
 * @param model                 The model used for the interaction.
 * @param agent                 The agent used for the interaction.
 * @param agentConfig           The agent configuration.
 * @param created               Creation timestamp.
 * @param updated               Last update timestamp.
 * @param status                The status of the interaction.
 * @param steps                 List of steps.
 * @param usage                 Token usage details.
 * @param previousInteractionId ID of the previous interaction in the conversation.
 * @param environmentId         ID of the environment.
 * @param cachedContent         URI of the cached content used.
 * @param safetySettings        The safety settings to apply.
 * @param errors                Diagnostic faults or platform errors recorded on the interaction.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Interaction(
    String id,
    String model,
    String agent,
    @JsonProperty("agent_config") Config.AgentConfig agentConfig,
    Instant created,
    Instant updated,
    Status status,
    List<Step> steps,
    Usage usage,
    @JsonProperty("previous_interaction_id") String previousInteractionId,
    @JsonProperty("environment_id") String environmentId,
    @JsonProperty("cached_content") String cachedContent,
    @JsonProperty("safety_settings") List<SafetySetting> safetySettings,
    List<Events.Error> errors
) {
    /**
     * Backward-compatible constructor without errors.
     *
     * @param id                    The unique identifier for the interaction.
     * @param model                 The model used for the interaction.
     * @param agent                 The agent used for the interaction.
     * @param agentConfig           The agent configuration.
     * @param created               Creation timestamp.
     * @param updated               Last update timestamp.
     * @param status                The status of the interaction.
     * @param steps                 List of steps.
     * @param usage                 Token usage details.
     * @param previousInteractionId ID of the previous interaction in the conversation.
     * @param environmentId         ID of the environment.
     * @param cachedContent         URI of the cached content used.
     * @param safetySettings        The safety settings to apply.
     */
    public Interaction(
        String id,
        String model,
        String agent,
        Config.AgentConfig agentConfig,
        Instant created,
        Instant updated,
        Status status,
        List<Step> steps,
        Usage usage,
        String previousInteractionId,
        String environmentId,
        String cachedContent,
        List<SafetySetting> safetySettings
    ) {
        this(id, model, agent, agentConfig, created, updated, status, steps, usage, previousInteractionId, environmentId, cachedContent, safetySettings, null);
    }

    /**
     * Extracts the concatenated text from the last consecutive model output steps.
     *
     * @return The output text, or null if not found.
     */
    public String outputText() {
        if (steps == null || steps.isEmpty()) {
            return null;
        }

        StringBuilder textBuilder = new StringBuilder();
        boolean done = false;

        for (int i = steps.size() - 1; i >= 0; i--) {
            Step step = steps.get(i);
            if (done) break;

            if (step instanceof Step.ModelOutputStep modelOutputStep) {
                if (modelOutputStep.content() != null) {
                    for (int j = modelOutputStep.content().size() - 1; j >= 0; j--) {
                        Content content = modelOutputStep.content().get(j);
                        if (content instanceof Content.TextContent textContent) {
                            textBuilder.insert(0, textContent.text());
                        } else {
                            done = true;
                            break;
                        }
                    }
                }
            } else {
                break;
            }
        }

        String result = textBuilder.toString();
        return result.isEmpty() ? null : result;
    }

    /**
     * Extracts the last image generated by the model in response to the current request.
     *
     * @return The image content, or null if not found.
     */
    public Content.ImageContent outputImage() {
        if (steps == null || steps.isEmpty()) return null;
        for (int i = steps.size() - 1; i >= 0; i--) {
            Step step = steps.get(i);
            if (step instanceof Step.UserInputStep) break;
            if (step instanceof Step.ModelOutputStep modelOutputStep && modelOutputStep.content() != null) {
                for (int j = modelOutputStep.content().size() - 1; j >= 0; j--) {
                    Content content = modelOutputStep.content().get(j);
                    if (content instanceof Content.ImageContent imageContent) {
                        return imageContent;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Extracts the last audio generated by the model in response to the current request.
     *
     * @return The audio content, or null if not found.
     */
    public Content.AudioContent outputAudio() {
        if (steps == null || steps.isEmpty()) return null;
        for (int i = steps.size() - 1; i >= 0; i--) {
            Step step = steps.get(i);
            if (step instanceof Step.UserInputStep) break;
            if (step instanceof Step.ModelOutputStep modelOutputStep && modelOutputStep.content() != null) {
                for (int j = modelOutputStep.content().size() - 1; j >= 0; j--) {
                    Content content = modelOutputStep.content().get(j);
                    if (content instanceof Content.AudioContent audioContent) {
                        return audioContent;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Extracts the last video generated by the model in response to the current request.
     *
     * @return The video content, or null if not found.
     */
    public Content.VideoContent outputVideo() {
        if (steps == null || steps.isEmpty()) return null;
        for (int i = steps.size() - 1; i >= 0; i--) {
            Step step = steps.get(i);
            if (step instanceof Step.UserInputStep) break;
            if (step instanceof Step.ModelOutputStep modelOutputStep && modelOutputStep.content() != null) {
                for (int j = modelOutputStep.content().size() - 1; j >= 0; j--) {
                    Content content = modelOutputStep.content().get(j);
                    if (content instanceof Content.VideoContent videoContent) {
                        return videoContent;
                    }
                }
            }
        }
        return null;
    }

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
     * @param totalThoughtTokens      Total thought (reasoning) tokens.
     * @param totalTokens             Total tokens.
     * @param groundingToolCount      Grounding tool counts.
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
        @JsonProperty("total_thought_tokens") Integer totalThoughtTokens,
        @JsonProperty("total_tokens") Integer totalTokens,
        @JsonProperty("grounding_tool_count") List<GroundingToolCount> groundingToolCount
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
     * Grounding tool count.
     *
     * @param count The number of grounding tool counts.
     * @param type  The grounding tool type associated with the count.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GroundingToolCount(
        Integer count,
        String type
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
        @JsonProperty("audio") AUDIO,
        /** Video modality. */
        @JsonProperty("video") VIDEO
    }

    /**
     * Interaction status.
     */
    public enum Status {
        /** Interaction queued for execution. */
        @JsonProperty("queued") QUEUED,
        /** Interaction in progress. */
        @JsonProperty("in_progress") IN_PROGRESS,
        /** Interaction requires usage action. */
        @JsonProperty("requires_action") REQUIRES_ACTION,
        /** Interaction completed successfully. */
        @JsonProperty("completed") COMPLETED,
        /** Interaction failed. */
        @JsonProperty("failed") FAILED,
        /** Interaction cancelled. */
        @JsonProperty("cancelled") CANCELLED,
        /** Interaction incomplete. */
        @JsonProperty("incomplete") INCOMPLETE,
        /** Interaction budget exceeded. */
        @JsonProperty("budget_exceeded") BUDGET_EXCEEDED;
        
        /**
         * Checks whether the status represents a finished state.
         *
         * @return true if the status is COMPLETED, FAILED, or CANCELLED.
         */
        public boolean isFinished() {
            return this == COMPLETED || this == FAILED || this == CANCELLED;
        }
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

    /**
     * Returns a new builder for Interaction.
     * @return a new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for Interaction.
     */
    public static class Builder {
        private String id;
        private String model;
        private String agent;
        private Config.AgentConfig agentConfig;
        private Instant created;
        private Instant updated;
        private Status status;
        private List<Step> steps;
        private Usage usage;
        private String previousInteractionId;
        private String environmentId;
        private String cachedContent;
        private List<SafetySetting> safetySettings;
        private List<Events.Error> errors;

        /** Creates a new Builder. */
        public Builder() {}

        /**
         * Sets the ID.
         * @param id the id.
         * @return this builder.
         */
        public Builder id(String id) { this.id = id; return this; }

        /**
         * Sets the model.
         * @param model the model.
         * @return this builder.
         */
        public Builder model(String model) { this.model = model; return this; }

        /**
         * Sets the agent.
         * @param agent the agent.
         * @return this builder.
         */
        public Builder agent(String agent) { this.agent = agent; return this; }

        /**
         * Sets the agent config.
         * @param agentConfig the agent config.
         * @return this builder.
         */
        public Builder agentConfig(Config.AgentConfig agentConfig) { this.agentConfig = agentConfig; return this; }

        /**
         * Sets the created time.
         * @param created the created time.
         * @return this builder.
         */
        public Builder created(Instant created) { this.created = created; return this; }

        /**
         * Sets the updated time.
         * @param updated the updated time.
         * @return this builder.
         */
        public Builder updated(Instant updated) { this.updated = updated; return this; }

        /**
         * Sets the status.
         * @param status the status.
         * @return this builder.
         */
        public Builder status(Status status) { this.status = status; return this; }

        /**
         * Sets the steps.
         * @param steps the steps.
         * @return this builder.
         */
        public Builder steps(List<Step> steps) { this.steps = steps; return this; }

        /**
         * Sets the usage.
         * @param usage the usage.
         * @return this builder.
         */
        public Builder usage(Usage usage) { this.usage = usage; return this; }

        /**
         * Sets the previous interaction ID.
         * @param previousInteractionId the ID.
         * @return this builder.
         */
        public Builder previousInteractionId(String previousInteractionId) { this.previousInteractionId = previousInteractionId; return this; }

        /**
         * Sets the environment ID.
         * @param environmentId the environment ID.
         * @return this builder.
         */
        public Builder environmentId(String environmentId) { this.environmentId = environmentId; return this; }

        /**
         * Sets the cached content.
         * @param cachedContent the cached content.
         * @return this builder.
         */
        public Builder cachedContent(String cachedContent) { this.cachedContent = cachedContent; return this; }

        /**
         * Sets the safety settings.
         * @param safetySettings the safety settings.
         * @return this builder.
         */
        public Builder safetySettings(List<SafetySetting> safetySettings) { this.safetySettings = safetySettings; return this; }

        /**
         * Sets the errors.
         * @param errors the list of diagnostic errors.
         * @return this builder.
         */
        public Builder errors(List<Events.Error> errors) { this.errors = errors; return this; }

        /**
         * Builds the Interaction.
         * @return the new Interaction.
         */
        public Interaction build() {
            return new Interaction(id, model, agent, agentConfig, created, updated, status, steps, usage, previousInteractionId, environmentId, cachedContent, safetySettings, errors);
        }
    }
}
