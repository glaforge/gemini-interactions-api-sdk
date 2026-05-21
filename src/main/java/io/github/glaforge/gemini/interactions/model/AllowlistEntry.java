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
 * A single domain allowlist rule with optional header injection.
 *
 * @param domain    Domain to allow outbound requests to. Supports wildcards (e.g. "*.googleapis.com").
 * @param transform Headers to inject on all outbound requests matching this domain.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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
        this(domain, null);
    }
}
