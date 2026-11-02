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
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Request message for WebhookService.PingWebhook.
 *
 * @param id Required. The ID of the webhook to ping.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record PingWebhookRequest(
    String id
) {
    /**
     * Backward-compatible no-arg constructor.
     */
    public PingWebhookRequest() {
        this(null);
    }

    /**
     * Creates a new PingWebhookRequest with the specified webhook ID.
     *
     * @param id The ID of the webhook to ping.
     * @return A PingWebhookRequest instance.
     */
    public static PingWebhookRequest of(String id) {
        return new PingWebhookRequest(id);
    }
}
