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
import io.github.glaforge.gemini.interactions.model.TurnContent;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom Jackson deserializer for {@link TurnContent}.
 */
public class TurnContentDeserializer extends ValueDeserializer<TurnContent> {

    /** Default constructor. */
    public TurnContentDeserializer() {
        super();
    }

    @Override
    public TurnContent deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            return TurnContent.of(p.getString());
        }

        JsonNode node = ctxt.readTree(p);
        if (node == null || node.isNull()) {
            return null;
        }

        if (node.isTextual()) {
            return TurnContent.of(node.asText());
        }

        if (node.isArray()) {
            List<Content> parts = new ArrayList<>();
            for (JsonNode element : node) {
                Content content = ctxt.readTreeAsValue(element, Content.class);
                parts.add(content);
            }
            return TurnContent.of(parts);
        }

        if (node.isObject() && node.has("type")) {
            Content content = ctxt.readTreeAsValue(node, Content.class);
            return TurnContent.of(content);
        }

        return TurnContent.of(node.toString());
    }
}
