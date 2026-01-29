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

package io.github.glaforge.gemini.schema;

import java.util.Arrays;
import java.util.Map;

/**
 * Schema for String types.
 * <p>
 * Example usage:
 * <pre>{@code
 * StringSchema schema = new StringSchema()
 *     .format(StringSchema.Format.DATE)
 *     .enumValues("2023-11-01", "2023-11-02");
 * }</pre>
 */
public class StringSchema extends Schema {
    private String[] enumValues;
    private String format;

    /** Initializes a new string schema. */
    public StringSchema() {
        super("string");
    }

    /**
     * Define the allowed enum values for this string.
     * @param values The allowed string values.
     * @return The StringSchema instance.
     */
    public StringSchema enumValues(String... values) {
        this.enumValues = values;
        return this;
    }

    /** Predefined formats for string schemas. */
    public enum Format {
        /** Date-time format. */
        DATE_TIME("date-time"),
        /** Date format. */
        DATE("date"),
        /** Time format. */
        TIME("time");

        private final String value;

        Format(String value) {
            this.value = value;
        }

        /**
         * Returns the string representation of the format.
         * @return the string representation of the format.
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * Define the format of the string (e.g., "date-time").
     * @param format The format string.
     * @return The StringSchema instance.
     */
    public StringSchema format(String format) {
        this.format = format;
        return this;
    }

    /**
     * Define the format of the string using a predefined enum.
     * @param format The format enum.
     * @return The StringSchema instance.
     */
    public StringSchema format(Format format) {
        this.format = format.getValue();
        return this;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        if (enumValues != null && enumValues.length > 0) {
            map.put("enum", Arrays.asList(enumValues));
        }
        if (format != null) {
            map.put("format", format);
        }
        return map;
    }
}
