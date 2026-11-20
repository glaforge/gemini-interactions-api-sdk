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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The compute lifecycle mode for an execution environment.
 */
public enum EnvironmentMode {
    /** Managed on-demand compute environment. */
    ON_DEMAND("on_demand"),
    /** Self-hosted compute environment executed by a customer-managed sandbox daemon. */
    SELF_HOSTED("self_hosted");

    private final String value;

    EnvironmentMode(String value) {
        this.value = value;
    }

    /**
     * Returns the JSON wire string value for this mode.
     *
     * @return The wire value ({@code "on_demand"} or {@code "self_hosted"}).
     */
    @JsonValue
    public String value() {
        return value;
    }

    /**
     * Parses an {@link EnvironmentMode} from its JSON wire string value.
     *
     * @param value The wire string value.
     * @return The matching {@link EnvironmentMode}, or {@code null} if {@code value} is {@code null}.
     */
    @JsonCreator
    public static EnvironmentMode fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (EnvironmentMode mode : values()) {
            if (mode.value.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown EnvironmentMode: " + value);
    }
}
