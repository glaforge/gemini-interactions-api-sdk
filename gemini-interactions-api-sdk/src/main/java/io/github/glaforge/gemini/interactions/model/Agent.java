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
import tools.jackson.databind.annotation.JsonDeserialize;
import io.github.glaforge.gemini.interactions.model.deserializer.BaseEnvironmentDeserializer;
import java.util.List;

/**
 * An agent definition for the custom Agents API.
 *
 * @param id                The unique identifier for the agent.
 * @param description       Agent description.
 * @param baseAgent         The base agent to extend (e.g. standard agents like "deep-research-preview-04-2026").
 * @param baseEnvironment   The environment configuration (either EnvironmentConfig or a string like "default").
 * @param systemInstruction System instruction for the agent.
 * @param tools             The tools available to the agent.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Agent(
    String id,
    String description,
    @JsonProperty("base_agent") String baseAgent,
    @JsonProperty("base_environment") @JsonDeserialize(using = BaseEnvironmentDeserializer.class) Object baseEnvironment,
    @JsonProperty("system_instruction") String systemInstruction,
    List<AgentTool> tools
) {
    /**
     * Creates a new Builder for Agent.
     *
     * @return Agent builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link Agent}.
     */
    public static class Builder {
        private String id;
        private String description;
        private String baseAgent;
        private Object baseEnvironment;
        private String systemInstruction;
        private List<AgentTool> tools;

        /** Creates a new Builder. */
        public Builder() {}

        /**
         * Sets agent ID.
         *
         * @param id The agent ID.
         * @return This builder.
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * Sets description.
         *
         * @param description The description.
         * @return This builder.
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets base agent name.
         *
         * @param baseAgent The base agent.
         * @return This builder.
         */
        public Builder baseAgent(String baseAgent) {
            this.baseAgent = baseAgent;
            return this;
        }

        /**
         * Sets the predefined base environment name.
         *
         * @param baseEnvironment Predefined base environment name (e.g. "default").
         * @return This builder.
         */
        public Builder baseEnvironment(String baseEnvironment) {
            this.baseEnvironment = baseEnvironment;
            return this;
        }

        /**
         * Sets custom base environment configuration.
         *
         * @param baseEnvironment Custom base environment configuration.
         * @return This builder.
         */
        public Builder baseEnvironment(EnvironmentConfig baseEnvironment) {
            this.baseEnvironment = baseEnvironment;
            return this;
        }

        /**
         * Sets system instruction.
         *
         * @param systemInstruction System instruction.
         * @return This builder.
         */
        public Builder systemInstruction(String systemInstruction) {
            this.systemInstruction = systemInstruction;
            return this;
        }

        /**
         * Sets the tools list.
         *
         * @param tools The tools list.
         * @return This builder.
         */
        public Builder tools(List<AgentTool> tools) {
            this.tools = tools;
            return this;
        }

        /**
         * Sets the tools array.
         *
         * @param tools The tools.
         * @return This builder.
         */
        public Builder tools(AgentTool... tools) {
            this.tools = List.of(tools);
            return this;
        }

        /**
         * Builds the Agent.
         *
         * @return The Agent.
         */
        public Agent build() {
            return new Agent(id, description, baseAgent, baseEnvironment, systemInstruction, tools);
        }
    }
}
