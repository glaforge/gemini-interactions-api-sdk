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
 * Schema for Number and Integer types.
 * <p>
 * Example usage:
 * <pre>{@code
 * num()
 *     .min(0)
 *     .max(100);
 * }</pre>
 * <p>
 * For integers:
 * <pre>{@code
 * integer()
 *     .min(1);
 * }</pre>
 */
public class NumberSchema extends Schema {
    private Double minimum;
    private Double maximum;

    /** Initializes a new number schema. */
    public NumberSchema() {
        super("number");
    }

    /**
     * Mark this number as an integer.
     * @return The NumberSchema instance.
     */
    public NumberSchema integer() {
        this.type = "integer";
        return this;
    }

    /**
     * Set the minimum value.
     * @param min The minimum value.
     * @return The NumberSchema instance.
     */
    public NumberSchema min(double min) {
        this.minimum = min;
        return this;
    }

    /**
     * Set the maximum value.
     * @param max The maximum value.
     * @return The NumberSchema instance.
     */
    public NumberSchema max(double max) {
        this.maximum = max;
        return this;
    }

    private Double[] enumValues;

    /**
     * Define the allowed enum values for this number.
     * @param values The allowed number values.
     * @return The NumberSchema instance.
     */
    public NumberSchema enumValues(double... values) {
        this.enumValues = new Double[values.length];
        for (int i = 0; i < values.length; i++) {
            this.enumValues[i] = values[i];
        }
        return this;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        if (enumValues != null && enumValues.length > 0) {
            map.put("enum", Arrays.asList(enumValues));
        }
        if (minimum != null) {
            map.put("minimum", minimum);
        }
        if (maximum != null) {
            map.put("maximum", maximum);
        }
        return map;
    }
}
