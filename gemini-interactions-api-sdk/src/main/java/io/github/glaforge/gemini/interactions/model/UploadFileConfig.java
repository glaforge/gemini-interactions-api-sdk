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

import java.nio.file.Path;

/**
 * Optional configuration metadata when uploading a file via the Gemini Files API.
 *
 * @param mimeType    Optional MIME type of the file (auto-detected from filename when uploading a {@link Path} if omitted).
 * @param displayName Optional human-readable display name (up to 512 characters).
 * @param name        Optional custom file resource name or ID (up to 40 lowercase alphanumeric characters or dashes, e.g. {@code "my-report"} or {@code "files/my-report"}).
 */
public record UploadFileConfig(
    String mimeType,
    String displayName,
    String name
) {
    /**
     * Creates a new builder for {@link UploadFileConfig}.
     *
     * @return A new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link UploadFileConfig}.
     */
    public static class Builder {
        private String mimeType;
        private String displayName;
        private String name;

        /** Creates a new Builder. */
        public Builder() {}

        /**
         * Sets the MIME type of the file.
         *
         * @param mimeType The MIME type (e.g. {@code "application/pdf"}).
         * @return This builder.
         */
        public Builder mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        /**
         * Sets the human-readable display name of the file (up to 512 characters).
         *
         * @param displayName The display name.
         * @return This builder.
         */
        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * Sets an optional custom resource name or ID for the file.
         *
         * @param name Custom resource name (e.g. {@code "files/my-file-123"} or {@code "my-file-123"}).
         * @return This builder.
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Builds the {@link UploadFileConfig}.
         *
         * @return A new {@link UploadFileConfig}.
         */
        public UploadFileConfig build() {
            return new UploadFileConfig(mimeType, displayName, name);
        }
    }
}
