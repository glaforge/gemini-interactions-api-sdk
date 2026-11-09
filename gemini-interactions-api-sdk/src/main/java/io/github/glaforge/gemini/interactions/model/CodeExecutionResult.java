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
import io.github.glaforge.gemini.interactions.model.deserializer.CodeExecutionResultDeserializer;
import io.github.glaforge.gemini.interactions.model.deserializer.CodeExecutionResultSerializer;
import java.util.List;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Type-safe union record for code execution result output, representing either a plain text string
 * (such as standard output/error) or a rich list of multimodal {@link Content} parts (such as generated
 * images, charts, or documents).
 *
 * @param text     The plain text output, or null if represented as content parts.
 * @param contents The list of multimodal content parts, or null if represented as plain text.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(using = CodeExecutionResultSerializer.class)
@JsonDeserialize(using = CodeExecutionResultDeserializer.class)
public record CodeExecutionResult(
    String text,
    List<Content> contents
) {
    /**
     * Creates a CodeExecutionResult from a plain text string.
     *
     * @param text The text output string.
     * @return A CodeExecutionResult instance.
     */
    public static CodeExecutionResult of(String text) {
        return new CodeExecutionResult(text, null);
    }

    /**
     * Creates a CodeExecutionResult from a list of Content parts.
     *
     * @param contents The list of content parts.
     * @return A CodeExecutionResult instance.
     */
    public static CodeExecutionResult of(List<Content> contents) {
        return new CodeExecutionResult(null, contents != null ? List.copyOf(contents) : null);
    }

    /**
     * Creates a CodeExecutionResult from an array of Content parts.
     *
     * @param contents The content parts.
     * @return A CodeExecutionResult instance.
     */
    public static CodeExecutionResult of(Content... contents) {
        return new CodeExecutionResult(null, contents != null ? List.of(contents) : null);
    }

    /**
     * Creates a CodeExecutionResult from an arbitrary object (String, Content, List of Content, or CodeExecutionResult).
     *
     * @param result The result object.
     * @return A CodeExecutionResult instance, or null if result is null.
     */
    @SuppressWarnings("unchecked")
    public static CodeExecutionResult of(Object result) {
        if (result == null) {
            return null;
        }
        if (result instanceof CodeExecutionResult cer) {
            return cer;
        }
        if (result instanceof String s) {
            return of(s);
        }
        if (result instanceof Content c) {
            return of(List.of(c));
        }
        if (result instanceof List<?> list) {
            if (list.isEmpty()) {
                return of(List.<Content>of());
            }
            if (list.getFirst() instanceof Content) {
                return of((List<Content>) list);
            }
        }
        throw new IllegalArgumentException("Unsupported code execution result type: " + result.getClass());
    }

    /**
     * Checks whether this result represents plain text output.
     *
     * @return true if text is non-null.
     */
    public boolean isText() {
        return text != null;
    }

    /**
     * Checks whether this result represents a list of multimodal content parts.
     *
     * @return true if contents is non-null.
     */
    public boolean isContents() {
        return contents != null;
    }

    /**
     * Returns the underlying value (either {@link String} or {@code List<Content>}).
     *
     * @return The underlying value.
     */
    public Object value() {
        return text != null ? text : contents;
    }
}
