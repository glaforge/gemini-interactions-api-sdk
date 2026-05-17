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
 * Represents a signing secret used to verify webhook payloads.
 *
 * @param truncatedSecret The truncated version of the signing secret.
 * @param expireTime      The expiration date of the signing secret.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SigningSecret(
    @JsonProperty("truncated_secret") String truncatedSecret,
    @JsonProperty("expire_time") String expireTime
) {}
