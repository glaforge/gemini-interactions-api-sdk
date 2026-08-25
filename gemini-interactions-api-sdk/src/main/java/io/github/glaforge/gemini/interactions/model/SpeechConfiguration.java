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
import io.github.glaforge.gemini.interactions.model.deserializer.SpeechConfigurationDeserializer;
import io.github.glaforge.gemini.interactions.model.deserializer.SpeechConfigurationSerializer;

import java.util.List;

/**
 * Type-safe configuration for speech generation, representing either a single-speaker
 * list of {@link Config.SpeechConfig} or a multi-speaker {@link Config.SpeakerConfig}.
 *
 * @param singleSpeakerConfigs List of single speaker configs (or null if multi-speaker).
 * @param multiSpeakerConfig Multi-speaker config (or null if single-speaker).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(using = SpeechConfigurationSerializer.class)
@JsonDeserialize(using = SpeechConfigurationDeserializer.class)
public record SpeechConfiguration(
    List<Config.SpeechConfig> singleSpeakerConfigs,
    Config.SpeakerConfig multiSpeakerConfig
) {
    /**
     * Creates a SpeechConfiguration from a list of single-speaker configs.
     *
     * @param configs List of SpeechConfig.
     * @return SpeechConfiguration.
     */
    public static SpeechConfiguration of(List<Config.SpeechConfig> configs) {
        return new SpeechConfiguration(configs, null);
    }

    /**
     * Creates a SpeechConfiguration from single-speaker configs.
     *
     * @param configs SpeechConfig array.
     * @return SpeechConfiguration.
     */
    public static SpeechConfiguration of(Config.SpeechConfig... configs) {
        return new SpeechConfiguration(List.of(configs), null);
    }

    /**
     * Creates a SpeechConfiguration from a multi-speaker config.
     *
     * @param config SpeakerConfig.
     * @return SpeechConfiguration.
     */
    public static SpeechConfiguration of(Config.SpeakerConfig config) {
        return new SpeechConfiguration(null, config);
    }

    /**
     * Returns true if this configuration is multi-speaker.
     *
     * @return true if multiSpeakerConfig is present.
     */
    public boolean isMultiSpeaker() {
        return multiSpeakerConfig != null;
    }

    /**
     * Returns true if this configuration is single-speaker.
     *
     * @return true if singleSpeakerConfigs is present and non-empty.
     */
    public boolean isSingleSpeaker() {
        return singleSpeakerConfigs != null && !singleSpeakerConfigs.isEmpty();
    }
}
