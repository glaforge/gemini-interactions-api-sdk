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

package io.github.glaforge.gemini.interactions.model.deserializer;

import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams;
import io.github.glaforge.gemini.interactions.model.TriggerInteraction;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom Jackson deserializer for {@link TriggerInteraction}.
 */
public class TriggerInteractionDeserializer extends ValueDeserializer<TriggerInteraction> {

    /** Default constructor. */
    public TriggerInteractionDeserializer() {
        super();
    }

    @Override
    public TriggerInteraction deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        JsonNode node = ctxt.readTree(p);
        if (node == null || node.isNull()) {
            return null;
        }

        // An Interaction resource has "status" and "id" (or "events").
        if (node.has("status") && (node.has("id") || node.has("created") || node.has("events"))) {
            Interaction interaction = ctxt.readTreeAsValue(node, Interaction.class);
            return TriggerInteraction.of(interaction);
        }

        // Check for specific request types
        if (node.has("agent")) {
            InteractionParams.AgentInteractionParams agentParams =
                ctxt.readTreeAsValue(node, InteractionParams.AgentInteractionParams.class);
            return TriggerInteraction.of(agentParams);
        } else if (node.has("model")) {
            InteractionParams.ModelInteractionParams modelParams =
                ctxt.readTreeAsValue(node, InteractionParams.ModelInteractionParams.class);
            return TriggerInteraction.of(modelParams);
        }

        // Fallback: try Interaction first, then AgentInteractionParams
        try {
            Interaction interaction = ctxt.readTreeAsValue(node, Interaction.class);
            return TriggerInteraction.of(interaction);
        } catch (Exception ignored) {
            InteractionParams.AgentInteractionParams agentParams =
                ctxt.readTreeAsValue(node, InteractionParams.AgentInteractionParams.class);
            return TriggerInteraction.of(agentParams);
        }
    }
}
