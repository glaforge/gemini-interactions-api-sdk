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
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionInput;
import io.github.glaforge.gemini.interactions.model.Step;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom Jackson deserializer for {@link InteractionInput}.
 */
public class InteractionInputDeserializer extends ValueDeserializer<InteractionInput> {

    /** Default constructor. */
    public InteractionInputDeserializer() {
        super();
    }

    @Override
    public InteractionInput deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            return InteractionInput.of(p.getString());
        }

        JsonNode node = ctxt.readTree(p);
        if (node == null || node.isNull()) {
            return null;
        }

        if (node.isTextual()) {
            return InteractionInput.of(node.asText());
        }

        if (node.isArray()) {
            if (node.isEmpty()) {
                return InteractionInput.ofContents(List.of());
            }
            JsonNode first = node.get(0);
            if (first.has("role")) {
                List<Interaction.Turn> turns = new ArrayList<>();
                for (JsonNode elem : node) {
                    turns.add(ctxt.readTreeAsValue(elem, Interaction.Turn.class));
                }
                return InteractionInput.ofTurns(turns);
            }
            if (first.has("type")) {
                String type = first.get("type").asText("");
                if (type.equals("text") || type.equals("image") || type.equals("audio")
                        || type.equals("document") || type.equals("video")) {
                    List<Content> contents = new ArrayList<>();
                    for (JsonNode elem : node) {
                        contents.add(ctxt.readTreeAsValue(elem, Content.class));
                    }
                    return InteractionInput.ofContents(contents);
                } else {
                    try {
                        List<Step> steps = new ArrayList<>();
                        for (JsonNode elem : node) {
                            steps.add(ctxt.readTreeAsValue(elem, Step.class));
                        }
                        return InteractionInput.ofSteps(steps);
                    } catch (Exception ignored) {
                        List<Content> contents = new ArrayList<>();
                        for (JsonNode elem : node) {
                            contents.add(ctxt.readTreeAsValue(elem, Content.class));
                        }
                        return InteractionInput.ofContents(contents);
                    }
                }
            }

            List<Content> contents = new ArrayList<>();
            for (JsonNode elem : node) {
                contents.add(ctxt.readTreeAsValue(elem, Content.class));
            }
            return InteractionInput.ofContents(contents);
        }

        if (node.isObject() && node.has("type")) {
            Content c = ctxt.readTreeAsValue(node, Content.class);
            return InteractionInput.of(c);
        }

        return InteractionInput.of(node.toString());
    }
}
