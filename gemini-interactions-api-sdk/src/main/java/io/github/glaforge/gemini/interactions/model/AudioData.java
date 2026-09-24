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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 * Audio payload used for voice creation, preview, or replication.
 *
 * @param mimeType The IANA MIME type of the audio data (e.g. "audio/wav", "audio/mpeg").
 * @param data     The raw audio bytes, encoded in Base64.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record AudioData(
    @JsonProperty("mime_type") String mimeType,
    String data
) {
    /**
     * Creates AudioData from a MIME type and base64 encoded string.
     *
     * @param mimeType The MIME type.
     * @param data     The base64 encoded audio string.
     * @return An AudioData instance.
     */
    public static AudioData of(String mimeType, String data) {
        return new AudioData(mimeType, data);
    }

    /**
     * Creates AudioData from a MIME type and raw audio bytes.
     *
     * @param mimeType The MIME type.
     * @param bytes    The raw audio bytes.
     * @return An AudioData instance.
     */
    public static AudioData of(String mimeType, byte[] bytes) {
        String base64 = bytes != null ? Base64.getEncoder().encodeToString(bytes) : null;
        return new AudioData(mimeType, base64);
    }

    /**
     * Creates AudioData by reading audio bytes from a local file path.
     *
     * @param mimeType The MIME type (e.g. "audio/wav").
     * @param path     The path to the audio file.
     * @return An AudioData instance.
     * @throws IOException If reading the file fails.
     */
    public static AudioData fromFile(String mimeType, Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        return of(mimeType, bytes);
    }

    /**
     * Creates AudioData by reading audio bytes from an {@link InputStream}.
     *
     * @param mimeType The MIME type (e.g. "audio/wav").
     * @param is       The input stream to read.
     * @return An AudioData instance.
     * @throws IOException If reading the stream fails.
     */
    public static AudioData fromInputStream(String mimeType, InputStream is) throws IOException {
        byte[] bytes = is.readAllBytes();
        return of(mimeType, bytes);
    }

    /**
     * Decodes the base64 audio data into raw bytes.
     *
     * @return The decoded audio byte array, or null if data is null.
     */
    public byte[] decodedBytes() {
        if (data == null) {
            return null;
        }
        return Base64.getDecoder().decode(data);
    }
}
