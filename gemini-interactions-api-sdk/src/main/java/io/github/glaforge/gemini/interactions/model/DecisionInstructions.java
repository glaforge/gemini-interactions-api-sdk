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
import io.github.glaforge.gemini.interactions.model.deserializer.DecisionInstructionsDeserializer;
import io.github.glaforge.gemini.interactions.model.deserializer.DecisionInstructionsSerializer;
import java.util.List;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Type-safe union record representing decision question instructions.
 * Instructions accept either a plain text string or one or more {@link Content.TextContent} blocks.
 *
 * @param text   The plain text instructions, or null if represented as text content blocks.
 * @param blocks The list of text content blocks, or null if represented as plain text.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(using = DecisionInstructionsSerializer.class)
@JsonDeserialize(using = DecisionInstructionsDeserializer.class)
public record DecisionInstructions(
    String text,
    List<Content.TextContent> blocks
) {
    /**
     * Creates DecisionInstructions from a plain text string.
     *
     * @param text The question instructions text.
     * @return A DecisionInstructions instance.
     */
    public static DecisionInstructions of(String text) {
        return new DecisionInstructions(text, null);
    }

    /**
     * Creates DecisionInstructions from a single {@link Content.TextContent} block.
     *
     * @param block The text content block.
     * @return A DecisionInstructions instance.
     */
    public static DecisionInstructions of(Content.TextContent block) {
        return ofBlocks(block != null ? List.of(block) : null);
    }

    /**
     * Creates DecisionInstructions from a list of {@link Content.TextContent} blocks.
     *
     * @param blocks The list of text content blocks.
     * @return A DecisionInstructions instance.
     */
    public static DecisionInstructions ofBlocks(List<Content.TextContent> blocks) {
        return new DecisionInstructions(null, blocks != null ? List.copyOf(blocks) : null);
    }

    /**
     * Creates DecisionInstructions from an array of {@link Content.TextContent} blocks.
     *
     * @param blocks The text content blocks.
     * @return A DecisionInstructions instance.
     */
    public static DecisionInstructions ofBlocks(Content.TextContent... blocks) {
        return ofBlocks(blocks != null ? List.of(blocks) : null);
    }

    /**
     * Checks whether these instructions are stored as a plain text string.
     *
     * @return true if {@code text} is non-null.
     */
    public boolean isText() {
        return text != null;
    }

    /**
     * Checks whether these instructions are stored as {@link Content.TextContent} blocks.
     *
     * @return true if {@code blocks} is non-null.
     */
    public boolean isBlocks() {
        return blocks != null;
    }

    /**
     * Returns the full instruction text, concatenating adjacent {@link Content.TextContent} blocks
     * in order with no separator (matching the Decisions API behavior).
     *
     * @return The resolved instruction text, or null if empty.
     */
    public String asText() {
        if (text != null) {
            return text;
        }
        if (blocks != null) {
            StringBuilder sb = new StringBuilder();
            for (Content.TextContent block : blocks) {
                if (block != null && block.text() != null) {
                    sb.append(block.text());
                }
            }
            return sb.toString();
        }
        return null;
    }
}
