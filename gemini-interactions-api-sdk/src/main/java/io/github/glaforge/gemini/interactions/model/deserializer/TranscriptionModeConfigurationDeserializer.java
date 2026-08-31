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

import io.github.glaforge.gemini.interactions.model.Config.SmartTranscriptionMode;
import io.github.glaforge.gemini.interactions.model.Config.TranscriptionMode;
import io.github.glaforge.gemini.interactions.model.Config.VerbatimTranscriptionMode;
import io.github.glaforge.gemini.interactions.model.TranscriptionModeConfiguration;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom Jackson deserializer for {@link TranscriptionModeConfiguration}.
 */
public class TranscriptionModeConfigurationDeserializer extends ValueDeserializer<TranscriptionModeConfiguration> {

    /** Default constructor. */
    public TranscriptionModeConfigurationDeserializer() {
        super();
    }

    @Override
    public TranscriptionModeConfiguration deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            return TranscriptionModeConfiguration.of(p.getString());
        }

        JsonNode node = ctxt.readTree(p);
        if (node != null && node.isTextual()) {
            return TranscriptionModeConfiguration.of(node.asText());
        }

        if (node != null && node.isObject()) {
            String type = node.has("type") ? node.get("type").asText() : "";
            TranscriptionMode mode;
            if ("smart".equalsIgnoreCase(type)) {
                mode = ctxt.readTreeAsValue(node, SmartTranscriptionMode.class);
            } else if ("verbatim".equalsIgnoreCase(type)) {
                mode = ctxt.readTreeAsValue(node, VerbatimTranscriptionMode.class);
            } else {
                if (node.has("diarization_mode") || node.has("timestamp_granularities")) {
                    mode = ctxt.readTreeAsValue(node, VerbatimTranscriptionMode.class);
                } else {
                    mode = ctxt.readTreeAsValue(node, SmartTranscriptionMode.class);
                }
            }
            return TranscriptionModeConfiguration.of(mode);
        }

        return null;
    }
}
