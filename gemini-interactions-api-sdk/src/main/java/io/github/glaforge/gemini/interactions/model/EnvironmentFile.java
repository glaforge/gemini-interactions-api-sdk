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

import java.time.Instant;

/**
 * Metadata for a file or directory within an execution environment.
 *
 * @param name      The name of the file or directory (e.g. "main.py" or "src").
 * @param path      The full relative path within the environment (e.g. "workspace/src/main.py").
 * @param type      The type of the entry ("file" or "directory").
 * @param sizeBytes The size of the file/directory in bytes.
 * @param mimeType  The MIME type of the file.
 * @param created   The creation timestamp.
 * @param modified  The modification timestamp.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EnvironmentFile(
    String name,
    String path,
    String type,
    @JsonProperty("size_bytes") Long sizeBytes,
    @JsonProperty("mime_type") String mimeType,
    Instant created,
    Instant modified
) {
    /**
     * Returns true if this entry represents a directory.
     *
     * @return true if type is "directory".
     */
    public boolean isDirectory() {
        return "directory".equalsIgnoreCase(type);
    }

    /**
     * Returns true if this entry represents a regular file.
     *
     * @return true if type is "file".
     */
    public boolean isFile() {
        return "file".equalsIgnoreCase(type);
    }
}
