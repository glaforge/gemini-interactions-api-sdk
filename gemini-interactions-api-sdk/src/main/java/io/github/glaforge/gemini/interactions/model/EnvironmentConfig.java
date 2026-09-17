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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for a custom environment.
 *
 * @param type          The type (must be "remote").
 * @param environmentId Optional. The environment ID for the interaction. If specified, the request will
 *                      update the existing environment instead of creating a new one.
 * @param network       Network configuration.
 * @param sources       Sources to be mounted into the environment.
 * @param env           Environment variables to inject into the environment sandbox (literal strings or credential references).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record EnvironmentConfig(
    String type,
    @JsonProperty("environment_id") String environmentId,
    NetworkConfiguration network,
    List<Source> sources,
    Map<String, Object> env
) {
    /**
     * Backward-compatible constructor without env.
     *
     * @param type          The type.
     * @param environmentId Optional environment ID.
     * @param network       Network configuration.
     * @param sources       Mounted sources list.
     */
    public EnvironmentConfig(String type, String environmentId, NetworkConfiguration network, List<Source> sources) {
        this(type, environmentId, network, sources, null);
    }
    /**
     * Creates a custom remote EnvironmentConfig without an existing environment ID.
     *
     * @param type    The environment type.
     * @param network Network configuration.
     * @param sources Mounted sources list.
     */
    public EnvironmentConfig(String type, NetworkConfiguration network, List<Source> sources) {
        this(type, null, network, sources);
    }

    /**
     * Creates a custom remote EnvironmentConfig.
     *
     * @param network Network configuration.
     * @param sources Mounted sources list.
     */
    public EnvironmentConfig(NetworkConfiguration network, List<Source> sources) {
        this("remote", null, network, sources);
    }

    /**
     * Creates a custom remote EnvironmentConfig from an EnvironmentNetworkEgressAllowlist.
     *
     * @param config  EnvironmentNetworkEgressAllowlist.
     * @param sources Mounted sources list.
     */
    public EnvironmentConfig(EnvironmentNetworkEgressAllowlist config, List<Source> sources) {
        this("remote", null, config != null ? NetworkConfiguration.of(config) : null, sources);
    }

    /**
     * Creates a custom remote EnvironmentConfig from a preset string.
     *
     * @param preset  Network preset string.
     * @param sources Mounted sources list.
     */
    public EnvironmentConfig(String preset, List<Source> sources) {
        this("remote", null, preset != null ? NetworkConfiguration.of(preset) : null, sources);
    }

    /**
     * Creates an EnvironmentConfig targeting an existing environment to update network configuration or refresh credentials.
     *
     * @param environmentId The existing environment ID.
     * @param network       Updated network configuration.
     * @return EnvironmentConfig configured to update the existing environment.
     */
    public static EnvironmentConfig forExisting(String environmentId, NetworkConfiguration network) {
        return new EnvironmentConfig("remote", environmentId, network, null);
    }

    /**
     * Creates an EnvironmentConfig targeting an existing environment with an allowlist.
     *
     * @param environmentId The existing environment ID.
     * @param allowlist     Updated egress allowlist.
     * @return EnvironmentConfig configured to update the existing environment.
     */
    public static EnvironmentConfig forExisting(String environmentId, EnvironmentNetworkEgressAllowlist allowlist) {
        return forExisting(environmentId, allowlist != null ? NetworkConfiguration.of(allowlist) : null);
    }

    /**
     * Creates an EnvironmentConfig targeting an existing environment with a network preset (e.g. "disabled").
     *
     * @param environmentId The existing environment ID.
     * @param networkPreset Updated network preset string.
     * @return EnvironmentConfig configured to update the existing environment.
     */
    public static EnvironmentConfig forExisting(String environmentId, String networkPreset) {
        return forExisting(environmentId, networkPreset != null ? NetworkConfiguration.of(networkPreset) : null);
    }

    /**
     * Returns a new builder for EnvironmentConfig.
     *
     * @return a new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder for {@link EnvironmentConfig}. */
    public static class Builder {
        private String type = "remote";
        private String environmentId;
        private NetworkConfiguration network;
        private List<Source> sources;
        private Map<String, Object> env;

        /** Creates a new Builder. */
        public Builder() {}

        /**
         * Sets the type.
         *
         * @param type Environment type (typically "remote").
         * @return This builder.
         */
        public Builder type(String type) { this.type = type; return this; }

        /**
         * Sets the environment ID for updating an existing environment.
         *
         * @param environmentId Environment ID.
         * @return This builder.
         */
        public Builder environmentId(String environmentId) { this.environmentId = environmentId; return this; }

        /**
         * Sets the network configuration.
         *
         * @param network Network configuration.
         * @return This builder.
         */
        public Builder network(NetworkConfiguration network) { this.network = network; return this; }

        /**
         * Sets the network configuration using an allowlist.
         *
         * @param config Egress allowlist.
         * @return This builder.
         */
        public Builder network(EnvironmentNetworkEgressAllowlist config) {
            this.network = config != null ? NetworkConfiguration.of(config) : null;
            return this;
        }

        /**
         * Sets the network configuration preset string (e.g. "disabled").
         *
         * @param preset Network preset.
         * @return This builder.
         */
        public Builder network(String preset) {
            this.network = preset != null ? NetworkConfiguration.of(preset) : null;
            return this;
        }

        /**
         * Sets the mounted sources list.
         *
         * @param sources Sources to mount.
         * @return This builder.
         */
        public Builder sources(List<Source> sources) { this.sources = sources; return this; }

        /**
         * Sets the mounted sources.
         *
         * @param sources Sources to mount.
         * @return This builder.
         */
        public Builder sources(Source... sources) { this.sources = List.of(sources); return this; }

        /**
         * Sets the entire environment variable map.
         *
         * @param env Map of environment variables (strings or credential references).
         * @return This builder.
         */
        public Builder env(Map<String, ?> env) {
            if (env == null) {
                this.env = null;
            } else {
                this.env = new HashMap<>(env);
            }
            return this;
        }

        /**
         * Adds an environment variable with a literal string value.
         *
         * @param key   The variable name.
         * @param value The variable value.
         * @return This builder.
         */
        public Builder env(String key, String value) {
            if (this.env == null) {
                this.env = new HashMap<>();
            }
            this.env.put(key, value);
            return this;
        }

        /**
         * Adds an environment variable referencing a server-managed credential ID.
         *
         * @param key          The variable name.
         * @param credentialId The referenced credential ID.
         * @return This builder.
         */
        public Builder envCredential(String key, String credentialId) {
            if (this.env == null) {
                this.env = new HashMap<>();
            }
            this.env.put(key, CredentialReference.of(credentialId));
            return this;
        }

        /**
         * Builds the EnvironmentConfig.
         *
         * @return The EnvironmentConfig.
         */
        public EnvironmentConfig build() {
            return new EnvironmentConfig(type, environmentId, network, sources, env);
        }
    }
}
