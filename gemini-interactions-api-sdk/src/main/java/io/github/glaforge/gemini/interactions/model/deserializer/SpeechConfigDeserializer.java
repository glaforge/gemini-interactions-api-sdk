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

import io.github.glaforge.gemini.interactions.model.Config;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom Jackson deserializer for {@code speech_config} which can be either
 * a list of {@link Config.SpeechConfig} or a single {@link Config.SpeakerConfig} object.
 */
public class SpeechConfigDeserializer extends ValueDeserializer<Object> {

    /**
     * Default constructor.
     */
    public SpeechConfigDeserializer() {
        super();
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        if (p.currentToken() == JsonToken.START_ARRAY) {
            JsonNode arrayNode = ctxt.readTree(p);
            List<Config.SpeechConfig> list = new ArrayList<>();
            for (JsonNode element : arrayNode) {
                list.add(ctxt.readTreeAsValue(element, Config.SpeechConfig.class));
            }
            return list;
        }

        JsonNode node = ctxt.readTree(p);
        if (node.has("speakers")) {
            return ctxt.readTreeAsValue(node, Config.SpeakerConfig.class);
        }
        return ctxt.readTreeAsValue(node, Config.SpeechConfig.class);
    }
}
