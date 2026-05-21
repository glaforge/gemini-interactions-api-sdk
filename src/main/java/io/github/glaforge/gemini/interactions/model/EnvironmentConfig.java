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
import tools.jackson.databind.annotation.JsonDeserialize;
import io.github.glaforge.gemini.interactions.model.deserializer.NetworkConfigDeserializer;
import java.util.List;

/**
 * Configuration for a custom environment.
 *
 * @param type    The type (must be "remote").
 * @param network Network configuration (either EnvironmentNetworkEgressAllowlist or the string "disabled").
 * @param sources Sources to be mounted into the environment.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EnvironmentConfig(
    String type,
    @JsonDeserialize(using = NetworkConfigDeserializer.class) Object network,
    List<Source> sources
) {
    /**
     * Creates a custom remote EnvironmentConfig.
     *
     * @param network Network config (EnvironmentNetworkEgressAllowlist or "disabled").
     * @param sources Mounted sources list.
     */
    public EnvironmentConfig(Object network, List<Source> sources) {
        this("remote", network, sources);
    }
}
