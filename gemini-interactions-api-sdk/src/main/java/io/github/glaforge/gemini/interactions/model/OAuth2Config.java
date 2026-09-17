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
 * Configuration for an OAuth2 credential.
 *
 * @param clientId     The OAuth2 client ID.
 * @param clientSecret The OAuth2 client secret (write-only, never returned by server).
 * @param refreshToken The OAuth2 refresh token (write-only, never returned by server).
 * @param tokenUrl     The authorization token URL endpoint to fetch/refresh access tokens.
 * @param scopes       Optional list of requested OAuth2 scopes.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record OAuth2Config(
    @JsonProperty("client_id") String clientId,
    @JsonProperty("client_secret") String clientSecret,
    @JsonProperty("refresh_token") String refreshToken,
    @JsonProperty("token_url") String tokenUrl,
    List<String> scopes
) {
    /**
     * Creates a builder for OAuth2Config.
     *
     * @return A new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link OAuth2Config}.
     */
    public static class Builder {
        private String clientId;
        private String clientSecret;
        private String refreshToken;
        private String tokenUrl;
        private List<String> scopes;

        /** Creates a new Builder. */
        public Builder() {}

        /**
         * Sets the client ID.
         *
         * @param clientId The client ID.
         * @return This builder.
         */
        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        /**
         * Sets the client secret.
         *
         * @param clientSecret The client secret.
         * @return This builder.
         */
        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        /**
         * Sets the refresh token.
         *
         * @param refreshToken The refresh token.
         * @return This builder.
         */
        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        /**
         * Sets the token URL endpoint.
         *
         * @param tokenUrl The token URL endpoint.
         * @return This builder.
         */
        public Builder tokenUrl(String tokenUrl) {
            this.tokenUrl = tokenUrl;
            return this;
        }

        /**
         * Sets the OAuth2 scopes.
         *
         * @param scopes The scopes list.
         * @return This builder.
         */
        public Builder scopes(List<String> scopes) {
            this.scopes = scopes;
            return this;
        }

        /**
         * Sets the OAuth2 scopes.
         *
         * @param scopes The scopes.
         * @return This builder.
         */
        public Builder scopes(String... scopes) {
            this.scopes = List.of(scopes);
            return this;
        }

        /**
         * Builds the OAuth2Config instance.
         *
         * @return The configured OAuth2Config.
         */
        public OAuth2Config build() {
            return new OAuth2Config(clientId, clientSecret, refreshToken, tokenUrl, scopes);
        }
    }
}
