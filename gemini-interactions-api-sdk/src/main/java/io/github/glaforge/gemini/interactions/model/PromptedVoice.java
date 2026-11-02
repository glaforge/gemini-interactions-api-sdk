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
 * Parameters for prompted voice generation (Voice Design).
 *
 * @param input     The natural-language prompt describing the desired voice.
 * @param baseVoice Optional. The name or ID of an existing base voice to edit or remix (e.g., "Puck" or "voice_abc123").
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record PromptedVoice(
    String input,
    @JsonProperty("base_voice") String baseVoice
) {
    /**
     * Backward-compatible constructor with only the prompt input.
     *
     * @param input The natural-language prompt describing the desired voice.
     */
    public PromptedVoice(String input) {
        this(input, null);
    }

    /**
     * Creates a new PromptedVoice with the specified description input.
     *
     * @param input The natural-language prompt describing the voice.
     * @return A PromptedVoice instance.
     */
    public static PromptedVoice of(String input) {
        return new PromptedVoice(input, null);
    }

    /**
     * Creates a new PromptedVoice remixing or editing an existing base voice.
     *
     * @param input     The natural-language prompt describing relative alterations to make to the base voice.
     * @param baseVoice The name or ID of an existing base voice to edit or remix.
     * @return A PromptedVoice instance.
     */
    public static PromptedVoice of(String input, String baseVoice) {
        return new PromptedVoice(input, baseVoice);
    }
}
