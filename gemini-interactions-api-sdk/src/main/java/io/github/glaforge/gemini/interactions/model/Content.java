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
    @JsonSubTypes.Type(value = Content.VideoContent.class, name = "video"),
    @JsonSubTypes.Type(value = Content.ThoughtContent.class, name = "thought")
})
public sealed interface Content permits
    Content.TextContent,
    Content.ImageContent,
    Content.AudioContent,
    Content.DocumentContent,
    Content.VideoContent,
    Content.ThoughtContent {

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
        @JsonProperty("high") HIGH,
        /** Ultra high resolution. */
        @JsonProperty("ultra_high") ULTRA_HIGH
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
        @JsonSubTypes.Type(value = Content.PlaceCitation.class, name = "place_citation"),
        @JsonSubTypes.Type(value = Content.WordInfo.class, name = "word_info")
    })
    sealed interface Annotation permits UrlCitation, FileCitation, PlaceCitation, WordInfo {
        /**
         * Returns the type of annotation.
         *
         * @return The annotation type.
         */
        String type();
    }

    /**
     * URL citation annotation.
     *
     * @param type       The type.
     * @param startIndex The start index.
     * @param endIndex   The end index.
     * @param url        The URL.
     * @param title      The title.
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
     *
     * @param type        The type.
     * @param startIndex  The start index.
     * @param endIndex    The end index.
     * @param documentUri The document URI.
     * @param fileName    The file name.
     * @param source      The source.
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
     *
     * @param type           The type.
     * @param startIndex     The start index.
     * @param endIndex       The end index.
     * @param placeId        The place ID.
     * @param name           The name.
     * @param url            The URL.
     * @param reviewSnippets Review snippets.
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
     * Word-level ASR annotation for transcription output.
     *
     * @param type        The type ("word_info").
     * @param startIndex  Start of segment of response attributed to source.
     * @param endIndex    End of attributed segment, exclusive.
     * @param startOffset Start offset in time of the word relative to audio start.
     * @param endOffset   End offset in time of the word relative to audio start.
     * @param text        The transcribed word.
     * @param speaker     Optional speaker label (e.g. "spk_1").
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record WordInfo(
        String type,
        @JsonProperty("start_index") Integer startIndex,
        @JsonProperty("end_index") Integer endIndex,
        @JsonProperty("start_offset") String startOffset,
        @JsonProperty("end_offset") String endOffset,
        String text,
        String speaker
    ) implements Annotation {
        /** Compact constructor establishing default type. */
        public WordInfo {
            if (type == null) {
                type = "word_info";
            }
        }
    }

    /**
     * Review snippet for place citation or Google Maps results.
     *
     * @param title    The title.
     * @param url      The URL.
     * @param reviewId The review ID.
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

        @Override
        public String toString() {
            return "ImageContent[type=" + type + ", data=" + (data == null ? "null" : "<" + data.length + " bytes>") + ", uri=" + uri + ", mimeType=" + mimeType + ", resolution=" + resolution + "]";
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

        @Override
        public String toString() {
            return "AudioContent[type=" + type + ", data=" + (data == null ? "null" : "<" + data.length + " bytes>") + ", uri=" + uri + ", mimeType=" + mimeType + ", rate=" + rate + ", channels=" + channels + "]";
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
    ) implements Content {
        @Override
        public String toString() {
            return "DocumentContent[type=" + type + ", data=" + (data == null ? "null" : "<" + data.length + " bytes>") + ", uri=" + uri + ", mimeType=" + mimeType + "]";
        }
    }

    /**
     * Configuration for processing media (video).
     */
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Content.StaticMediaProcessing.class, name = "static")
    })
    sealed interface MediaProcessing permits Content.StaticMediaProcessing {
        /**
         * Returns the type of media processing.
         * @return The processing type.
         */
        String type();
    }

    /**
     * Static media processing configuration.
     *
     * @param type        The type of media processing (must be "static").
     * @param startOffset Optional segment start time (e.g. "10.5s").
     * @param endOffset   Optional segment end time (e.g. "30s").
     * @param fps         Optional video frame-rate sampling density.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record StaticMediaProcessing(
        String type,
        @JsonProperty("start_offset") String startOffset,
        @JsonProperty("end_offset") String endOffset,
        Double fps
    ) implements MediaProcessing {
        /**
         * Creates a new StaticMediaProcessing configuration.
         *
         * @param startOffset Optional segment start time (e.g. "10.5s").
         * @param endOffset   Optional segment end time (e.g. "30s").
         * @param fps         Optional video frame-rate sampling density.
         */
        public StaticMediaProcessing(String startOffset, String endOffset, Double fps) {
            this("static", startOffset, endOffset, fps);
        }
    }

    /**
     * Content containing video.
     *
     * @param type       The type of content (must be "video").
     * @param data       Base64 encoded video data.
     * @param uri        URI of the video.
     * @param mimeType   MIME type of the video.
     * @param resolution Resolution of the video.
     * @param processing How the model processes this video for understanding (MediaProcessing or String like "static" / "agentic").
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record VideoContent(
        String type,
        byte[] data,
        String uri,
        @JsonProperty("mime_type") String mimeType,
        Resolution resolution,
        MediaProcessingConfiguration processing
    ) implements Content {
        /**
         * Creates a VideoContent instance without processing configuration.
         *
         * @param type       The type of content (must be "video").
         * @param data       Base64 encoded video data.
         * @param uri        URI of the video.
         * @param mimeType   MIME type of the video.
         * @param resolution Resolution of the video.
         */
        public VideoContent(String type, byte[] data, String uri, String mimeType, Resolution resolution) {
            this(type, data, uri, mimeType, resolution, (MediaProcessingConfiguration) null);
        }

        /**
         * Creates a VideoContent instance with a preset processing mode string.
         *
         * @param type       The type of content (must be "video").
         * @param data       Base64 encoded video data.
         * @param uri        URI of the video.
         * @param mimeType   MIME type of the video.
         * @param resolution Resolution of the video.
         * @param processing Preset processing mode string.
         */
        public VideoContent(String type, byte[] data, String uri, String mimeType, Resolution resolution, String processing) {
            this(type, data, uri, mimeType, resolution, processing != null ? MediaProcessingConfiguration.of(processing) : null);
        }

        /**
         * Creates a VideoContent instance with a MediaProcessing configuration.
         *
         * @param type       The type of content (must be "video").
         * @param data       Base64 encoded video data.
         * @param uri        URI of the video.
         * @param mimeType   MIME type of the video.
         * @param resolution Resolution of the video.
         * @param processing MediaProcessing configuration.
         */
        public VideoContent(String type, byte[] data, String uri, String mimeType, Resolution resolution, MediaProcessing processing) {
            this(type, data, uri, mimeType, resolution, processing != null ? MediaProcessingConfiguration.of(processing) : null);
        }

        @Override
        public String toString() {
            return "VideoContent[type=" + type + ", data=" + (data == null ? "null" : "<" + data.length + " bytes>") + ", uri=" + uri + ", mimeType=" + mimeType + ", resolution=" + resolution + ", processing=" + processing + "]";
        }
    }

    /**
     * Content containing a thought.
     *
     * @param type      The type of content (must be "thought").
     * @param signature The thought signature.
     * @param summary   The thought summary.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ThoughtContent(
        String type,
        String signature,
        List<Content> summary
    ) implements Content {
        /**
         * Creates a new ThoughtContent with default type "thought".
         *
         * @param signature The thought signature.
         * @param summary   The thought summary.
         */
        public ThoughtContent(String signature, List<Content> summary) {
            this("thought", signature, summary);
        }
    }
}
