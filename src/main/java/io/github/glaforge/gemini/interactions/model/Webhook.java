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
import java.util.List;

/**
 * A Webhook resource.
 *
 * @param id                The ID of the webhook.
 * @param name              The user-provided name of the webhook.
 * @param uri               The URI to which webhook events will be sent.
 * @param subscribedEvents  The events that the webhook is subscribed to.
 * @param state             The state of the webhook.
 * @param createTime        The timestamp when the webhook was created.
 * @param updateTime        The timestamp when the webhook was last updated.
 * @param signingSecrets    The signing secrets associated with this webhook.
 * @param newSigningSecret  The new signing secret for the webhook. Only populated on create.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Webhook(
    String id,
    String name,
    String uri,
    @JsonProperty("subscribed_events") List<String> subscribedEvents,
    State state,
    @JsonProperty("create_time") String createTime,
    @JsonProperty("update_time") String updateTime,
    @JsonProperty("signing_secrets") List<SigningSecret> signingSecrets,
    @JsonProperty("new_signing_secret") String newSigningSecret
) {
    /**
     * The state of the webhook.
     */
    public enum State {
        /** The webhook is enabled. */
        @JsonProperty("enabled") ENABLED,
        /** The webhook is disabled by the user. */
        @JsonProperty("disabled") DISABLED,
        /** The webhook is disabled due to failed deliveries. */
        @JsonProperty("disabled_due_to_failed_deliveries") DISABLED_DUE_TO_FAILED_DELIVERIES
    }
}
