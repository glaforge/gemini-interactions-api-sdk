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

/**
 * Configuration for an environment that lives on the client connection rather
 * than in a server-managed sandbox.
 *
 * @param type The type (must be "local").
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LocalEnvironmentConfig(
    String type
) {
    /**
     * Creates a new local EnvironmentConfig.
     */
    public LocalEnvironmentConfig() {
        this("local");
    }
}
