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

import io.github.glaforge.gemini.interactions.model.Content;
import io.github.glaforge.gemini.interactions.model.DecisionInstructions;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom Jackson deserializer for {@link DecisionInstructions}.
 */
public class DecisionInstructionsDeserializer extends ValueDeserializer<DecisionInstructions> {

    /** Default constructor. */
    public DecisionInstructionsDeserializer() {
        super();
    }

    @Override
    public DecisionInstructions deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            return DecisionInstructions.of(p.getString());
        }

        JsonNode node = ctxt.readTree(p);
        if (node == null || node.isNull()) {
            return null;
        }

        if (node.isTextual()) {
            return DecisionInstructions.of(node.asText());
        }

        if (node.isArray()) {
            List<Content.TextContent> blocks = new ArrayList<>();
            for (JsonNode elem : node) {
                blocks.add(ctxt.readTreeAsValue(elem, Content.TextContent.class));
            }
            return DecisionInstructions.ofBlocks(blocks);
        }

        if (node.isObject()) {
            Content.TextContent single = ctxt.readTreeAsValue(node, Content.TextContent.class);
            return DecisionInstructions.of(single);
        }

        return DecisionInstructions.of(node.toString());
    }
}
