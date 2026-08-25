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
import tools.jackson.databind.annotation.JsonSerialize;
import io.github.glaforge.gemini.interactions.model.deserializer.NetworkConfigurationDeserializer;
import io.github.glaforge.gemini.interactions.model.deserializer.NetworkConfigurationSerializer;

/**
 * Type-safe network configuration for an environment, representing either a preset
 * name (e.g. "default", "allow_all") or a detailed {@link EnvironmentNetworkEgressAllowlist}.
 *
 * @param preset Predefined network configuration name (or null if custom config).
 * @param config Detailed network egress allowlist (or null if preset).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(using = NetworkConfigurationSerializer.class)
@JsonDeserialize(using = NetworkConfigurationDeserializer.class)
public record NetworkConfiguration(
    String preset,
    EnvironmentNetworkEgressAllowlist config
) {
    /**
     * Creates a NetworkConfiguration from a preset name.
     *
     * @param preset Preset name.
     * @return NetworkConfiguration.
     */
    public static NetworkConfiguration of(String preset) {
        return new NetworkConfiguration(preset, null);
    }

    /**
     * Creates a NetworkConfiguration from an EnvironmentNetworkEgressAllowlist object.
     *
     * @param config EnvironmentNetworkEgressAllowlist object.
     * @return NetworkConfiguration.
     */
    public static NetworkConfiguration of(EnvironmentNetworkEgressAllowlist config) {
        return new NetworkConfiguration(null, config);
    }

    /**
     * Creates a NetworkConfiguration from an Object (String, EnvironmentNetworkEgressAllowlist, or NetworkConfiguration).
     *
     * @param network Network configuration object.
     * @return NetworkConfiguration.
     */
    public static NetworkConfiguration of(Object network) {
        if (network == null) {
            return null;
        }
        if (network instanceof NetworkConfiguration nc) {
            return nc;
        }
        if (network instanceof String s) {
            return of(s);
        }
        if (network instanceof EnvironmentNetworkEgressAllowlist e) {
            return of(e);
        }
        throw new IllegalArgumentException("Unsupported network configuration type: " + network.getClass());
    }

    /**
     * Returns true if this configuration is a preset name.
     *
     * @return true if preset is non-null.
     */
    public boolean isPreset() {
        return preset != null;
    }

    /**
     * Returns true if this configuration is a custom EnvironmentNetworkEgressAllowlist object.
     *
     * @return true if config is non-null.
     */
    public boolean isCustom() {
        return config != null;
    }
}
