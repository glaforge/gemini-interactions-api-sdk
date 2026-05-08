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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;
import java.util.Map;

/**
 * Represents the content of the response or input.
 * This is a sealed interface corresponding to the 'Content' schema in the OpenAPI spec.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Content.TextContent.class, name = "text"),
    @JsonSubTypes.Type(value = Content.ImageContent.class, name = "image"),
    @JsonSubTypes.Type(value = Content.AudioContent.class, name = "audio"),
    @JsonSubTypes.Type(value = Content.DocumentContent.class, name = "document"),
    @JsonSubTypes.Type(value = Content.VideoContent.class, name = "video")
})
public sealed interface Content permits
    Content.TextContent,
    Content.ImageContent,
    Content.AudioContent,
    Content.DocumentContent,
    Content.VideoContent {

    /**
     * Returns the type of content.
     *
     * @return The content type.
     */
    String type();

    /**
     * Resolution of the media.
     */
    public enum Resolution {
        /** Low resolution. */
        @JsonProperty("low") LOW,
        /** Medium resolution. */
        @JsonProperty("medium") MEDIUM,
        /** High resolution. */
        @JsonProperty("high") HIGH
    }

    /**
     * Status of the URL context retrieval.
     */
    public enum UrlContextStatus {
        /** Successful retrieval. */
        @JsonProperty("success") SUCCESS,
        /** Error during retrieval. */
        @JsonProperty("error") ERROR,
        /** Content behind paywall. */
        @JsonProperty("paywall") PAYWALL,
        /** Content deemed unsafe. */
        @JsonProperty("unsafe") UNSAFE
    }

    // --- Basic Media Types ---

    /**
     * Content containing text.
     *
     * @param type        The type of content (must be "text").
     * @param text        The text content.
     * @param annotations List of annotations for the text.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record TextContent(
        String type,
        String text,
        List<Annotation> annotations
    ) implements Content {
        /**
         * Creates a new TextContent with default type "text".
         *
         * @param text The text content.
         */
        public TextContent(String text) {
            this("text", text, null);
        }
    }

    /**
     * Annotation for text content.
     */
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Content.UrlCitation.class, name = "url_citation"),
        @JsonSubTypes.Type(value = Content.FileCitation.class, name = "file_citation"),
        @JsonSubTypes.Type(value = Content.PlaceCitation.class, name = "place_citation")
    })
    sealed interface Annotation permits UrlCitation, FileCitation, PlaceCitation {
        /**
         * Returns the type of annotation.
         *
         * @return The annotation type.
         */
        String type();
    }

    /**
     * URL citation annotation.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record UrlCitation(
        String type,
        @JsonProperty("start_index") Integer startIndex,
        @JsonProperty("end_index") Integer endIndex,
        String url,
        String title
    ) implements Annotation {}

    /**
     * File citation annotation.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record FileCitation(
        String type,
        @JsonProperty("start_index") Integer startIndex,
        @JsonProperty("end_index") Integer endIndex,
        @JsonProperty("document_uri") String documentUri,
        @JsonProperty("file_name") String fileName,
        String source
    ) implements Annotation {}

    /**
     * Place citation annotation.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record PlaceCitation(
        String type,
        @JsonProperty("start_index") Integer startIndex,
        @JsonProperty("end_index") Integer endIndex,
        @JsonProperty("place_id") String placeId,
        String name,
        String url,
        @JsonProperty("review_snippets") List<ReviewSnippet> reviewSnippets
    ) implements Annotation {}

    /**
     * Review snippet for place citation or Google Maps results.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ReviewSnippet(
        String title,
        String url,
        @JsonProperty("review_id") String reviewId
    ) {}

    /**
     * Content containing an image.
     *
     * @param type       The type of content (must be "image").
     * @param data       Base64 encoded image data.
     * @param uri        URI of the image.
     * @param mimeType   MIME type of the image.
     * @param resolution Resolution of the image.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ImageContent(
        String type,
        byte[] data,
        String uri,
        @JsonProperty("mime_type") String mimeType,
        Resolution resolution
    ) implements Content {
        /**
         * Creates a new ImageContent with default type "image".
         *
         * @param data     The base64 encoded image data.
         * @param mimeType The MIME type of the image.
         */
        public ImageContent(byte[] data, String mimeType) {
            this("image", data, null, mimeType, null);
        }
    }

    /**
     * Content containing audio.
     *
     * @param type     The type of content (must be "audio").
     * @param data     Base64 encoded audio data.
     * @param uri      URI of the audio.
     * @param mimeType MIME type of the audio (e.g. "audio/wav", "audio/mpeg", "audio/m4a", "audio/l16").
     * @param rate     Sample rate of the audio in Hertz.
     * @param channels Number of audio channels.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record AudioContent(
        String type,
        byte[] data,
        String uri,
        @JsonProperty("mime_type") String mimeType,
        Integer rate,
        Integer channels
    ) implements Content {
        /**
         * Creates a new AudioContent with default type "audio" and no rate or channels set.
         *
         * @param data     The base64 encoded audio data.
         * @param mimeType The MIME type of the audio.
         */
        public AudioContent(byte[] data, String mimeType) {
            this("audio", data, null, mimeType, null, null);
        }
    }

    /**
     * Content containing a document.
     *
     * @param type     The type of content (must be "document").
     * @param data     Base64 encoded document data.
     * @param uri      URI of the document.
     * @param mimeType MIME type of the document.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record DocumentContent(
        String type,
        byte[] data,
        String uri,
        @JsonProperty("mime_type") String mimeType
    ) implements Content {}

    /**
     * Content containing video.
     *
     * @param type       The type of content (must be "video").
     * @param data       Base64 encoded video data.
     * @param uri        URI of the video.
     * @param mimeType   MIME type of the video.
     * @param resolution Resolution of the video.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record VideoContent(
        String type,
        byte[] data,
        String uri,
        @JsonProperty("mime_type") String mimeType,
        Resolution resolution
    ) implements Content {}
}
