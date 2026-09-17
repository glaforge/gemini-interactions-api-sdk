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
import io.github.glaforge.gemini.interactions.model.deserializer.TurnContentDeserializer;
import io.github.glaforge.gemini.interactions.model.deserializer.TurnContentSerializer;
import java.util.List;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Type-safe union record for interaction turn content, representing either a plain text string
 * or a rich list of multimodal {@link Content} parts.
 *
 * @param text  The plain text content, or null if represented as content parts.
 * @param parts The list of content parts, or null if represented as plain text.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(using = TurnContentSerializer.class)
@JsonDeserialize(using = TurnContentDeserializer.class)
public record TurnContent(
    String text,
    List<Content> parts
) {
    /**
     * Creates a TurnContent from a plain text string.
     *
     * @param text The text string.
     * @return A TurnContent instance.
     */
    public static TurnContent of(String text) {
        return new TurnContent(text, null);
    }

    /**
     * Creates a TurnContent from a list of Content parts.
     *
     * @param parts The list of content parts.
     * @return A TurnContent instance.
     */
    public static TurnContent of(List<Content> parts) {
        return new TurnContent(null, parts != null ? List.copyOf(parts) : null);
    }

    /**
     * Creates a TurnContent from an array of Content parts.
     *
     * @param parts The content parts.
     * @return A TurnContent instance.
     */
    public static TurnContent of(Content... parts) {
        return new TurnContent(null, parts != null ? List.of(parts) : null);
    }

    /**
     * Creates a TurnContent from an arbitrary object (String, Content, List of Content, or TurnContent).
     *
     * @param content The content object.
     * @return A TurnContent instance.
     */
    @SuppressWarnings("unchecked")
    public static TurnContent of(Object content) {
        if (content == null) {
            return null;
        }
        if (content instanceof TurnContent tc) {
            return tc;
        }
        if (content instanceof String s) {
            return of(s);
        }
        if (content instanceof Content c) {
            return of(c);
        }
        if (content instanceof List<?> list) {
            if (list.isEmpty()) {
                return of(List.<Content>of());
            }
            if (list.getFirst() instanceof Content) {
                return of((List<Content>) list);
            }
        }
        throw new IllegalArgumentException("Unsupported turn content type: " + content.getClass());
    }

    /**
     * Checks whether this represents plain text.
     *
     * @return true if text is non-null.
     */
    public boolean isText() {
        return text != null;
    }

    /**
     * Checks whether this represents a list of content parts.
     *
     * @return true if parts is non-null.
     */
    public boolean isParts() {
        return parts != null;
    }

    /**
     * Returns the underlying value (either {@link String} or {@code List<Content>}).
     *
     * @return The underlying value.
     */
    public Object value() {
        return text != null ? text : parts;
    }
}
