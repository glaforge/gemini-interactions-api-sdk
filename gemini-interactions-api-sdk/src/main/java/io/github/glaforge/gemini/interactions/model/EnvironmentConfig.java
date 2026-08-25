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
import java.util.List;

/**
 * Configuration for a custom environment.
 *
 * @param type    The type (must be "remote").
 * @param network Network configuration.
 * @param sources Sources to be mounted into the environment.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EnvironmentConfig(
    String type,
    NetworkConfiguration network,
    List<Source> sources
) {
    /**
     * Creates a custom remote EnvironmentConfig.
     *
     * @param network Network configuration.
     * @param sources Mounted sources list.
     */
    public EnvironmentConfig(NetworkConfiguration network, List<Source> sources) {
        this("remote", network, sources);
    }

    /**
     * Creates a custom remote EnvironmentConfig from an EnvironmentNetworkEgressAllowlist.
     *
     * @param config EnvironmentNetworkEgressAllowlist.
     * @param sources Mounted sources list.
     */
    public EnvironmentConfig(EnvironmentNetworkEgressAllowlist config, List<Source> sources) {
        this("remote", config != null ? NetworkConfiguration.of(config) : null, sources);
    }

    /**
     * Creates a custom remote EnvironmentConfig from a preset string.
     *
     * @param preset Network preset string.
     * @param sources Mounted sources list.
     */
    public EnvironmentConfig(String preset, List<Source> sources) {
        this("remote", preset != null ? NetworkConfiguration.of(preset) : null, sources);
    }
}
