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
import io.github.glaforge.gemini.interactions.model.EnvironmentNetworkEgressAllowlist;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom Jackson deserializer for {@link EnvironmentNetworkEgressAllowlist}, supporting
 * {@code allowlist} as either a list of domain rules or the string {@code "disabled"}.
 */
public class EnvironmentNetworkEgressAllowlistDeserializer extends ValueDeserializer<EnvironmentNetworkEgressAllowlist> {

    /**
     * Default constructor.
     */
    public EnvironmentNetworkEgressAllowlistDeserializer() {
        super();
    }

    @Override
    public EnvironmentNetworkEgressAllowlist deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        JsonNode node = ctxt.readTree(p);
        if (node == null || node.isNull()) {
            return null;
        }

        if (node.isTextual()) {
            return new EnvironmentNetworkEgressAllowlist(null, node.asText());
        }

        if (node.has("allowlist") && !node.get("allowlist").isNull()) {
            JsonNode allowlistNode = node.get("allowlist");
            if (allowlistNode.isTextual()) {
                return new EnvironmentNetworkEgressAllowlist(null, allowlistNode.asText());
            } else if (allowlistNode.isArray()) {
                JavaType listType = ctxt.getTypeFactory().constructCollectionType(List.class, AllowlistEntry.class);
                List<AllowlistEntry> entries = ctxt.readTreeAsValue(allowlistNode, listType);
                return new EnvironmentNetworkEgressAllowlist(entries, null);
            }
        }

        return new EnvironmentNetworkEgressAllowlist(null, null);
    }
}
