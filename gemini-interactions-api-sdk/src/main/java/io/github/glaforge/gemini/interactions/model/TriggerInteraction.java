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
import io.github.glaforge.gemini.interactions.model.deserializer.TriggerInteractionDeserializer;
import io.github.glaforge.gemini.interactions.model.deserializer.TriggerInteractionSerializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Type-safe configuration for trigger interaction, representing either an
 * {@link InteractionParams.Request} execution template or a hydrated {@link Interaction} resource.
 *
 * @param request  The interaction request parameters template, or null if represented as an interaction resource.
 * @param resource The interaction resource, or null if represented as request parameters.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(using = TriggerInteractionSerializer.class)
@JsonDeserialize(using = TriggerInteractionDeserializer.class)
public record TriggerInteraction(
    InteractionParams.Request request,
    Interaction resource
) {
    /**
     * Creates a TriggerInteraction wrapping an {@link InteractionParams.Request}.
     *
     * @param request The interaction request template.
     * @return A TriggerInteraction instance.
     */
    public static TriggerInteraction of(InteractionParams.Request request) {
        return new TriggerInteraction(request, null);
    }

    /**
     * Creates a TriggerInteraction wrapping an {@link Interaction}.
     *
     * @param resource The interaction resource.
     * @return A TriggerInteraction instance.
     */
    public static TriggerInteraction of(Interaction resource) {
        return new TriggerInteraction(null, resource);
    }

    /**
     * Creates a TriggerInteraction from an Object (TriggerInteraction, Request, or Interaction).
     *
     * @param interaction The interaction object.
     * @return A TriggerInteraction instance.
     */
    public static TriggerInteraction of(Object interaction) {
        if (interaction == null) {
            return null;
        }
        if (interaction instanceof TriggerInteraction ti) {
            return ti;
        }
        if (interaction instanceof InteractionParams.Request req) {
            return of(req);
        }
        if (interaction instanceof Interaction res) {
            return of(res);
        }
        throw new IllegalArgumentException("Unsupported trigger interaction type: " + interaction.getClass());
    }

    /**
     * Checks if this holds an interaction request template.
     *
     * @return true if request is non-null.
     */
    public boolean isRequest() {
        return request != null;
    }

    /**
     * Checks if this holds an interaction resource.
     *
     * @return true if resource is non-null.
     */
    public boolean isResource() {
        return resource != null;
    }
}
