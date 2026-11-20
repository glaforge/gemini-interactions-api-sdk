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
import io.github.glaforge.gemini.interactions.model.DocumentProcessingConfiguration;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom Jackson deserializer for {@link DocumentProcessingConfiguration}.
 */
public class DocumentProcessingConfigurationDeserializer extends ValueDeserializer<DocumentProcessingConfiguration> {

    /**
     * Default constructor.
     */
    public DocumentProcessingConfigurationDeserializer() {
        super();
    }

    @Override
    public DocumentProcessingConfiguration deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            return DocumentProcessingConfiguration.of(p.getString());
        }

        JsonNode node = ctxt.readTree(p);
        Content.DocumentStaticMediaProcessing config = ctxt.readTreeAsValue(node, Content.DocumentStaticMediaProcessing.class);
        return DocumentProcessingConfiguration.of(config);
    }
}
