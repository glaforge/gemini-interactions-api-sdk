/*
 * Copyright 2025 Google LLC
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
 * A harm category for safety settings.
 */
public enum HarmCategory {
    /** Hate speech. */
    @JsonProperty("hate_speech") HATE_SPEECH,
    /** Dangerous content. */
    @JsonProperty("dangerous_content") DANGEROUS_CONTENT,
    /** Harassment. */
    @JsonProperty("harassment") HARASSMENT,
    /** Sexually explicit. */
    @JsonProperty("sexually_explicit") SEXUALLY_EXPLICIT,
    /** Civic integrity. */
    @JsonProperty("civic_integrity") CIVIC_INTEGRITY,
    /** Image hate. */
    @JsonProperty("image_hate") IMAGE_HATE,
    /** Image dangerous content. */
    @JsonProperty("image_dangerous_content") IMAGE_DANGEROUS_CONTENT,
    /** Image harassment. */
    @JsonProperty("image_harassment") IMAGE_HARASSMENT,
    /** Image sexually explicit. */
    @JsonProperty("image_sexually_explicit") IMAGE_SEXUALLY_EXPLICIT,
    /** Jailbreak. */
    @JsonProperty("jailbreak") JAILBREAK
}
