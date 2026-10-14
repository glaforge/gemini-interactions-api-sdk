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
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for an environment that lives on the client connection rather
 * than in a server-managed sandbox.
 *
 * @param type The type (must be "local").
 * @param env  Environment variables to inject into the environment (literal strings or credential references).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record LocalEnvironmentConfig(
    String type,
    Map<String, Object> env
) {
    /**
     * Creates a new local EnvironmentConfig with the specified type.
     *
     * @param type The type (must be "local").
     */
    public LocalEnvironmentConfig(String type) {
        this(type, null);
    }

    /**
     * Creates a new local EnvironmentConfig.
     */
    public LocalEnvironmentConfig() {
        this("local", null);
    }

    /**
     * Returns a new builder for LocalEnvironmentConfig.
     *
     * @return A new Builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link LocalEnvironmentConfig}.
     */
    public static class Builder {
        private String type = "local";
        private Map<String, Object> env;

        /** Creates a new Builder. */
        public Builder() {}

        /**
         * Sets the environment type (defaults to "local").
         *
         * @param type The environment type.
         * @return This builder.
         */
        public Builder type(String type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the entire environment variable map.
         *
         * @param env Map of environment variables (strings or credential references).
         * @return This builder.
         */
        public Builder env(Map<String, ?> env) {
            if (env == null) {
                this.env = null;
            } else {
                this.env = new HashMap<>(env);
            }
            return this;
        }

        /**
         * Adds an environment variable with a literal string value.
         *
         * @param key   The variable name.
         * @param value The variable value.
         * @return This builder.
         */
        public Builder env(String key, String value) {
            if (this.env == null) {
                this.env = new HashMap<>();
            }
            this.env.put(key, value);
            return this;
        }

        /**
         * Adds an environment variable referencing a server-managed credential ID.
         *
         * @param key          The variable name.
         * @param credentialId The referenced credential ID.
         * @return This builder.
         */
        public Builder envCredential(String key, String credentialId) {
            if (this.env == null) {
                this.env = new HashMap<>();
            }
            this.env.put(key, CredentialReference.of(credentialId));
            return this;
        }

        /**
         * Builds the {@link LocalEnvironmentConfig}.
         *
         * @return A new LocalEnvironmentConfig instance.
         */
        public LocalEnvironmentConfig build() {
            return new LocalEnvironmentConfig(type, env);
        }
    }
}
