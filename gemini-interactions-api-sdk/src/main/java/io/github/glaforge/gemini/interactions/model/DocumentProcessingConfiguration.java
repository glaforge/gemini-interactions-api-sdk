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
import io.github.glaforge.gemini.interactions.model.deserializer.DocumentProcessingConfigurationDeserializer;
import io.github.glaforge.gemini.interactions.model.deserializer.DocumentProcessingConfigurationSerializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Type-safe document processing configuration for {@link Content.DocumentContent}, representing either a preset
 * mode name (e.g. {@code "static"}) or a detailed {@link Content.DocumentMediaProcessing} object.
 *
 * @param preset Predefined document processing mode string (or null if custom processing config).
 * @param config Custom document media processing configuration (or null if preset).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(using = DocumentProcessingConfigurationSerializer.class)
@JsonDeserialize(using = DocumentProcessingConfigurationDeserializer.class)
public record DocumentProcessingConfiguration(
    String preset,
    Content.DocumentMediaProcessing config
) {
    /**
     * Creates a DocumentProcessingConfiguration from a preset mode name (e.g. {@code "static"}).
     *
     * @param preset Preset mode name.
     * @return A DocumentProcessingConfiguration.
     */
    public static DocumentProcessingConfiguration of(String preset) {
        return new DocumentProcessingConfiguration(preset, null);
    }

    /**
     * Creates a DocumentProcessingConfiguration from a {@link Content.DocumentMediaProcessing} object.
     *
     * @param config DocumentMediaProcessing object.
     * @return A DocumentProcessingConfiguration.
     */
    public static DocumentProcessingConfiguration of(Content.DocumentMediaProcessing config) {
        return new DocumentProcessingConfiguration(null, config);
    }

    /**
     * Creates a DocumentProcessingConfiguration using the {@code "static"} preset mode
     * (all extracted pages placed in context).
     *
     * @return A DocumentProcessingConfiguration set to {@code "static"}.
     */
    public static DocumentProcessingConfiguration staticPreset() {
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
     * Returns true if this configuration is a custom {@link Content.DocumentMediaProcessing} object.
     *
     * @return true if config is non-null.
     */
    public boolean isCustom() {
        return config != null;
    }
}
