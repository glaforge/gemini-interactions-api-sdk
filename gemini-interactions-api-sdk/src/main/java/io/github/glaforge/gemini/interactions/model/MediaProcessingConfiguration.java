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
import io.github.glaforge.gemini.interactions.model.deserializer.MediaProcessingConfigurationDeserializer;
import io.github.glaforge.gemini.interactions.model.deserializer.MediaProcessingConfigurationSerializer;

/**
 * Type-safe media processing configuration for media content, representing either a preset
 * name (e.g. "static", "agentic") or a detailed {@link Content.MediaProcessing} object.
 *
 * @param preset Predefined media processing mode string (or null if custom processing config).
 * @param config Custom media processing configuration (or null if preset).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(using = MediaProcessingConfigurationSerializer.class)
@JsonDeserialize(using = MediaProcessingConfigurationDeserializer.class)
public record MediaProcessingConfiguration(
    String preset,
    Content.MediaProcessing config
) {
    /**
     * Creates a MediaProcessingConfiguration from a preset mode name.
     *
     * @param preset Preset mode name.
     * @return MediaProcessingConfiguration.
     */
    public static MediaProcessingConfiguration of(String preset) {
        return new MediaProcessingConfiguration(preset, null);
    }

    /**
     * Creates a MediaProcessingConfiguration from a MediaProcessing object.
     *
     * @param config MediaProcessing object.
     * @return MediaProcessingConfiguration.
     */
    public static MediaProcessingConfiguration of(Content.MediaProcessing config) {
        return new MediaProcessingConfiguration(null, config);
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
     * Returns true if this configuration is a custom MediaProcessing object.
     *
     * @return true if config is non-null.
     */
    public boolean isCustom() {
        return config != null;
    }
}
