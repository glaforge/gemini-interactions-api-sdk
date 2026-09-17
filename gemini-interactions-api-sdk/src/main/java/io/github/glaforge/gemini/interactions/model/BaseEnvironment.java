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
import io.github.glaforge.gemini.interactions.model.deserializer.BaseEnvironmentDeserializer;
import io.github.glaforge.gemini.interactions.model.deserializer.BaseEnvironmentSerializer;

/**
 * Type-safe base environment configuration for an Agent, representing either a preset
 * name (e.g. "default") or a custom {@link EnvironmentConfig}.
 *
 * @param preset Predefined base environment name (or null if custom config).
 * @param config Custom base environment config (or null if preset).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(using = BaseEnvironmentSerializer.class)
@JsonDeserialize(using = BaseEnvironmentDeserializer.class)
public record BaseEnvironment(
    String preset,
    EnvironmentConfig config
) {
    /**
     * Creates a BaseEnvironment from a preset name.
     *
     * @param preset Preset environment name.
     * @return BaseEnvironment.
     */
    public static BaseEnvironment of(String preset) {
        return new BaseEnvironment(preset, null);
    }

    /**
     * Creates a BaseEnvironment from an EnvironmentConfig object.
     *
     * @param config EnvironmentConfig.
     * @return BaseEnvironment.
     */
    public static BaseEnvironment of(EnvironmentConfig config) {
        return new BaseEnvironment(null, config);
    }

    /**
     * Creates a BaseEnvironment from an Object (String, EnvironmentConfig, or BaseEnvironment).
     *
     * @param environment The environment object.
     * @return BaseEnvironment instance.
     */
    public static BaseEnvironment of(Object environment) {
        if (environment == null) {
            return null;
        }
        if (environment instanceof BaseEnvironment be) {
            return be;
        }
        if (environment instanceof String s) {
            return of(s);
        }
        if (environment instanceof EnvironmentConfig ec) {
            return of(ec);
        }
        throw new IllegalArgumentException("Unsupported base environment type: " + environment.getClass());
    }

    /**
     * Returns true if this base environment is a preset name.
     *
     * @return true if preset is non-null.
     */
    public boolean isPreset() {
        return preset != null;
    }

    /**
     * Returns true if this base environment is a custom EnvironmentConfig object.
     *
     * @return true if config is non-null.
     */
     public boolean isCustom() {
         return config != null;
     }

    /**
     * Returns the underlying value (either {@link String} preset or {@link EnvironmentConfig}).
     *
     * @return The underlying value.
     */
    public Object value() {
        return preset != null ? preset : config;
    }
}
