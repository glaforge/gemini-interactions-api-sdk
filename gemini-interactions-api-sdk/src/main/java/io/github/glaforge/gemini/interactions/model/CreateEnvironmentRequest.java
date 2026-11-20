/*
 * Copyright 2026 Google LLC
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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Request payload for creating an execution environment.
 *
 * @param fromEnvironment   Optional. The source environment to copy/fork from.
 * @param network           Network configuration for the environment.
 * @param sources           Sources to be mounted into the environment.
 * @param mode              Optional. The compute lifecycle mode for this environment ({@code on_demand} or {@code self_hosted}).
 * @param customConfig      Optional. Arbitrary customer-defined JSON configuration passed through to the self-hosted environment poller.
 * @param systemInstruction Optional. System instruction applied to interactions running in this environment (supported for {@code self_hosted} environments).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateEnvironmentRequest(
    @JsonProperty("from_environment") String fromEnvironment,
    NetworkConfiguration network,
    List<Source> sources,
    EnvironmentMode mode,
    @JsonProperty("custom_config") Map<String, Object> customConfig,
    @JsonProperty("system_instruction") String systemInstruction
) {
    /**
     * Backward-compatible constructor without mode, customConfig, and systemInstruction.
     *
     * @param fromEnvironment Optional. The source environment to copy/fork from.
     * @param network         Network configuration for the environment.
     * @param sources         Sources to be mounted into the environment.
     */
    public CreateEnvironmentRequest(String fromEnvironment, NetworkConfiguration network, List<Source> sources) {
        this(fromEnvironment, network, sources, null, null, null);
    }

    /**
     * Creates a CreateEnvironmentRequest with network and sources.
     *
     * @param network Network configuration for the environment.
     * @param sources Sources to be mounted into the environment.
     */
    public CreateEnvironmentRequest(NetworkConfiguration network, List<Source> sources) {
        this(null, network, sources, null, null, null);
    }

    /**
     * Returns a new builder for CreateEnvironmentRequest.
     * @return a new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder for {@link CreateEnvironmentRequest}. */
    public static class Builder {
        /** Creates a new Builder. */
        public Builder() {}

        private String fromEnvironment;
        private NetworkConfiguration network;
        private List<Source> sources;
        private EnvironmentMode mode;
        private Map<String, Object> customConfig;
        private String systemInstruction;

        /**
         * Sets the source environment to copy/fork from.
         * @param fromEnvironment Source environment ID or format environments/{environment_id}.
         * @return This builder.
         */
        public Builder fromEnvironment(String fromEnvironment) { this.fromEnvironment = fromEnvironment; return this; }

        /**
         * Sets the network configuration.
         * @param network Network configuration.
         * @return This builder.
         */
        public Builder network(NetworkConfiguration network) { this.network = network; return this; }

        /**
         * Sets the network configuration using an EnvironmentNetworkEgressAllowlist object.
         * @param config EnvironmentNetworkEgressAllowlist object.
         * @return This builder.
         */
        public Builder network(EnvironmentNetworkEgressAllowlist config) { this.network = config != null ? NetworkConfiguration.of(config) : null; return this; }

        /**
         * Sets the network configuration preset string.
         * @param preset Network preset string.
         * @return This builder.
         */
        public Builder network(String preset) { this.network = preset != null ? NetworkConfiguration.of(preset) : null; return this; }

        /**
         * Sets the sources to mount.
         * @param sources Mounted sources list.
         * @return This builder.
         */
        public Builder sources(List<Source> sources) { this.sources = sources; return this; }

        /**
         * Sets the compute lifecycle mode for this environment.
         *
         * @param mode The environment mode ({@link EnvironmentMode#ON_DEMAND} or {@link EnvironmentMode#SELF_HOSTED}).
         * @return This builder.
         */
        public Builder mode(EnvironmentMode mode) { this.mode = mode; return this; }

        /**
         * Sets the arbitrary customer-defined JSON configuration passed through to the self-hosted environment poller.
         *
         * @param customConfig The custom configuration map.
         * @return This builder.
         */
        public Builder customConfig(Map<String, ?> customConfig) {
            this.customConfig = customConfig != null ? new HashMap<>(customConfig) : null;
            return this;
        }

        /**
         * Adds a single entry to the customer-defined custom configuration map.
         *
         * @param key   Configuration property name.
         * @param value Configuration property value.
         * @return This builder.
         */
        public Builder customConfig(String key, Object value) {
            if (this.customConfig == null) {
                this.customConfig = new HashMap<>();
            }
            this.customConfig.put(key, value);
            return this;
        }

        /**
         * Sets the system instruction applied to interactions running in this environment
         * (supported for {@link EnvironmentMode#SELF_HOSTED} environments).
         *
         * @param systemInstruction The system instruction text.
         * @return This builder.
         */
        public Builder systemInstruction(String systemInstruction) { this.systemInstruction = systemInstruction; return this; }

        /**
         * Builds the CreateEnvironmentRequest.
         * @return The CreateEnvironmentRequest.
         */
        public CreateEnvironmentRequest build() {
            return new CreateEnvironmentRequest(fromEnvironment, network, sources, mode, customConfig, systemInstruction);
        }
    }
}
