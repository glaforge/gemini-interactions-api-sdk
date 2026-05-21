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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A source to be mounted into the custom environment.
 *
 * @param type     The source type (e.g. "gcs", "inline", "repository", "skill_registry").
 * @param target   Where the source should appear in the environment.
 * @param content  The inline content if type is inline.
 * @param encoding Optional encoding for inline content (e.g. "base64").
 * @param source   The source location (GCS path, repository Git URL, etc.).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Source(
    Type type,
    String target,
    String content,
    String encoding,
    String source
) {
    /**
     * Enum for Source types.
     */
    public enum Type {
        /** GCS bucket source. */
        @JsonProperty("gcs") GCS,
        /** Inline content source. */
        @JsonProperty("inline") INLINE,
        /** Git repository source. */
        @JsonProperty("repository") REPOSITORY,
        /** Skill registry source. */
        @JsonProperty("skill_registry") SKILL_REGISTRY
    }
}
