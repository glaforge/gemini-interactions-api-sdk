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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.glaforge.gemini.schema.Schema;

import static io.github.glaforge.gemini.schema.GSchema.fromJson;

import java.util.List;
import java.util.Map;

/**
 * Request parameters for creating interactions.
 */
public class InteractionParams {

    /** Private constructor to prevent instantiation. */
    private InteractionParams() {}

    /**
     * Common interface for all interaction requests.
     */
    public sealed interface Request permits ModelInteractionParams, AgentInteractionParams {
        /**
         * Returns whether the response should be streamed.
         * @return whether the response should be streamed.
         */
        Boolean stream();
    }

    /**
     * Parameters for creating a model interaction.
     *
     * @param model                 The model to use (e.g., "gemini-2.5-flash").
     * @param input                 The input content (String, Content, List&lt;Content&gt;, List&lt;Turn&gt;).
     * @param generationConfig      Configuration for generation.
     * @param tools                 List of tools available for the interaction.
     * @param stream                Whether to stream the response.
     * @param store                 Whether to store the interaction.
     * @param background            Whether to run in background.
     * @param systemInstruction     System instruction for the model.
     * @param responseModalities    Requested response modalities.
     * @param responseFormat        Requested response format (JSON Schema).
     * @param responseMimeType      Requested response MIME type.
     * @param previousInteractionId ID of the previous interaction to continue.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ModelInteractionParams(
        String model,
        Object input, // String, Content, List<Content>, List<Turn>
        @JsonProperty("generation_config") Config.GenerationConfig generationConfig,
        List<Tool> tools,
        Boolean stream,
        Boolean store,
        Boolean background,
        @JsonProperty("system_instruction") String systemInstruction,
        @JsonProperty("response_modalities") List<Interaction.Modality> responseModalities,
        @JsonProperty("response_format") Object responseFormat, // JSON Schema object
        @JsonProperty("response_mime_type") String responseMimeType,
        @JsonProperty("previous_interaction_id") String previousInteractionId
    ) implements Request {
        /**
         * Returns a new builder for model interaction parameters.
         * @return a new builder for model interaction parameters.
         */
        public static Builder builder() { return new Builder(); }
        /** Builder for CreateModelInteractionParams. */
        public static class Builder {
            /** Creates a new Builder. */
            public Builder() {}
            private String model;
            private Object input;
            private Config.GenerationConfig generationConfig;
            private List<Tool> tools;
            private Boolean stream;
            private Boolean store;
            private Boolean background;
            private String systemInstruction;
            private List<Interaction.Modality> responseModalities;
            private Object responseFormat;
            private String responseMimeType;
            private String previousInteractionId;
            private List<Config.SpeechConfig> speechConfigs;
            private Config.ImageConfig imageConfig;

            /**
             * Sets the model.
             *
             * @param model The model to use.
             * @return This builder.
             */
            public Builder model(String model) { this.model = model; return this; }

            /**
             * Sets the input content as a string.
             *
             * @param text The input text.
             * @return This builder.
             */
            public Builder input(String text) { this.input = text; return this; }

            /**
             * Sets the input content as a list of Content objects.
             *
             * @param content The input content.
             * @return This builder.
             */
            public Builder input(Content... content) { this.input = List.of(content); return this; }

            /**
             * Sets the input content as a list of Content objects.
             *
             * @param content The input content.
             * @return This builder.
             */
            public Builder inputContents(List<Content> content) { this.input = content; return this; }

            /**
             * Sets the input content as a list of Turns (multi-turn history).
             *
             * @param turns The input turns.
             * @return This builder.
             */
            public Builder input(Interaction.Turn... turns) { this.input = List.of(turns); return this; }

            /**
             * Sets the input content as a list of Turns (multi-turn history).
             *
             * @param turns The input turns.
             * @return This builder.
             */
            public Builder inputTurns(List<Interaction.Turn> turns) { this.input = turns; return this; }

            /**
             * Sets the generation config.
             *
             * @param generationConfig The generation configuration.
             * @return This builder.
             */
            public Builder generationConfig(Config.GenerationConfig generationConfig) { this.generationConfig = generationConfig; return this; }

            /**
             * Sets the tools.
             *
             * @param tools The tools.
             * @return This builder.
             */
            public Builder tools(Tool... tools) { this.tools = List.of(tools); return this; }

