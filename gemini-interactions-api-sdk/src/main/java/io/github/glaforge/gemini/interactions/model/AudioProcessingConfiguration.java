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
import io.github.glaforge.gemini.interactions.model.deserializer.AudioProcessingConfigurationDeserializer;
import io.github.glaforge.gemini.interactions.model.deserializer.AudioProcessingConfigurationSerializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Type-safe audio processing configuration for {@link Content.AudioContent}, representing either a preset
 * mode name (e.g. {@code "static"}) or a detailed {@link Content.AudioMediaProcessing} object.
 *
 * @param preset Predefined audio processing mode string (or null if custom processing config).
 * @param config Custom audio media processing configuration (or null if preset).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(using = AudioProcessingConfigurationSerializer.class)
@JsonDeserialize(using = AudioProcessingConfigurationDeserializer.class)
public record AudioProcessingConfiguration(
    String preset,
    Content.AudioMediaProcessing config
) {
    /**
     * Creates an AudioProcessingConfiguration from a preset mode name (e.g. {@code "static"}).
     *
     * @param preset Preset mode name.
     * @return An AudioProcessingConfiguration.
     */
    public static AudioProcessingConfiguration of(String preset) {
        return new AudioProcessingConfiguration(preset, null);
    }

    /**
     * Creates an AudioProcessingConfiguration from an {@link Content.AudioMediaProcessing} object.
     *
     * @param config AudioMediaProcessing object.
     * @return An AudioProcessingConfiguration.
     */
    public static AudioProcessingConfiguration of(Content.AudioMediaProcessing config) {
        return new AudioProcessingConfiguration(null, config);
    }

    /**
     * Creates an AudioProcessingConfiguration using the {@code "static"} preset mode
     * (all audio placed in context).
     *
     * @return An AudioProcessingConfiguration set to {@code "static"}.
     */
    public static AudioProcessingConfiguration staticPreset() {
        return of("static");
    }

    /**
     * Returns true if this configuration is a preset mode string.
     *
     * @return true if preset is non-null.
     */
    public boolean isPreset() {
        return preset != null;
    }

    /**
     * Returns true if this configuration is a custom {@link Content.AudioMediaProcessing} object.
     *
     * @return true if config is non-null.
     */
    public boolean isCustom() {
        return config != null;
    }
}
