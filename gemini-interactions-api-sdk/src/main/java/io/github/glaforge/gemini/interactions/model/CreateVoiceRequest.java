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

/**
 * Request message for creating a custom voice (VoicesService.CreateVoice).
 *
 * @param voice The voice specification to create.
 * @param store Whether the created voice is persisted and managed by Google (defaults to true for prompted voices).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateVoiceRequest(
    Voice voice,
    Boolean store
) {
    /**
     * Creates a CreateVoiceRequest with default store behavior.
     *
     * @param voice The voice to create.
     * @return A CreateVoiceRequest instance.
     */
    public static CreateVoiceRequest of(Voice voice) {
        return new CreateVoiceRequest(voice, null);
    }

    /**
     * Creates a CreateVoiceRequest with explicit store setting.
     *
     * @param voice The voice to create.
     * @param store Whether to store the voice in Google's managed project storage.
     * @return A CreateVoiceRequest instance.
     */
    public static CreateVoiceRequest of(Voice voice, boolean store) {
        return new CreateVoiceRequest(voice, store);
    }
}
