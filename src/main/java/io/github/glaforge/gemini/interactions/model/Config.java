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

/**
 * Configuration records for interactions.
 */
public class Config {

    /** Private constructor to prevent instantiation. */
    private Config() {}

    /**
     * Configuration options for model generation.
     *
     * @param temperature       Controls randomness in generation.
     * @param topP             The maximum cumulative probability of tokens to consider when sampling.
     * @param seed             Seed for random number generation.
     * @param stopSequences    List of strings that stop generation.
     * @param toolChoice       Configuration for tool use.
     * @param thinkingLevel    Level of thinking to use for the model.
     * @param thinkingSummaries Configuration for thinking summaries.
     * @param maxOutputTokens  The maximum number of tokens to include in a candidate.
     * @param speechConfig     Configuration for speech generation.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GenerationConfig(
        Double temperature,
        @JsonProperty("top_p") Double topP,
        Integer seed,
        @JsonProperty("stop_sequences") List<String> stopSequences,
        @JsonProperty("tool_choice") Tool.ToolChoiceConfig toolChoice,
        @JsonProperty("thinking_level") ThinkingLevel thinkingLevel,
        @JsonProperty("thinking_summaries") ThinkingSummaries thinkingSummaries,
        @JsonProperty("max_output_tokens") Integer maxOutputTokens,
        @JsonProperty("speech_config") List<SpeechConfig> speechConfig
    ) {}

    /**
     * Level of thinking to use for the model.
     */
    public enum ThinkingLevel {
        /** Low thinking level. */
        @JsonProperty("low") LOW,
        /** High thinking level. */
        @JsonProperty("high") HIGH
    }

    /**
     * Configuration for thinking summaries.
     */
    public enum ThinkingSummaries {
        /** Auto thinking summaries. */
        @JsonProperty("auto") AUTO,
        /** No thinking summaries. */
        @JsonProperty("none") NONE
    }

    /**
     * Configuration for speech generation.
     *
     * @param voice    The voice to use.
     * @param language The language of the speech.
     * @param speaker  The speaker identity.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SpeechConfig(
        String voice,
        String language,
        String speaker
    ) {}

    /**
     * Sealed interface for agent configurations.
     */
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = DynamicAgentConfig.class, name = "dynamic"),
        @JsonSubTypes.Type(value = DeepResearchAgentConfig.class, name = "deep-research") // mapping key from spec
    })
    public sealed interface AgentConfig permits DynamicAgentConfig, DeepResearchAgentConfig {
        /**
         * Returns the type of the agent.
         *
         * @return The agent type.
         */
        String type();
    }

    /**
     * Configuration for dynamic agents.
     *
     * @param type The type of agent (must be "dynamic").
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DynamicAgentConfig(
        String type
        // additionalProperties
    ) implements AgentConfig {
        /** Creates a new DynamicAgentConfig with default type "dynamic". */
        public DynamicAgentConfig() {
            this("dynamic");
        }
    }

    /**
     * Configuration for deep research agents.
     *
     * @param type              The type of agent (must be "deep-research").
     * @param thinkingSummaries Configuration for thinking summaries.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DeepResearchAgentConfig(
        String type,
        @JsonProperty("thinking_summaries") ThinkingSummaries thinkingSummaries
    ) implements AgentConfig {
        /** Creates a new DeepResearchAgentConfig with default type and no summaries. */
        public DeepResearchAgentConfig() {
            this("deep-research", null); // Example constructor
        }
        /**
         * Creates a new DeepResearchAgentConfig with default type.
         *
         * @param thinkingSummaries The thinking summaries configuration.
         */
        public DeepResearchAgentConfig(ThinkingSummaries thinkingSummaries) {
            this("deep-research", thinkingSummaries);
        }
    }
}
