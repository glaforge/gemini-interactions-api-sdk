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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Input-only parameters for replicated voice generation (Voice Replication).
 *
 * @param sourceAudio  The reference audio sample of the speaker's voice to replicate.
 * @param consentAudio Audio recording of the same speaker reading the required consent phrase.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReplicatedVoice(
    @JsonProperty("source_audio") AudioData sourceAudio,
    @JsonProperty("consent_audio") AudioData consentAudio
) {
    /**
     * Creates a new ReplicatedVoice with source and consent audio.
     *
     * @param sourceAudio  The reference audio sample.
     * @param consentAudio The consent audio recording.
     * @return A ReplicatedVoice instance.
     */
    public static ReplicatedVoice of(AudioData sourceAudio, AudioData consentAudio) {
        return new ReplicatedVoice(sourceAudio, consentAudio);
    }
}
