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

package io.github.glaforge.gemini.interactions.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import io.github.glaforge.gemini.interactions.model.deserializer.ToolChoiceConfigurationDeserializer;
import io.github.glaforge.gemini.interactions.model.deserializer.ToolChoiceConfigurationSerializer;

/**
 * Type-safe configuration for tool choice, representing either a mode string
 * ("auto", "any", "none", "validated") or a detailed {@link Tool.ToolChoiceConfig}.
 *
 * @param mode The tool choice mode string (e.g. "auto", "any", "none", "validated"), or null if configured via config.
 * @param config The detailed tool choice configuration, or null if using a preset mode string.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(using = ToolChoiceConfigurationSerializer.class)
@JsonDeserialize(using = ToolChoiceConfigurationDeserializer.class)
public record ToolChoiceConfiguration(
    String mode,
    Tool.ToolChoiceConfig config
) {
    /**
     * Creates a ToolChoiceConfiguration with a mode string ("auto", "any", "none", "validated").
     *
     * @param mode The tool choice mode string.
     * @return A ToolChoiceConfiguration instance.
     */
    public static ToolChoiceConfiguration of(String mode) {
        return new ToolChoiceConfiguration(mode, null);
    }

    /**
     * Creates a ToolChoiceConfiguration with a detailed ToolChoiceConfig.
     *
     * @param config The detailed tool choice configuration.
     * @return A ToolChoiceConfiguration instance.
     */
    public static ToolChoiceConfiguration of(Tool.ToolChoiceConfig config) {
        return new ToolChoiceConfiguration(null, config);
    }

    /**
     * Creates a ToolChoiceConfiguration with an Object (String or ToolChoiceConfig).
     *
     * @param toolChoice The tool choice object.
     * @return A ToolChoiceConfiguration instance.
     */
    public static ToolChoiceConfiguration of(Object toolChoice) {
        if (toolChoice == null) {
            return null;
        }
        if (toolChoice instanceof ToolChoiceConfiguration tcc) {
            return tcc;
        }
        if (toolChoice instanceof String s) {
            return of(s);
        }
        if (toolChoice instanceof Tool.ToolChoiceConfig tcc) {
            return of(tcc);
        }
        throw new IllegalArgumentException("Unsupported tool choice configuration type: " + toolChoice.getClass());
    }

    /**
     * Creates a ToolChoiceConfiguration for AUTO mode.
     *
     * @return A ToolChoiceConfiguration instance.
     */
    public static ToolChoiceConfiguration auto() {
        return of("auto");
    }

    /**
     * Creates a ToolChoiceConfiguration for ANY mode.
     *
     * @return A ToolChoiceConfiguration instance.
     */
    public static ToolChoiceConfiguration any() {
        return of("any");
    }

    /**
     * Creates a ToolChoiceConfiguration for NONE mode.
     *
     * @return A ToolChoiceConfiguration instance.
     */
    public static ToolChoiceConfiguration none() {
        return of("none");
    }

    /**
     * Creates a ToolChoiceConfiguration for VALIDATED mode.
     *
     * @return A ToolChoiceConfiguration instance.
     */
    public static ToolChoiceConfiguration validated() {
        return of("validated");
    }

    /**
     * Returns true if this configuration is a simple mode string.
     *
     * @return true if mode is non-null.
     */
    public boolean isMode() {
        return mode != null;
    }

    /**
     * Returns true if this configuration is a detailed ToolChoiceConfig.
     *
     * @return true if config is non-null.
     */
    public boolean isConfig() {
        return config != null;
    }
}
