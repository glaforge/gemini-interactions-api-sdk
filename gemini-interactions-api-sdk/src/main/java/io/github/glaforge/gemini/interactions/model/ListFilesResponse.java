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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Response containing a page of {@link GeminiFile} resources returned by the Gemini Files API.
 *
 * @param files         The list of files (never null; defaults to an empty list if omitted by the API).
 * @param nextPageToken A token that can be sent as {@code pageToken} in a subsequent {@code listFiles} call.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ListFilesResponse(
    List<GeminiFile> files,
    @JsonAlias("next_page_token") String nextPageToken
) {
    /** Compact constructor ensuring {@code files} is never null. */
    public ListFilesResponse {
        if (files == null) {
            files = List.of();
        }
    }
}
