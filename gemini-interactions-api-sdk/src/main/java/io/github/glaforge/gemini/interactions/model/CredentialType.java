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
 * Supported credential types for server-managed agent secrets.
 */
public enum CredentialType {
    /**
     * HTTP Bearer token credential.
     */
    @JsonProperty("bearer_token")
    BEARER_TOKEN,

    /**
     * OAuth2 client credential.
     */
    @JsonProperty("oauth2")
    OAUTH2,

    /**
     * Environment variable credential.
     */
    @JsonProperty("environment_variable")
    ENVIRONMENT_VARIABLE
}
