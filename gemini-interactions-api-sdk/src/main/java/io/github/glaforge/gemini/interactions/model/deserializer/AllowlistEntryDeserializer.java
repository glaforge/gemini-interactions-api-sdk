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

import io.github.glaforge.gemini.interactions.model.AllowlistEntry;
import java.util.List;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom Jackson deserializer for {@link AllowlistEntry}, supporting header transform
 * as either a single map (e.g. {"Authorization": "Bearer ..."}) or a list of maps.
 */
public class AllowlistEntryDeserializer extends ValueDeserializer<AllowlistEntry> {

    /**
     * Default constructor.
     */
    public AllowlistEntryDeserializer() {
        super();
    }

    @Override
    public AllowlistEntry deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        JsonNode node = ctxt.readTree(p);
        if (node == null || node.isNull()) {
            return null;
        }

        String domain = node.has("domain") && !node.get("domain").isNull() ? node.get("domain").asText() : null;
        List<Map<String, String>> transform = null;

        if (node.has("transform") && !node.get("transform").isNull()) {
            JsonNode transformNode = node.get("transform");
            JavaType mapType = ctxt.getTypeFactory().constructMapType(Map.class, String.class, String.class);
            if (transformNode.isObject()) {
                Map<String, String> singleMap = ctxt.readTreeAsValue(transformNode, mapType);
                if (singleMap != null) {
                    transform = List.of(singleMap);
                }
            } else if (transformNode.isArray()) {
                JavaType listType = ctxt.getTypeFactory().constructCollectionType(List.class, mapType);
                transform = ctxt.readTreeAsValue(transformNode, listType);
            }
        }

        return new AllowlistEntry(domain, transform);
    }
}
