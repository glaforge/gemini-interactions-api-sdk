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
import io.github.glaforge.gemini.interactions.model.deserializer.InteractionInputDeserializer;
import io.github.glaforge.gemini.interactions.model.deserializer.InteractionInputSerializer;
import java.util.List;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Type-safe union record representing interaction input payload.
 * Can be a plain text string, a list of {@link Content} objects, a list of conversation {@link Interaction.Turn} objects,
 * or a list of {@link Step} objects.
 *
 * @param text     The text prompt, or null if using multimodal content/turns/steps.
 * @param contents The content parts, or null if using text/turns/steps.
 * @param turns    The conversation turns, or null if using text/contents/steps.
 * @param steps    The interaction steps, or null if using text/contents/turns.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(using = InteractionInputSerializer.class)
@JsonDeserialize(using = InteractionInputDeserializer.class)
public record InteractionInput(
    String text,
    List<Content> contents,
    List<Interaction.Turn> turns,
    List<Step> steps
) {
    /**
     * Creates an InteractionInput from a plain text string.
     *
     * @param text The input prompt text.
     * @return An InteractionInput instance.
     */
    public static InteractionInput of(String text) {
        return new InteractionInput(text, null, null, null);
    }

    /**
     * Creates an InteractionInput from a single Content part.
     *
     * @param content The content part.
     * @return An InteractionInput instance.
     */
    public static InteractionInput of(Content content) {
        return ofContents(content != null ? List.of(content) : null);
    }

    /**
     * Creates an InteractionInput from a list of Content parts.
     *
     * @param contents The content parts.
     * @return An InteractionInput instance.
     */
    public static InteractionInput ofContents(List<Content> contents) {
        return new InteractionInput(null, contents != null ? List.copyOf(contents) : null, null, null);
    }

    /**
     * Creates an InteractionInput from array of Content parts.
     *
     * @param contents The content parts.
     * @return An InteractionInput instance.
     */
    public static InteractionInput ofContents(Content... contents) {
        return ofContents(contents != null ? List.of(contents) : null);
    }

    /**
     * Creates an InteractionInput from a list of conversation turns.
     *
     * @param turns The conversation turns.
     * @return An InteractionInput instance.
     */
    public static InteractionInput ofTurns(List<Interaction.Turn> turns) {
        return new InteractionInput(null, null, turns != null ? List.copyOf(turns) : null, null);
    }

    /**
     * Creates an InteractionInput from array of conversation turns.
     *
     * @param turns The conversation turns.
     * @return An InteractionInput instance.
     */
    public static InteractionInput ofTurns(Interaction.Turn... turns) {
        return ofTurns(turns != null ? List.of(turns) : null);
    }

    /**
     * Creates an InteractionInput from a list of steps.
     *
     * @param steps The steps.
     * @return An InteractionInput instance.
     */
    public static InteractionInput ofSteps(List<Step> steps) {
        return new InteractionInput(null, null, null, steps != null ? List.copyOf(steps) : null);
    }

    /**
     * Creates an InteractionInput from array of steps.
     *
     * @param steps The steps.
     * @return An InteractionInput instance.
     */
    public static InteractionInput ofSteps(Step... steps) {
        return ofSteps(steps != null ? List.of(steps) : null);
    }

    /**
     * Creates an InteractionInput from an arbitrary object.
     *
     * @param input The input object.
     * @return An InteractionInput instance.
     */
    @SuppressWarnings("unchecked")
    public static InteractionInput of(Object input) {
        if (input == null) {
            return null;
        }
        if (input instanceof InteractionInput ii) {
            return ii;
        }
        if (input instanceof String s) {
            return of(s);
        }
        if (input instanceof Content c) {
            return of(c);
        }
        if (input instanceof Interaction.Turn t) {
            return ofTurns(t);
        }
        if (input instanceof Step s) {
            return ofSteps(s);
        }
        if (input instanceof List<?> list) {
            if (list.isEmpty()) {
                return ofContents(List.<Content>of());
            }
            Object first = list.getFirst();
            if (first instanceof Content) {
                return ofContents((List<Content>) list);
            }
            if (first instanceof Interaction.Turn) {
                return ofTurns((List<Interaction.Turn>) list);
            }
            if (first instanceof Step) {
                return ofSteps((List<Step>) list);
            }
        }
        throw new IllegalArgumentException("Unsupported interaction input type: " + input.getClass());
    }

    /**
     * Checks if this represents a simple text prompt.
     *
     * @return true if text is non-null.
     */
    public boolean isText() {
        return text != null;
    }

    /**
     * Checks if this represents content parts.
     *
     * @return true if contents is non-null.
     */
    public boolean isContents() {
        return contents != null;
    }

    /**
     * Checks if this represents conversation turns.
     *
     * @return true if turns is non-null.
     */
    public boolean isTurns() {
        return turns != null;
    }

    /**
     * Checks if this represents interaction steps.
     *
     * @return true if steps is non-null.
     */
    public boolean isSteps() {
        return steps != null;
    }
}
