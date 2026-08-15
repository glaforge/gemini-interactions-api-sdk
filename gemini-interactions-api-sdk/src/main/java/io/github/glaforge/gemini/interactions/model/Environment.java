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
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.annotation.JsonDeserialize;
import io.github.glaforge.gemini.interactions.model.deserializer.NetworkConfigDeserializer;

import java.time.Instant;
import java.util.List;

/**
 * An execution environment for an agent.
 *
 * @param id           The unique identifier of the environment.
 * @param created      The time at which the environment was created.
 * @param updated      The time at which the environment was last updated.
 * @param lastAccessed The time at which the environment was last accessed.
 * @param status       The status of the environment container (active, expired).
 * @param fileCount    The number of files in the environment.
 * @param sizeBytes    The total size of the environment files in bytes.
 * @param network      Network configuration for the environment.
 * @param sources      Sources mounted into the environment.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Environment(
    String id,
    Instant created,
    Instant updated,
    @JsonProperty("last_accessed") Instant lastAccessed,
    Status status,
    @JsonProperty("file_count") Long fileCount,
    @JsonProperty("size_bytes") Long sizeBytes,
    @JsonDeserialize(using = NetworkConfigDeserializer.class) Object network,
    List<Source> sources
) {
    /**
     * The status of the environment container.
     */
    public enum Status {
        /** Environment is active. */
        @JsonProperty("active") ACTIVE,
        /** Environment has expired. */
        @JsonProperty("expired") EXPIRED
    }

    /**
     * Returns a new builder for an Environment.
     * @return a new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder for {@link Environment}. */
    public static class Builder {
        /** Creates a new Builder. */
        public Builder() {}

        private String id;
        private Instant created;
        private Instant updated;
        private Instant lastAccessed;
        private Status status;
        private Long fileCount;
        private Long sizeBytes;
        private Object network;
        private List<Source> sources;

        /**
         * Sets the id.
         * @param id The id.
         * @return This builder.
         */
        public Builder id(String id) { this.id = id; return this; }

        /**
         * Sets the created timestamp.
         * @param created The created timestamp.
         * @return This builder.
         */
        public Builder created(Instant created) { this.created = created; return this; }

        /**
         * Sets the updated timestamp.
         * @param updated The updated timestamp.
         * @return This builder.
         */
        public Builder updated(Instant updated) { this.updated = updated; return this; }

        /**
         * Sets the last accessed timestamp.
         * @param lastAccessed The last accessed timestamp.
         * @return This builder.
         */
        public Builder lastAccessed(Instant lastAccessed) { this.lastAccessed = lastAccessed; return this; }

        /**
         * Sets the status.
         * @param status The status.
         * @return This builder.
         */
        public Builder status(Status status) { this.status = status; return this; }

        /**
         * Sets the file count.
         * @param fileCount The file count.
         * @return This builder.
         */
        public Builder fileCount(Long fileCount) { this.fileCount = fileCount; return this; }

        /**
         * Sets the size in bytes.
         * @param sizeBytes The size in bytes.
         * @return This builder.
         */
        public Builder sizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; return this; }

        /**
         * Sets the network configuration object.
         * @param network Network configuration (EnvironmentNetworkEgressAllowlist or "disabled").
         * @return This builder.
         */
        public Builder network(Object network) { this.network = network; return this; }

        /**
         * Sets the network configuration to an egress allowlist.
         * @param network Network egress allowlist.
         * @return This builder.
         */
        public Builder network(EnvironmentNetworkEgressAllowlist network) { this.network = network; return this; }

        /**
         * Sets the network configuration mode (e.g. "disabled").
         * @param network Network mode string.
         * @return This builder.
         */
        public Builder network(String network) { this.network = network; return this; }

        /**
         * Sets the sources.
         * @param sources Mounted sources list.
         * @return This builder.
         */
        public Builder sources(List<Source> sources) { this.sources = sources; return this; }

        /**
         * Builds the Environment.
         * @return The Environment.
         */
        public Environment build() {
            return new Environment(id, created, updated, lastAccessed, status, fileCount, sizeBytes, network, sources);
        }
    }
}
