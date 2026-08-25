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

import java.util.List;

/**
 * Request payload for creating an execution environment.
 *
 * @param network Network configuration for the environment.
 * @param sources Sources to be mounted into the environment.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateEnvironmentRequest(
    NetworkConfiguration network,
    List<Source> sources
) {
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

        private NetworkConfiguration network;
        private List<Source> sources;

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
         * Builds the CreateEnvironmentRequest.
         * @return The CreateEnvironmentRequest.
         */
        public CreateEnvironmentRequest build() {
            return new CreateEnvironmentRequest(network, sources);
        }
    }
}
