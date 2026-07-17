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

package io.github.glaforge.gemini.interactions.model.deserializer;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.JsonNode;
import io.github.glaforge.gemini.interactions.model.EnvironmentConfig;
import io.github.glaforge.gemini.interactions.model.LocalEnvironmentConfig;

/**
 * Custom Jackson deserializer to handle base environment configuration which
 * can be either a string (predefined environment name) or a custom environment
 * configuration object.
 */
public class BaseEnvironmentDeserializer extends ValueDeserializer<Object> {

    /**
     * Default constructor.
     */
    public BaseEnvironmentDeserializer() {
        super();
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            return p.getString();
        }
        
        JsonNode node = ctxt.readTree(p);
        if (node.has("type") && "local".equals(node.get("type").asText())) {
            return ctxt.readTreeAsValue(node, LocalEnvironmentConfig.class);
        }
        return ctxt.readTreeAsValue(node, EnvironmentConfig.class);
    }
}
