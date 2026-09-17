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
 * Request payload for updating or rotating an existing Credential (PATCH /v1beta/credentials/{id}).
 *
 * @param bearerToken         Updated Bearer token configuration.
 * @param oauth2              Updated OAuth2 configuration.
 * @param environmentVariable Updated environment variable configuration.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record CredentialUpdate(
    @JsonProperty("bearer_token") BearerTokenConfig bearerToken,
    OAuth2Config oauth2,
    @JsonProperty("environment_variable") EnvironmentVariableConfig environmentVariable
) {
    /**
     * Creates a builder for CredentialUpdate.
     *
     * @return A new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a CredentialUpdate with a new Bearer token.
     *
     * @param bearerToken The updated bearer token configuration.
     * @return CredentialUpdate.
     */
    public static CredentialUpdate ofBearerToken(BearerTokenConfig bearerToken) {
        return new CredentialUpdate(bearerToken, null, null);
    }

    /**
     * Creates a CredentialUpdate with a new raw Bearer token string.
     *
     * @param token The updated bearer token secret string.
     * @return CredentialUpdate.
     */
    public static CredentialUpdate ofBearerToken(String token) {
        return new CredentialUpdate(new BearerTokenConfig(token), null, null);
    }

    /**
     * Creates a CredentialUpdate with updated OAuth2 credentials.
     *
     * @param oauth2 The updated OAuth2 configuration.
     * @return CredentialUpdate.
     */
    public static CredentialUpdate ofOAuth2(OAuth2Config oauth2) {
        return new CredentialUpdate(null, oauth2, null);
    }

    /**
     * Creates a CredentialUpdate with updated environment variable configuration.
     *
     * @param environmentVariable The updated environment variable configuration.
     * @return CredentialUpdate.
     */
    public static CredentialUpdate ofEnvironmentVariable(EnvironmentVariableConfig environmentVariable) {
        return new CredentialUpdate(null, null, environmentVariable);
    }

    /**
     * Creates a CredentialUpdate with an updated raw environment variable value string.
     *
     * @param value The updated environment variable secret value.
     * @return CredentialUpdate.
     */
    public static CredentialUpdate ofEnvironmentVariable(String value) {
        return new CredentialUpdate(null, null, new EnvironmentVariableConfig(value));
    }

    /**
     * Builder for {@link CredentialUpdate}.
     */
    public static class Builder {
        private BearerTokenConfig bearerToken;
        private OAuth2Config oauth2;
        private EnvironmentVariableConfig environmentVariable;

        /** Creates a new Builder. */
        public Builder() {}

        /**
         * Sets the bearer token configuration.
         *
         * @param bearerToken Bearer token configuration.
         * @return This builder.
         */
        public Builder bearerToken(BearerTokenConfig bearerToken) {
            this.bearerToken = bearerToken;
            return this;
        }

        /**
         * Sets the OAuth2 configuration.
         *
         * @param oauth2 OAuth2 configuration.
         * @return This builder.
         */
        public Builder oauth2(OAuth2Config oauth2) {
            this.oauth2 = oauth2;
            return this;
        }

        /**
         * Sets the environment variable configuration.
         *
         * @param environmentVariable Environment variable configuration.
         * @return This builder.
         */
        public Builder environmentVariable(EnvironmentVariableConfig environmentVariable) {
            this.environmentVariable = environmentVariable;
            return this;
        }

        /**
         * Builds the CredentialUpdate instance.
         *
         * @return The CredentialUpdate instance.
         */
        public CredentialUpdate build() {
            return new CredentialUpdate(bearerToken, oauth2, environmentVariable);
        }
    }
}
