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
import io.github.glaforge.gemini.interactions.model.deserializer.EnvironmentNetworkEgressAllowlistDeserializer;
import io.github.glaforge.gemini.interactions.model.deserializer.EnvironmentNetworkEgressAllowlistSerializer;
import java.util.List;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Outbound networking configuration for the sandbox allowlist.
 * <p>
 * Supports either a list of allowed outbound domain rules ({@link AllowlistEntry})
 * or the string {@code "disabled"} to block all network egress.
 * </p>
 *
 * @param allowlist List of allowed outbound domains (or null if disabled).
 * @param mode      Optional mode string, e.g. {@code "disabled"} when all network egress is blocked.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(using = EnvironmentNetworkEgressAllowlistSerializer.class)
@JsonDeserialize(using = EnvironmentNetworkEgressAllowlistDeserializer.class)
public record EnvironmentNetworkEgressAllowlist(
    List<AllowlistEntry> allowlist,
    String mode
) {
    /**
     * Creates an EnvironmentNetworkEgressAllowlist with a list of allowed outbound domains.
     *
     * @param allowlist List of allowed outbound domains.
     */
    public EnvironmentNetworkEgressAllowlist(List<AllowlistEntry> allowlist) {
        this(allowlist, null);
    }

    /**
     * Creates an EnvironmentNetworkEgressAllowlist from a list of allowed outbound domains.
     *
     * @param allowlist List of allowed outbound domains.
     * @return An EnvironmentNetworkEgressAllowlist instance.
     */
    public static EnvironmentNetworkEgressAllowlist of(List<AllowlistEntry> allowlist) {
        return new EnvironmentNetworkEgressAllowlist(allowlist, null);
    }

    /**
     * Creates an EnvironmentNetworkEgressAllowlist from one or more allowed outbound domain entries.
     *
     * @param entries Allowed outbound domain entries.
     * @return An EnvironmentNetworkEgressAllowlist instance.
     */
    public static EnvironmentNetworkEgressAllowlist of(AllowlistEntry... entries) {
        return new EnvironmentNetworkEgressAllowlist(entries != null ? List.of(entries) : null, null);
    }

    /**
     * Creates an EnvironmentNetworkEgressAllowlist with all network egress disabled
     * ({@code {"allowlist": "disabled"}}).
     *
     * @return An EnvironmentNetworkEgressAllowlist with egress disabled.
     */
    public static EnvironmentNetworkEgressAllowlist disabled() {
        return new EnvironmentNetworkEgressAllowlist(null, "disabled");
    }

    /**
     * Returns true if network egress is explicitly disabled ({@code "disabled"}).
     *
     * @return true if disabled.
     */
    public boolean isDisabled() {
        return "disabled".equalsIgnoreCase(mode);
    }
}
