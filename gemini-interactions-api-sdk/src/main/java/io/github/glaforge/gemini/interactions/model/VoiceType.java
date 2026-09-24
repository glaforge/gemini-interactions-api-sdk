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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The type of voice.
 */
public enum VoiceType {
    /** A custom voice replicated from reference and consent audio recordings. */
    @JsonProperty("replicated") REPLICATED,

    /** A custom voice generated from a natural-language text prompt. */
    @JsonProperty("prompted") PROMPTED,

    /** A built-in system voice from Google's voice catalog (e.g. Puck, Kore). */
    @JsonProperty("prebuilt") PREBUILT
}
