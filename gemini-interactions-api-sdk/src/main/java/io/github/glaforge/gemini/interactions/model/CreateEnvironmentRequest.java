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
import tools.jackson.databind.annotation.JsonDeserialize;
import io.github.glaforge.gemini.interactions.model.deserializer.NetworkConfigDeserializer;

import java.util.List;

/**
 * Request payload for creating an execution environment.
 *
 * @param network Network configuration for the environment (EnvironmentNetworkEgressAllowlist or "disabled").
 * @param sources Sources to be mounted into the environment.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateEnvironmentRequest(
    @JsonDeserialize(using = NetworkConfigDeserializer.class) Object network,
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

        private Object network;
        private List<Source> sources;

        /**
         * Sets the network configuration object.
         * @param network Network configuration.
         * @return This builder.
         */
        public Builder network(Object network) { this.network = network; return this; }

        /**
         * Sets the network configuration to an egress allowlist.
         * @param network Network egress allowlist.
         * @return This builder.
         */
        public Builder network(EnvironmentNetworkEgressAllowlist network) { this.network = network; return this; }

        /**
         * Sets the network configuration mode (e.g. "disabled").
         * @param network Network mode string.
         * @return This builder.
         */
        public Builder network(String network) { this.network = network; return this; }

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
