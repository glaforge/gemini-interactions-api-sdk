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
 * A single domain allowlist rule with optional header injection.
 *
 * @param domain    Domain to allow outbound requests to. Supports wildcards (e.g. "*.googleapis.com").
 * @param transform Headers to inject on all outbound requests matching this domain.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = AllowlistEntryDeserializer.class)
public record AllowlistEntry(
    String domain,
    List<Map<String, String>> transform
) {
    /**
     * Creates a new AllowlistEntry with only the domain specified.
     *
     * @param domain The allowed domain.
     */
    public AllowlistEntry(String domain) {
        this(domain, (List<Map<String, String>>) null);
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
     * Creates an AllowlistEntry with a single header transform mapping.
     *
     * @param domain    The allowed domain.
     * @param transform A single header mapping (e.g. Map.of("Authorization", "Bearer token")).
     * @return An AllowlistEntry with the specified transform.
     */
    public static AllowlistEntry of(String domain, Map<String, String> transform) {
        return new AllowlistEntry(domain, transform != null ? List.of(transform) : null);
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
}
