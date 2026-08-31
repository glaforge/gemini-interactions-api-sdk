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

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import io.github.glaforge.gemini.interactions.model.Config.TranscriptionMode;
import io.github.glaforge.gemini.interactions.model.deserializer.TranscriptionModeConfigurationDeserializer;
import io.github.glaforge.gemini.interactions.model.deserializer.TranscriptionModeConfigurationSerializer;

/**
 * Represents a type-safe union configuration for transcription modes.
 * Wraps either a preset mode string (e.g. "verbatim", "smart") or a structured {@link TranscriptionMode} object.
 *
 * @param preset The preset mode string (e.g. "verbatim", "smart"), if specified as a simple string.
 * @param config The structured transcription mode configuration, if specified as a detailed object.
 */
@JsonSerialize(using = TranscriptionModeConfigurationSerializer.class)
@JsonDeserialize(using = TranscriptionModeConfigurationDeserializer.class)
public record TranscriptionModeConfiguration(
    String preset,
    TranscriptionMode config
) {
    /**
     * Creates a TranscriptionModeConfiguration from a raw object.
     *
     * @param raw Can be a TranscriptionModeConfiguration, TranscriptionMode, or String preset.
     * @return The TranscriptionModeConfiguration, or null if raw is null.
     */
    public static TranscriptionModeConfiguration of(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof TranscriptionModeConfiguration t) {
            return t;
        }
        if (raw instanceof TranscriptionMode mode) {
            return new TranscriptionModeConfiguration(null, mode);
        }
        if (raw instanceof String str) {
            return new TranscriptionModeConfiguration(str, null);
        }
        throw new IllegalArgumentException("Unsupported transcription mode type: " + raw.getClass().getName());
    }

    /**
     * Returns true if this configuration is a simple preset mode string.
     *
     * @return true if preset is non-null.
     */
    public boolean isPreset() {
        return preset != null;
    }

    /**
     * Returns true if this configuration is a structured detailed object.
     *
     * @return true if config is non-null.
     */
    public boolean isCustom() {
        return config != null;
    }
}
