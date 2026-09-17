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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for a Bearer token credential.
 *
 * @param token      The secret token string (write-only, never returned by server).
 * @param headerName Optional header name, defaults to "Authorization".
 * @param prefix     Optional token prefix, defaults to "Bearer". Pass "" for no prefix.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record BearerTokenConfig(
    String token,
    @JsonProperty("header_name") String headerName,
    String prefix
) {
    /**
     * Creates a BearerTokenConfig with default header name and prefix.
     *
     * @param token The secret bearer token.
     */
    public BearerTokenConfig(String token) {
        this(token, null, null);
    }

    /**
     * Creates a builder for BearerTokenConfig.
     *
     * @return A new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link BearerTokenConfig}.
     */
    public static class Builder {
        private String token;
        private String headerName;
        private String prefix;

        /** Creates a new Builder. */
        public Builder() {}

        /**
         * Sets the secret token.
         *
         * @param token The token.
         * @return This builder.
         */
        public Builder token(String token) {
            this.token = token;
            return this;
        }

        /**
         * Sets the header name (e.g. "Authorization" or "x-goog-api-key").
         *
         * @param headerName The header name.
         * @return This builder.
         */
        public Builder headerName(String headerName) {
            this.headerName = headerName;
            return this;
        }

        /**
         * Sets the prefix (e.g. "Bearer" or empty string for none).
         *
         * @param prefix The prefix.
         * @return This builder.
         */
        public Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * Builds the BearerTokenConfig instance.
         *
         * @return The configured BearerTokenConfig.
         */
        public BearerTokenConfig build() {
            return new BearerTokenConfig(token, headerName, prefix);
        }
    }
}