            /**
             * Sets the tools.
             *
             * @param tools The list of tools.
             * @return This builder.
             */
            public Builder tools(List<Tool> tools) { this.tools = tools; return this; }

            /**
             * Sets the stream flag.
             *
             * @param stream Whether to stream the response.
             * @return This builder.
             */
            public Builder stream(Boolean stream) { this.stream = stream; return this; }

            /**
             * Sets the store flag.
             *
             * @param store Whether to store the interaction.
             * @return This builder.
             */
            public Builder store(Boolean store) { this.store = store; return this; }

            /**
             * Sets the background flag.
             *
             * @param background Whether to run in background.
             * @return This builder.
             */
            public Builder background(Boolean background) { this.background = background; return this; }

            /**
             * Sets the system instruction.
             *
             * @param systemInstruction The system instruction.
             * @return This builder.
             */
            public Builder systemInstruction(String systemInstruction) { this.systemInstruction = systemInstruction; return this; }

            /**
             * Sets the response modalities.
             *
             * @param responseModalities The response modalities.
             * @return This builder.
             */
            public Builder responseModalities(Interaction.Modality... responseModalities) { this.responseModalities = List.of(responseModalities); return this; }

            /**
             * Sets the response modalities.
             *
             * @param responseModalities The response modalities.
             * @return This builder.
             */
            public Builder responseModalities(List<Interaction.Modality> responseModalities) { this.responseModalities = responseModalities; return this; }

            /**
             * Sets the response format.
             *
             * @param responseFormat The response format.
             * @return This builder.
             */
            public Builder responseFormat(Object responseFormat) {
                if (responseFormat instanceof Schema schema) {
                    this.responseFormat = schema.toMap();
                } else {
                    this.responseFormat = responseFormat;
                }
                return this;
            }

            /**
             * Sets the response format using a Schema object.
             *
             * @param schema The schema to use as response format.
             * @return This builder.
             */
            public Builder responseFormat(Schema schema) {
                this.responseFormat = schema.toMap();
                return this;
            }

            /**
             * Sets the response MIME type.
             *
             * @param responseMimeType The response MIME type.
             * @return This builder.
             */
            public Builder responseMimeType(String responseMimeType) { this.responseMimeType = responseMimeType; return this; }

            /**
             * Sets the previous interaction ID.
             *
             * @param previousInteractionId The previous interaction ID.
             * @return This builder.
             */
            public Builder previousInteractionId(String previousInteractionId) { this.previousInteractionId = previousInteractionId; return this; }

            /**
             * Sets the image config.
             *
             * @param imageConfig The image configuration.
             * @return This builder.
             */
            public Builder imageConfig(Config.ImageConfig imageConfig) { this.imageConfig = imageConfig; return this; }

            /**
             * Sets the speech config.
             *
             * @param speechConfig The speech configuration.
             * @return This builder.
             */
            public Builder speechConfig(Config.SpeechConfig speechConfig) { this.speechConfigs = List.of(speechConfig); return this; }

            /**
             * Sets the speech configs.
             *
             * @param speechConfigs The speech configurations.
             * @return This builder.
             */
            public Builder speechConfigs(List<Config.SpeechConfig> speechConfigs) { this.speechConfigs = speechConfigs; return this; }

