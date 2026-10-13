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

import io.github.glaforge.gemini.interactions.model.EnvironmentNetworkEgressAllowlist;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Custom Jackson serializer for {@link EnvironmentNetworkEgressAllowlist}, supporting
 * {@code allowlist} as either a list of domain rules or the string {@code "disabled"}.
 */
public class EnvironmentNetworkEgressAllowlistSerializer extends ValueSerializer<EnvironmentNetworkEgressAllowlist> {

    /**
     * Default constructor.
     */
    public EnvironmentNetworkEgressAllowlistSerializer() {
        super();
    }

    @Override
    public void serialize(EnvironmentNetworkEgressAllowlist value, JsonGenerator g, SerializationContext ctxt) throws JacksonException {
        if (value == null) {
            g.writeNull();
            return;
        }

        g.writeStartObject();
        if (value.isDisabled()) {
            g.writeStringProperty("allowlist", "disabled");
        } else if (value.allowlist() != null) {
            g.writeName("allowlist");
            ctxt.writeValue(g, value.allowlist());
        }
        g.writeEndObject();
    }
}
