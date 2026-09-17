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
import java.util.List;

/**
 * Configuration for an environment variable credential.
 *
 * @param value             The secret value string (write-only, never returned by server).
 * @param injectionLocation Optional injection location (e.g. "header", "query", "body").
 * @param trustedDomains    Optional list of domains allowed to receive this variable value (e.g. ["*.slack.com", "slack.com"]).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record EnvironmentVariableConfig(
    String value,
    @JsonProperty("injection_location") String injectionLocation,
    @JsonProperty("trusted_domains") List<String> trustedDomains
) {
    /**
     * Creates an EnvironmentVariableConfig with only a value.
     *
     * @param value The secret value.
     */
    public EnvironmentVariableConfig(String value) {
        this(value, null, null);
    }

    /**
     * Creates a builder for EnvironmentVariableConfig.
     *
     * @return A new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link EnvironmentVariableConfig}.
     */
    public static class Builder {
        private String value;
        private String injectionLocation;
        private List<String> trustedDomains;

        /** Creates a new Builder. */
        public Builder() {}

        /**
         * Sets the secret value.
         *
         * @param value The value.
         * @return This builder.
         */
        public Builder value(String value) {
            this.value = value;
            return this;
        }

        /**
         * Sets the injection location (e.g. "header", "query", "body").
         *
         * @param injectionLocation The injection location.
         * @return This builder.
         */
        public Builder injectionLocation(String injectionLocation) {
            this.injectionLocation = injectionLocation;
            return this;
        }

        /**
         * Sets the trusted domains list.
         *
         * @param trustedDomains List of domain patterns.
         * @return This builder.
         */
        public Builder trustedDomains(List<String> trustedDomains) {
            this.trustedDomains = trustedDomains;
            return this;
        }

        /**
         * Sets the trusted domains.
         *
         * @param trustedDomains Domain patterns.
         * @return This builder.
         */
        public Builder trustedDomains(String... trustedDomains) {
            this.trustedDomains = List.of(trustedDomains);
            return this;
        }

        /**
         * Builds the EnvironmentVariableConfig instance.
         *
         * @return The configured EnvironmentVariableConfig.
         */
        public EnvironmentVariableConfig build() {
            return new EnvironmentVariableConfig(value, injectionLocation, trustedDomains);
        }
    }
}
