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

import io.github.glaforge.gemini.interactions.model.SpeechConfiguration;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Custom Jackson serializer for {@link SpeechConfiguration}.
 */
public class SpeechConfigurationSerializer extends ValueSerializer<SpeechConfiguration> {

    /**
     * Default constructor.
     */
    public SpeechConfigurationSerializer() {
        super();
    }

    @Override
    public void serialize(SpeechConfiguration value, JsonGenerator g, SerializationContext ctxt) throws JacksonException {
        if (value == null) {
            g.writeNull();
        } else if (value.isMultiSpeaker()) {
            ctxt.writeValue(g, value.multiSpeakerConfig());
        } else if (value.singleSpeakerConfigs() != null) {
            ctxt.writeValue(g, value.singleSpeakerConfigs());
        } else {
            g.writeNull();
        }
    }
}