            /**
             * Builds the CreateModelInteractionParams.
             *
             * @return The CreateModelInteractionParams parameters.
             */
            public ModelInteractionParams build() {
                Config.GenerationConfig finalConfig = generationConfig;
                if (imageConfig != null || speechConfigs != null) {
                    if (finalConfig == null) {
                        finalConfig = new Config.GenerationConfig(null, null, null, null, null, null, null, null, speechConfigs, imageConfig);
                    } else {
                        finalConfig = new Config.GenerationConfig(
                            finalConfig.temperature(),
                            finalConfig.topP(),
                            finalConfig.seed(),
                            finalConfig.stopSequences(),
                            finalConfig.toolChoice(),
                            finalConfig.thinkingLevel(),
                            finalConfig.thinkingSummaries(),
                            finalConfig.maxOutputTokens(),
                            speechConfigs != null ? speechConfigs : finalConfig.speechConfig(),
                            imageConfig != null ? imageConfig : finalConfig.imageConfig()
                        );
                    }
                }
                return new ModelInteractionParams(model, input, finalConfig, tools, stream, store, background, systemInstruction, responseModalities, responseFormat, responseMimeType, previousInteractionId);
            }
        }
    }

    /**
     * Parameters for creating an agent interaction.
     *
     * @param agent                 The agent to use.
     * @param input                 The input content.
     * @param agentConfig           Configuration for the agent.
     * @param generationConfig      Configuration for generation.
     * @param tools                 List of tools available.
     * @param stream                Whether to stream the response.
     * @param store                 Whether to store the interaction.
     * @param background            Whether to run in background.
     * @param systemInstruction     System instruction.
     * @param responseModalities    Requested response modalities.
     * @param responseFormat        Requested response format.
     * @param responseMimeType      Requested response MIME type.
     * @param previousInteractionId ID of the previous interaction.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AgentInteractionParams(
        String agent,
        Object input,
        @JsonProperty("agent_config") Config.AgentConfig agentConfig,
        @JsonProperty("generation_config") Config.GenerationConfig generationConfig,
        List<Tool> tools,
        Boolean stream,
        Boolean store,
        Boolean background,
        @JsonProperty("system_instruction") String systemInstruction,
        @JsonProperty("response_modalities") List<Interaction.Modality> responseModalities,
        @JsonProperty("response_format") Object responseFormat,
        @JsonProperty("response_mime_type") String responseMimeType,
        @JsonProperty("previous_interaction_id") String previousInteractionId
    ) implements Request {
        /**
         * Returns a new builder for agent interaction parameters.
         * @return a new builder for agent interaction parameters.
         */
        public static Builder builder() { return new Builder(); }

        /** Builder for CreateAgentInteractionParams. */
        public static class Builder {
            /** Creates a new Builder. */
            public Builder() {}
            private String agent;
            private Object input;
            private Config.AgentConfig agentConfig;
            private Config.GenerationConfig generationConfig;
            private List<Tool> tools;
            private Boolean stream;
            private Boolean store;
            private Boolean background;
            private String systemInstruction;
            private List<Interaction.Modality> responseModalities;
            private Object responseFormat;
            private String responseMimeType;
            private String previousInteractionId;
            private List<Config.SpeechConfig> speechConfigs;
            private Config.ImageConfig imageConfig;

            /**
             * Sets the agent.
             *
             * @param agent The agent name.
             * @return This builder.
             */
            public Builder agent(String agent) { this.agent = agent; return this; }

            /**
             * Sets the input content as a string.
             *
             * @param text The input text.
             * @return This builder.
             */
            public Builder input(String text) { this.input = text; return this; }

            /**
             * Sets the input content as a list of Content objects.
             *
             * @param content The input content.
             * @return This builder.
             */
            public Builder input(Content... content) { this.input = List.of(content); return this; }

            /**
             * Sets the input content as a list of Content objects.
             *
             * @param content The input content.
             * @return This builder.
             */
            public Builder inputContents(List<Content> content) { this.input = content; return this; }

            /**
             * Sets the input content as a list of Turns (multi-turn history).
             *
             * @param turns The input turns.
             * @return This builder.
             */
            public Builder input(Interaction.Turn... turns) { this.input = List.of(turns); return this; }

             /**
             * Sets the input content as a list of Turns (multi-turn history).
             *
             * @param turns The input turns.
             * @return This builder.
             */
            public Builder inputTurns(List<Interaction.Turn> turns) { this.input = turns; return this; }

            /**
             * Sets the agent configuration.
             *
             * @param agentConfig The agent configuration.
             * @return This builder.
             */
            public Builder agentConfig(Config.AgentConfig agentConfig) { this.agentConfig = agentConfig; return this; }

            /**
             * Sets the generation config.
             *
             * @param generationConfig The generation configuration.
             * @return This builder.
             */
            public Builder generationConfig(Config.GenerationConfig generationConfig) { this.generationConfig = generationConfig; return this; }
            /**
             * Sets the tools.
             *
             * @param tools The tools.
             * @return This builder.
             */
            public Builder tools(Tool... tools) { this.tools = List.of(tools); return this; }

            /**
             * Sets the tools.
             *
             * @param tools The list of tools.
             * @return This builder.
             */
            public Builder tools(List<Tool> tools) { this.tools = tools; return this; }

            /**
             * Sets the stream flag.
             *
             * @param stream Whether to stream the response.
             * @return This builder.
             */
            public Builder stream(Boolean stream) { this.stream = stream; return this; }

            /**
             * Sets the store flag.
             *
             * @param store Whether to store the interaction.
             * @return This builder.
             */
            public Builder store(Boolean store) { this.store = store; return this; }

            /**
             * Sets the background flag.
             *
             * @param background Whether to run in background.
             * @return This builder.
             */
            public Builder background(Boolean background) { this.background = background; return this; }

            /**
             * Sets the system instruction.
             *
             * @param systemInstruction The system instruction.
             * @return This builder.
             */
            public Builder systemInstruction(String systemInstruction) { this.systemInstruction = systemInstruction; return this; }

            /**
             * Sets the response modalities.
             *
             * @param responseModalities The response modalities.
             * @return This builder.
             */
            public Builder responseModalities(Interaction.Modality... responseModalities) { this.responseModalities = List.of(responseModalities); return this; }

            /**
             * Sets the response modalities.
             *
             * @param responseModalities The response modalities.
             * @return This builder.
             */
            public Builder responseModalities(List<Interaction.Modality> responseModalities) { this.responseModalities = responseModalities; return this; }

            /**
             * Sets the response format from a map.
             *
             * @param responseFormat The response format map.
             * @return This builder.
             */
            public Builder responseFormat(Map<String, Object> responseFormat) {
                this.responseFormat = responseFormat;
                return this;
            }

            /**
             * Sets the response format from a schema.
             *
             * @param responseFormat The response format schema.
             * @return This builder.
             */
            public Builder responseFormat(Schema responseFormat) {
                this.responseFormat = responseFormat.toMap();
                return this;
            }

            /**
             * Sets the response format from a JSON string.
             *
             * @param responseFormat The response format JSON string.
             * @return This builder.
             */
            public Builder responseFormat(String responseFormat) {
                this.responseFormat = fromJson(responseFormat).toMap();
                return this;
            }

            /**
             * Sets the response MIME type.
             *
             * @param responseMimeType The response MIME type.
             * @return This builder.
             */
            public Builder responseMimeType(String responseMimeType) { this.responseMimeType = responseMimeType; return this; }

            /**
             * Sets the previous interaction ID.
             *
             * @param previousInteractionId The previous interaction ID.
             * @return This builder.
             */
            public Builder previousInteractionId(String previousInteractionId) { this.previousInteractionId = previousInteractionId; return this; }

            /**
             * Sets the image config.
             *
             * @param imageConfig The image configuration.
             * @return This builder.
             */
            public Builder imageConfig(Config.ImageConfig imageConfig) { this.imageConfig = imageConfig; return this; }

            /**
             * Sets the speech config.
             *
             * @param speechConfig The speech configuration.
             * @return This builder.
             */
            public Builder speechConfig(Config.SpeechConfig speechConfig) { this.speechConfigs = List.of(speechConfig); return this; }

            /**
             * Sets the speech configs.
             *
             * @param speechConfigs The speech configurations.
             * @return This builder.
             */
            public Builder speechConfigs(List<Config.SpeechConfig> speechConfigs) { this.speechConfigs = speechConfigs; return this; }

            /**
             * Builds the CreateAgentInteractionParams.
             *
             * @return The CreateAgentInteractionParams parameters.
             */
            public AgentInteractionParams build() {
                Config.GenerationConfig finalConfig = generationConfig;
                if (imageConfig != null || speechConfigs != null) {
                    if (finalConfig == null) {
                        finalConfig = new Config.GenerationConfig(null, null, null, null, null, null, null, null, speechConfigs, imageConfig);
                    } else {
                        finalConfig = new Config.GenerationConfig(
                            finalConfig.temperature(),
                            finalConfig.topP(),
                            finalConfig.seed(),
                            finalConfig.stopSequences(),
                            finalConfig.toolChoice(),
                            finalConfig.thinkingLevel(),
                            finalConfig.thinkingSummaries(),
                            finalConfig.maxOutputTokens(),
                            speechConfigs != null ? speechConfigs : finalConfig.speechConfig(),
                            imageConfig != null ? imageConfig : finalConfig.imageConfig()
                        );
                    }
                }
                return new AgentInteractionParams(agent, input, agentConfig, finalConfig, tools, stream, store, background, systemInstruction, responseModalities, responseFormat, responseMimeType, previousInteractionId);
            }
        }
    }
}
