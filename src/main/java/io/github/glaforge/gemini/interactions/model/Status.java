/*
 * Copyright 2025 Google LLC
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
import java.util.List;
import java.util.Map;

/**
 * The Status type defines a logical error model.
 *
 * @param code    The status code.
 * @param message A developer-facing error message.
 * @param details A list of messages that carry the error details.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Status(
    Integer code,
    String message,
    List<Map<String, Object>> details
) {
    /**
     * Returns a new builder for Status.
     * @return a new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for Status.
     */
    public static class Builder {
        private Integer code;
        private String message;
        private List<Map<String, Object>> details;

        public Builder code(Integer code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder details(List<Map<String, Object>> details) {
            this.details = details;
            return this;
        }

        public Status build() {
            return new Status(code, message, details);
        }
    }
}
