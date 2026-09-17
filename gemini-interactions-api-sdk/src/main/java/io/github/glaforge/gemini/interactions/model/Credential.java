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
import java.time.Instant;

/**
 * Represents a server-managed Credential resource.
 *
 * @param id                  The unique identifier for the credential.
 * @param type                The type of the credential.
 * @param status              The status of the credential.
 * @param createTime          Creation timestamp.
 * @param updateTime          Last update timestamp.
 * @param bearerToken         Bearer token configuration (write-only on creation).
 * @param oauth2              OAuth2 configuration (write-only secrets on creation).
 * @param environmentVariable Environment variable configuration (write-only value on creation).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Credential(
    String id,
    CredentialType type,
    Status status,
    @JsonProperty("create_time") Instant createTime,
    @JsonProperty("update_time") Instant updateTime,
    @JsonProperty("bearer_token") BearerTokenConfig bearerToken,
    OAuth2Config oauth2,
    @JsonProperty("environment_variable") EnvironmentVariableConfig environmentVariable
) {
    /**
     * Credential status.
     */
    public enum Status {
        /** Credential is active and valid for injection. */
        @JsonProperty("active") ACTIVE,
        /** Credential has expired. */
        @JsonProperty("expired") EXPIRED,
        /** Credential has been revoked. */
        @JsonProperty("revoked") REVOKED
    }

    /**
     * Creates a new Builder for a Credential.
     *
     * @return A new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a Bearer token credential.
     *
     * @param id          Optional credential ID.
     * @param bearerToken Bearer token configuration.
     * @return A Credential instance configured for bearer token.
     */
    public static Credential bearerToken(String id, BearerTokenConfig bearerToken) {
        return builder()
            .id(id)
            .type(CredentialType.BEARER_TOKEN)
            .bearerToken(bearerToken)
            .build();
    }

    /**
     * Creates an OAuth2 credential.
     *
     * @param id     Optional credential ID.
     * @param oauth2 OAuth2 configuration.
     * @return A Credential instance configured for OAuth2.
     */
    public static Credential oauth2(String id, OAuth2Config oauth2) {
        return builder()
            .id(id)
            .type(CredentialType.OAUTH2)
            .oauth2(oauth2)
            .build();
    }

    /**
     * Creates an environment variable credential.
     *
     * @param id                  Optional credential ID.
     * @param environmentVariable Environment variable configuration.
     * @return A Credential instance configured for environment variable.
     */
    public static Credential environmentVariable(String id, EnvironmentVariableConfig environmentVariable) {
        return builder()
            .id(id)
            .type(CredentialType.ENVIRONMENT_VARIABLE)
            .environmentVariable(environmentVariable)
            .build();
    }

    /**
     * Builder for {@link Credential}.
     */
    public static class Builder {
        private String id;
        private CredentialType type;
        private Status status;
        private Instant createTime;
        private Instant updateTime;
        private BearerTokenConfig bearerToken;
        private OAuth2Config oauth2;
        private EnvironmentVariableConfig environmentVariable;

        /** Creates a new Builder. */
        public Builder() {}

        /**
         * Sets the credential ID.
         *
         * @param id The credential ID.
         * @return This builder.
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the credential type.
         *
         * @param type The credential type.
         * @return This builder.
         */
        public Builder type(CredentialType type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the credential status.
         *
         * @param status The credential status.
         * @return This builder.
         */
        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        /**
         * Sets the creation timestamp.
         *
         * @param createTime The creation timestamp.
         * @return This builder.
         */
        public Builder createTime(Instant createTime) {
            this.createTime = createTime;
            return this;
        }

        /**
         * Sets the last update timestamp.
         *
         * @param updateTime The update timestamp.
         * @return This builder.
         */
        public Builder updateTime(Instant updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        /**
         * Sets the bearer token configuration. Automatically sets type to BEARER_TOKEN if not already set.
         *
         * @param bearerToken The bearer token config.
         * @return This builder.
         */
        public Builder bearerToken(BearerTokenConfig bearerToken) {
            this.bearerToken = bearerToken;
            if (this.type == null) {
                this.type = CredentialType.BEARER_TOKEN;
            }
            return this;
        }

        /**
         * Sets the OAuth2 configuration. Automatically sets type to OAUTH2 if not already set.
         *
         * @param oauth2 The OAuth2 config.
         * @return This builder.
         */
        public Builder oauth2(OAuth2Config oauth2) {
            this.oauth2 = oauth2;
            if (this.type == null) {
                this.type = CredentialType.OAUTH2;
            }
            return this;
        }

        /**
         * Sets the environment variable configuration. Automatically sets type to ENVIRONMENT_VARIABLE if not already set.
         *
         * @param environmentVariable The environment variable config.
         * @return This builder.
         */
        public Builder environmentVariable(EnvironmentVariableConfig environmentVariable) {
            this.environmentVariable = environmentVariable;
            if (this.type == null) {
                this.type = CredentialType.ENVIRONMENT_VARIABLE;
            }
            return this;
        }

        /**
         * Builds the Credential instance.
         *
         * @return The configured Credential.
         */
        public Credential build() {
            return new Credential(id, type, status, createTime, updateTime, bearerToken, oauth2, environmentVariable);
        }
    }
}
