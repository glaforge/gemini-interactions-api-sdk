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
import io.github.glaforge.gemini.interactions.model.deserializer.AllowlistEntryDeserializer;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * A single domain allowlist rule with optional header injection and credential attachment.
 * <p>
 * Corresponds to {@code EgressRule} in the Gemini Interactions API specification.
 * </p>
 *
 * @param domain     Domain to allow outbound requests to. Supports wildcards (e.g. "*.googleapis.com").
 * @param transform  Headers to inject on all outbound requests matching this domain.
 * @param credential Optional. Reference to a server-managed Credential resource by ID.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = AllowlistEntryDeserializer.class)
public record AllowlistEntry(
    String domain,
    List<Map<String, String>> transform,
    String credential
) {
    /**
     * Backward-compatible constructor with only domain and transform.
     *
     * @param domain    The allowed domain.
     * @param transform Headers to inject on all outbound requests matching this domain.
     */
    public AllowlistEntry(String domain, List<Map<String, String>> transform) {
        this(domain, transform, null);
    }

    /**
     * Creates a new AllowlistEntry with only the domain specified.
     *
     * @param domain The allowed domain.
     */
    public AllowlistEntry(String domain) {
        this(domain, (List<Map<String, String>>) null, null);
    }

    /**
     * Creates an AllowlistEntry with only the domain specified.
     *
     * @param domain The allowed domain.
     * @return An AllowlistEntry with no header transforms.
     */
    public static AllowlistEntry of(String domain) {
        return new AllowlistEntry(domain);
    }

    /**
     * Creates an AllowlistEntry with a domain and server-managed credential ID.
     *
     * @param domain     The allowed domain.
     * @param credential Server-managed credential resource ID.
     * @return An AllowlistEntry with the specified credential.
     */
    public static AllowlistEntry of(String domain, String credential) {
        return new AllowlistEntry(domain, null, credential);
    }

    /**
     * Creates an AllowlistEntry with a single header transform mapping.
     *
     * @param domain    The allowed domain.
     * @param transform A single header mapping (e.g. Map.of("Authorization", "Bearer token")).
     * @return An AllowlistEntry with the specified transform.
     */
    public static AllowlistEntry of(String domain, Map<String, String> transform) {
        return new AllowlistEntry(domain, transform != null ? List.of(transform) : null, null);
    }

    /**
     * Creates an AllowlistEntry with a single header name and value.
     *
     * @param domain      The allowed domain.
     * @param headerName  Header name to inject (e.g. "Authorization").
     * @param headerValue Header value to inject (e.g. "Bearer ...").
     * @return An AllowlistEntry with the specified injected header.
     */
    public static AllowlistEntry of(String domain, String headerName, String headerValue) {
        return of(domain, headerName != null && headerValue != null ? Map.of(headerName, headerValue) : null);
    }

    /**
     * Creates an AllowlistEntry with a single header transform mapping and a credential.
     *
     * @param domain     The allowed domain.
     * @param transform  A single header mapping.
     * @param credential Server-managed credential resource ID.
     * @return An AllowlistEntry with the specified transform and credential.
     */
    public static AllowlistEntry of(String domain, Map<String, String> transform, String credential) {
        return new AllowlistEntry(domain, transform != null ? List.of(transform) : null, credential);
    }

    /**
     * Creates a new Builder for {@link AllowlistEntry}.
     *
     * @return A new Builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder for {@link AllowlistEntry}. */
    public static class Builder {
        private String domain;
        private List<Map<String, String>> transform;
        private String credential;

        /**
         * Creates a new Builder instance.
         */
        public Builder() {}

        /**
         * Sets the domain.
         *
         * @param domain The allowed domain.
         * @return This builder.
         */
        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        /**
         * Sets the header transforms list.
         *
         * @param transform List of header transforms.
         * @return This builder.
         */
        public Builder transform(List<Map<String, String>> transform) {
            this.transform = transform;
            return this;
        }

        /**
         * Sets a single header transform mapping.
         *
         * @param transform A single header mapping.
         * @return This builder.
         */
        public Builder transform(Map<String, String> transform) {
            this.transform = transform != null ? List.of(transform) : null;
            return this;
        }

        /**
         * Sets a single header to inject.
         *
         * @param headerName  Header name.
         * @param headerValue Header value.
         * @return This builder.
         */
        public Builder transform(String headerName, String headerValue) {
            this.transform = (headerName != null && headerValue != null) ? List.of(Map.of(headerName, headerValue)) : null;
            return this;
        }

        /**
         * Sets the server-managed credential ID.
         *
         * @param credential Credential ID.
         * @return This builder.
         */
        public Builder credential(String credential) {
            this.credential = credential;
            return this;
        }

        /**
         * Builds the AllowlistEntry.
         *
         * @return A new AllowlistEntry instance.
         */
        public AllowlistEntry build() {
            return new AllowlistEntry(domain, transform, credential);
        }
    }
}
