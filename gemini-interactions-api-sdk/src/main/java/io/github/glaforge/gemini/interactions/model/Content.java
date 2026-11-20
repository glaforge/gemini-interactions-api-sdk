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

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.HashMap;
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
    visible = true,
    defaultImpl = Content.UnknownContent.class
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
    Content.ThoughtContent,
    Content.UnknownContent {

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

        /**
         * Creates a new TextContent with text and annotations.
         *
         * @param text        The text content.
         * @param annotations List of annotations.
         */
        public TextContent(String text, List<Annotation> annotations) {
            this("text", text, annotations != null ? List.copyOf(annotations) : null);
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
        @JsonSubTypes.Type(value = Content.WordInfo.class, name = "word_info"),
        @JsonSubTypes.Type(value = Content.SpeechAnnotation.class, name = "speech_metadata")
    })
    sealed interface Annotation permits UrlCitation, FileCitation, PlaceCitation, WordInfo, SpeechAnnotation {
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
     * @param type           The type ("file_citation").
     * @param startIndex     The start index.
     * @param endIndex       The end index.
     * @param documentUri    The document URI.
     * @param fileName       The file name.
     * @param source         The source attributed for a portion of the text.
     * @param customMetadata User provided metadata about the retrieved context.
     * @param pageNumber     Page number of the cited document, if applicable.
     * @param mediaId        Media ID in case of image citations, if applicable.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record FileCitation(
        String type,
        @JsonProperty("start_index") Integer startIndex,
        @JsonProperty("end_index") Integer endIndex,
        @JsonProperty("document_uri") String documentUri,
        @JsonProperty("file_name") String fileName,
        String source,
        @JsonProperty("custom_metadata") Map<String, Object> customMetadata,
        @JsonProperty("page_number") Integer pageNumber,
        @JsonProperty("media_id") String mediaId
    ) implements Annotation {
        /**
         * Backward-compatible constructor without customMetadata, pageNumber, and mediaId.
         *
         * @param type        The type.
         * @param startIndex  The start index.
         * @param endIndex    The end index.
         * @param documentUri The document URI.
         * @param fileName    The file name.
         * @param source      The source.
         */
        public FileCitation(
            String type,
            Integer startIndex,
            Integer endIndex,
            String documentUri,
            String fileName,
            String source
        ) {
            this(type, startIndex, endIndex, documentUri, fileName, source, null, null, null);
        }
    }

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
     * Speech annotation for text content styling and speaker assignment.
     *
     * @param type       The annotation type ("speech_metadata").
     * @param startIndex Start of segment of response attributed to source.
     * @param endIndex   End of attributed segment, exclusive.
     * @param speaker    The speaker to associate with this turn.
     * @param style      Style instruction for speech synthesis.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record SpeechAnnotation(
        String type,
        @JsonProperty("start_index") Integer startIndex,
        @JsonProperty("end_index") Integer endIndex,
        String speaker,
        String style
    ) implements Annotation {
        /** Compact constructor establishing default type. */
        public SpeechAnnotation {
            if (type == null) {
                type = "speech_metadata";
            }
        }

        /**
         * Creates a speech annotation with speaker and style.
         *
         * @param speaker The speaker name.
         * @param style   The speech style instruction.
         */
        public SpeechAnnotation(String speaker, String style) {
            this("speech_metadata", null, null, speaker, style);
        }

        /**
         * Creates a speech annotation with style.
         *
         * @param style The speech style instruction.
         */
        public SpeechAnnotation(String style) {
            this("speech_metadata", null, null, null, style);
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
         * @param data     The image bytes.
         * @param mimeType The MIME type of the image.
         */
        public ImageContent(byte[] data, String mimeType) {
            this("image", data, null, mimeType, null);
        }

        /**
         * Creates a new ImageContent from bytes, MIME type, and resolution.
         *
         * @param data       The image bytes.
         * @param mimeType   The MIME type of the image.
         * @param resolution The media resolution.
         */
        public ImageContent(byte[] data, String mimeType, Resolution resolution) {
            this("image", data, null, mimeType, resolution);
        }

        /**
         * Creates a new ImageContent from a URI and MIME type.
         *
         * @param uri      The image URI.
         * @param mimeType The MIME type of the image.
         */
        public ImageContent(String uri, String mimeType) {
            this("image", null, uri, mimeType, null);
        }

        /**
         * Creates a new ImageContent from a URI, MIME type, and resolution.
         *
         * @param uri        The image URI.
         * @param mimeType   The MIME type of the image.
         * @param resolution The media resolution.
         */
        public ImageContent(String uri, String mimeType, Resolution resolution) {
            this("image", null, uri, mimeType, resolution);
        }

        @Override
        public String toString() {
            return "ImageContent[type=" + type + ", data=" + (data == null ? "null" : "<" + data.length + " bytes>") + ", uri=" + uri + ", mimeType=" + mimeType + ", resolution=" + resolution + "]";
        }
    }

    /**
     * Configuration for processing audio content.
     */
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Content.AudioStaticMediaProcessing.class, name = "static")
    })
    sealed interface AudioMediaProcessing permits Content.AudioStaticMediaProcessing {
        /**
         * Returns the type of audio processing.
         *
         * @return The processing type.
         */
        String type();
    }

    /**
     * Static media processing configuration for audio content.
     *
     * @param type        The type of audio processing (must be {@code "static"}).
     * @param startOffset Optional segment start time relative to the start of the audio stream (e.g. {@code "10.5s"}).
     * @param endOffset   Optional segment end time relative to the start of the audio stream (e.g. {@code "30s"}).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record AudioStaticMediaProcessing(
        String type,
        @JsonProperty("start_offset") String startOffset,
        @JsonProperty("end_offset") String endOffset
    ) implements AudioMediaProcessing {
        /** Compact constructor providing default type {@code "static"}. */
        public AudioStaticMediaProcessing {
            if (type == null) {
                type = "static";
            }
        }

        /**
         * Creates a new AudioStaticMediaProcessing configuration with start and end offsets.
         *
         * @param startOffset Optional segment start time (e.g. {@code "10.5s"}).
         * @param endOffset   Optional segment end time (e.g. {@code "30s"}).
         */
        public AudioStaticMediaProcessing(String startOffset, String endOffset) {
            this("static", startOffset, endOffset);
        }
    }

    /**
     * Content containing audio.
     *
     * @param type       The type of content (must be "audio").
     * @param data       Base64 encoded audio data.
     * @param uri        URI of the audio.
     * @param mimeType   MIME type of the audio (e.g. "audio/wav", "audio/mpeg", "audio/m4a", "audio/l16").
     * @param sampleRate Sample rate of the audio in Hertz.
     * @param channels   Number of audio channels.
     * @param processing Optional audio processing configuration ({@link AudioMediaProcessing} or preset string like {@code "static"}).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record AudioContent(
        String type,
        byte[] data,
        String uri,
        @JsonProperty("mime_type") String mimeType,
        @JsonProperty("sample_rate") @JsonAlias("rate") Integer sampleRate,
        Integer channels,
        AudioProcessingConfiguration processing
    ) implements Content {
        /** Compact constructor providing default type {@code "audio"}. */
        public AudioContent {
            if (type == null) {
                type = "audio";
            }
        }

        /**
         * Backward-compatible constructor without processing configuration.
         *
         * @param type       The type of content (must be "audio").
         * @param data       Base64 encoded audio data.
         * @param uri        URI of the audio.
         * @param mimeType   MIME type of the audio.
         * @param sampleRate Sample rate of the audio in Hertz.
         * @param channels   Number of audio channels.
         */
        public AudioContent(String type, byte[] data, String uri, String mimeType, Integer sampleRate, Integer channels) {
            this(type, data, uri, mimeType, sampleRate, channels, null);
        }

        /**
         * Creates a new AudioContent with default type "audio" and no sample rate or channels set.
         *
         * @param data     The base64 encoded audio data.
         * @param mimeType The MIME type of the audio.
         */
        public AudioContent(byte[] data, String mimeType) {
            this("audio", data, null, mimeType, null, null, null);
        }

        /**
         * Creates a new AudioContent with default type "audio", sample rate, and channels.
         *
         * @param data       The base64 encoded audio data.
         * @param mimeType   The MIME type of the audio.
         * @param sampleRate The sample rate in Hertz.
         * @param channels   The number of audio channels.
         */
        public AudioContent(byte[] data, String mimeType, Integer sampleRate, Integer channels) {
            this("audio", data, null, mimeType, sampleRate, channels, null);
        }

        /**
         * Creates a new AudioContent from bytes with an {@link AudioProcessingConfiguration}.
         *
         * @param data       The audio bytes.
         * @param mimeType   The MIME type of the audio.
         * @param processing The audio processing configuration.
         */
        public AudioContent(byte[] data, String mimeType, AudioProcessingConfiguration processing) {
            this("audio", data, null, mimeType, null, null, processing);
        }

        /**
         * Creates a new AudioContent from bytes with an {@link AudioMediaProcessing} object.
         *
         * @param data       The audio bytes.
         * @param mimeType   The MIME type of the audio.
         * @param processing The audio media processing configuration.
         */
        public AudioContent(byte[] data, String mimeType, AudioMediaProcessing processing) {
            this("audio", data, null, mimeType, null, null, processing != null ? AudioProcessingConfiguration.of(processing) : null);
        }

        /**
         * Creates a new AudioContent from bytes with a preset processing mode string (e.g. {@code "static"}).
         *
         * @param data       The audio bytes.
         * @param mimeType   The MIME type of the audio.
         * @param processing The preset processing mode string.
         */
        public AudioContent(byte[] data, String mimeType, String processing) {
            this("audio", data, null, mimeType, null, null, processing != null ? AudioProcessingConfiguration.of(processing) : null);
        }

        /**
         * Creates a new AudioContent from a URI and MIME type.
         *
         * @param uri      The audio URI.
         * @param mimeType The MIME type of the audio.
         */
        public AudioContent(String uri, String mimeType) {
            this("audio", null, uri, mimeType, null, null, null);
        }

        /**
         * Creates a new AudioContent from a URI, MIME type, and {@link AudioProcessingConfiguration}.
         *
         * @param uri        The audio URI.
         * @param mimeType   The MIME type of the audio.
         * @param processing The audio processing configuration.
         */
        public AudioContent(String uri, String mimeType, AudioProcessingConfiguration processing) {
            this("audio", null, uri, mimeType, null, null, processing);
        }

        /**
         * Creates a new AudioContent from a URI, MIME type, and {@link AudioMediaProcessing} object.
         *
         * @param uri        The audio URI.
         * @param mimeType   The MIME type of the audio.
         * @param processing The audio media processing configuration.
         */
        public AudioContent(String uri, String mimeType, AudioMediaProcessing processing) {
            this("audio", null, uri, mimeType, null, null, processing != null ? AudioProcessingConfiguration.of(processing) : null);
        }

        /**
         * Creates a new AudioContent from a URI, MIME type, and preset processing mode string (e.g. {@code "static"}).
         *
         * @param uri        The audio URI.
         * @param mimeType   The MIME type of the audio.
         * @param processing The preset processing mode string.
         */
        public AudioContent(String uri, String mimeType, String processing) {
            this("audio", null, uri, mimeType, null, null, processing != null ? AudioProcessingConfiguration.of(processing) : null);
        }

        /**
         * Returns the sample rate of the audio in Hertz.
         *
         * @return The sample rate.
         * @deprecated Use {@link #sampleRate()} instead.
         */
        @Deprecated
        @JsonIgnore
        public Integer rate() {
            return sampleRate;
        }

        @Override
        public String toString() {
            return "AudioContent[type=" + type + ", data=" + (data == null ? "null" : "<" + data.length + " bytes>") + ", uri=" + uri + ", mimeType=" + mimeType + ", sampleRate=" + sampleRate + ", channels=" + channels + ", processing=" + processing + "]";
        }
    }

    /**
     * Content layer to extract from a paginated document (PDF, Office documents, presentations).
     */
    public enum PageContentLayer {
        /** A rendered image of the page. */
        @JsonProperty("page_image") PAGE_IMAGE,
        /** The text of the page. */
        @JsonProperty("page_text") PAGE_TEXT
    }

    /**
     * Processing options for paginated documents, such as PDFs, Office documents, and presentations.
     *
     * @param startPage     Optional 1-indexed start page, inclusive.
     * @param endPage       Optional 1-indexed end page, inclusive (must be &gt;= {@code startPage} if {@code startPage} is set).
     * @param contentLayers Optional content layers to extract from a paginated document.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record PageProcessingOptions(
        @JsonProperty("start_page") Integer startPage,
        @JsonProperty("end_page") Integer endPage,
        @JsonProperty("content_layers") List<PageContentLayer> contentLayers
    ) {
        /**
         * Creates a new builder for {@link PageProcessingOptions}.
         *
         * @return A new builder.
         */
        public static Builder builder() {
            return new Builder();
        }

        /** Builder for {@link PageProcessingOptions}. */
        public static class Builder {
            private Integer startPage;
            private Integer endPage;
            private List<PageContentLayer> contentLayers;

            /** Creates a new Builder. */
            public Builder() {}

            /**
             * Sets the 1-indexed inclusive start page.
             *
             * @param startPage The start page.
             * @return This builder.
             */
            public Builder startPage(Integer startPage) { this.startPage = startPage; return this; }

            /**
             * Sets the 1-indexed inclusive end page.
             *
             * @param endPage The end page.
             * @return This builder.
             */
            public Builder endPage(Integer endPage) { this.endPage = endPage; return this; }

            /**
             * Sets the content layers to extract.
             *
             * @param contentLayers The list of page content layers.
             * @return This builder.
             */
            public Builder contentLayers(List<PageContentLayer> contentLayers) {
                this.contentLayers = contentLayers != null ? List.copyOf(contentLayers) : null;
                return this;
            }

            /**
             * Sets the content layers to extract.
             *
             * @param contentLayers The page content layers.
             * @return This builder.
             */
            public Builder contentLayers(PageContentLayer... contentLayers) {
                this.contentLayers = contentLayers != null ? List.of(contentLayers) : null;
                return this;
            }

            /**
             * Builds the {@link PageProcessingOptions}.
             *
             * @return A new {@link PageProcessingOptions}.
             */
            public PageProcessingOptions build() {
                return new PageProcessingOptions(startPage, endPage, contentLayers);
            }
        }
    }

    /**
     * Content layer to extract from a spreadsheet (.xlsx, .xls, .ods).
     */
    public enum SpreadsheetContentLayer {
        /** A rendered image of the sheet. */
        @JsonProperty("visual") VISUAL,
        /** Cell values, merges, and formats. */
        @JsonProperty("data") DATA,
        /** Formula expressions and their computed results. */
        @JsonProperty("formulas") FORMULAS
    }

    /**
     * Processing options for spreadsheets (.xlsx, .xls, .ods).
     *
     * @param visibleCells  Optional range of cells to extract in A1 notation (e.g. {@code "A1:D10"}).
     * @param sheetIndex    Optional 1-indexed index of the sheet to extract in workbook order.
     * @param contentLayers Optional content layers to extract from a spreadsheet.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record SpreadsheetProcessingOptions(
        @JsonProperty("visible_cells") String visibleCells,
        @JsonProperty("sheet_index") Integer sheetIndex,
        @JsonProperty("content_layers") List<SpreadsheetContentLayer> contentLayers
    ) {
        /**
         * Creates a new builder for {@link SpreadsheetProcessingOptions}.
         *
         * @return A new builder.
         */
        public static Builder builder() {
            return new Builder();
        }

        /** Builder for {@link SpreadsheetProcessingOptions}. */
        public static class Builder {
            private String visibleCells;
            private Integer sheetIndex;
            private List<SpreadsheetContentLayer> contentLayers;

            /** Creates a new Builder. */
            public Builder() {}

            /**
             * Sets the visible cell range in A1 notation (e.g. {@code "A1:D10"}).
             *
             * @param visibleCells The A1 cell range.
             * @return This builder.
             */
            public Builder visibleCells(String visibleCells) { this.visibleCells = visibleCells; return this; }

            /**
             * Sets the 1-indexed sheet index.
             *
             * @param sheetIndex The 1-indexed sheet index.
             * @return This builder.
             */
            public Builder sheetIndex(Integer sheetIndex) { this.sheetIndex = sheetIndex; return this; }

            /**
             * Sets the content layers to extract.
             *
             * @param contentLayers The list of spreadsheet content layers.
             * @return This builder.
             */
            public Builder contentLayers(List<SpreadsheetContentLayer> contentLayers) {
                this.contentLayers = contentLayers != null ? List.copyOf(contentLayers) : null;
                return this;
            }

            /**
             * Sets the content layers to extract.
             *
             * @param contentLayers The spreadsheet content layers.
             * @return This builder.
             */
            public Builder contentLayers(SpreadsheetContentLayer... contentLayers) {
                this.contentLayers = contentLayers != null ? List.of(contentLayers) : null;
                return this;
            }

            /**
             * Builds the {@link SpreadsheetProcessingOptions}.
             *
             * @return A new {@link SpreadsheetProcessingOptions}.
             */
            public SpreadsheetProcessingOptions build() {
                return new SpreadsheetProcessingOptions(visibleCells, sheetIndex, contentLayers);
            }
        }
    }

    /**
     * Configuration for processing document content.
     */
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = Content.DocumentStaticMediaProcessing.class, name = "static")
    })
    sealed interface DocumentMediaProcessing permits Content.DocumentStaticMediaProcessing {
        /**
         * Returns the type of document processing.
         *
         * @return The processing type.
         */
        String type();
    }

    /**
     * Static media processing configuration for document content.
     *
     * @param type               The type of document processing (must be {@code "static"}).
     * @param pageOptions        Options for paginated documents (PDF, Office documents, presentations).
     * @param spreadsheetOptions Options for spreadsheets (.xlsx, .xls, .ods).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record DocumentStaticMediaProcessing(
        String type,
        @JsonProperty("page_options") PageProcessingOptions pageOptions,
        @JsonProperty("spreadsheet_options") SpreadsheetProcessingOptions spreadsheetOptions
    ) implements DocumentMediaProcessing {
        /** Compact constructor providing default type {@code "static"}. */
        public DocumentStaticMediaProcessing {
            if (type == null) {
                type = "static";
            }
        }

        /**
         * Creates a DocumentStaticMediaProcessing configuration with page and/or spreadsheet options.
         *
         * @param pageOptions        Options for paginated documents.
         * @param spreadsheetOptions Options for spreadsheets.
         */
        public DocumentStaticMediaProcessing(PageProcessingOptions pageOptions, SpreadsheetProcessingOptions spreadsheetOptions) {
            this("static", pageOptions, spreadsheetOptions);
        }

        /**
         * Creates a DocumentStaticMediaProcessing configuration for paginated documents.
         *
         * @param pageOptions Options for paginated documents.
         * @return A new DocumentStaticMediaProcessing.
         */
        public static DocumentStaticMediaProcessing forPages(PageProcessingOptions pageOptions) {
            return new DocumentStaticMediaProcessing("static", pageOptions, null);
        }

        /**
         * Creates a DocumentStaticMediaProcessing configuration for spreadsheets.
         *
         * @param spreadsheetOptions Options for spreadsheets.
         * @return A new DocumentStaticMediaProcessing.
         */
        public static DocumentStaticMediaProcessing forSpreadsheet(SpreadsheetProcessingOptions spreadsheetOptions) {
            return new DocumentStaticMediaProcessing("static", null, spreadsheetOptions);
        }
    }

    /**
     * Content containing a document (PDF, CSV, Microsoft Office, or OpenDocument formats).
     *
     * @param type       The type of content (must be "document").
     * @param data       Base64 encoded document data.
     * @param uri        URI of the document.
     * @param mimeType   MIME type of the document (e.g. {@link #APPLICATION_PDF}, {@link #TEXT_CSV},
     *                   {@link #APPLICATION_DOCX}, {@link #APPLICATION_XLSX}, {@link #APPLICATION_PPTX}).
     * @param resolution Optional resolution of the media (applicable when {@link PageContentLayer#PAGE_IMAGE} is selected).
     * @param processing Optional document processing configuration ({@link DocumentMediaProcessing} or preset string like {@code "static"}).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record DocumentContent(
        String type,
        byte[] data,
        String uri,
        @JsonProperty("mime_type") String mimeType,
        Resolution resolution,
        DocumentProcessingConfiguration processing
    ) implements Content {
        /** PDF document format. */
        public static final String APPLICATION_PDF = "application/pdf";
        /** CSV document format. */
        public static final String TEXT_CSV = "text/csv";
        /** PPTX presentation format. */
        public static final String APPLICATION_PPTX = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        /** PPT presentation format. */
        public static final String APPLICATION_PPT = "application/vnd.ms-powerpoint";
        /** ODP presentation format. */
        public static final String APPLICATION_ODP = "application/vnd.oasis.opendocument.presentation";
        /** DOCX document format. */
        public static final String APPLICATION_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        /** DOC document format. */
        public static final String APPLICATION_DOC = "application/msword";
        /** DOTX document template format. */
        public static final String APPLICATION_DOTX = "application/vnd.openxmlformats-officedocument.wordprocessingml.template";
        /** ODT document format. */
        public static final String APPLICATION_ODT = "application/vnd.oasis.opendocument.text";
        /** XLSX spreadsheet format. */
        public static final String APPLICATION_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        /** XLS spreadsheet format. */
        public static final String APPLICATION_XLS = "application/vnd.ms-excel";
        /** ODS spreadsheet format. */
        public static final String APPLICATION_ODS = "application/vnd.oasis.opendocument.spreadsheet";

        /** Compact constructor providing default type {@code "document"}. */
        public DocumentContent {
            if (type == null) {
                type = "document";
            }
        }

        /**
         * Backward-compatible constructor without resolution and processing.
         *
         * @param type     The type of content (must be "document").
         * @param data     Base64 encoded document data.
         * @param uri      URI of the document.
         * @param mimeType MIME type of the document.
         */
        public DocumentContent(String type, byte[] data, String uri, String mimeType) {
            this(type, data, uri, mimeType, null, null);
        }

        /**
         * Creates a new DocumentContent from raw bytes with default type "document".
         *
         * @param data     The document data bytes.
         * @param mimeType The MIME type of the document.
         */
        public DocumentContent(byte[] data, String mimeType) {
            this("document", data, null, mimeType, null, null);
        }

        /**
         * Creates a new DocumentContent from raw bytes with resolution and {@link DocumentProcessingConfiguration}.
         *
         * @param data       The document data bytes.
         * @param mimeType   The MIME type of the document.
         * @param resolution Optional media resolution.
         * @param processing Optional document processing configuration.
         */
        public DocumentContent(byte[] data, String mimeType, Resolution resolution, DocumentProcessingConfiguration processing) {
            this("document", data, null, mimeType, resolution, processing);
        }

        /**
         * Creates a new DocumentContent from raw bytes with resolution and {@link DocumentMediaProcessing}.
         *
         * @param data       The document data bytes.
         * @param mimeType   The MIME type of the document.
         * @param resolution Optional media resolution.
         * @param processing Optional document media processing configuration.
         */
        public DocumentContent(byte[] data, String mimeType, Resolution resolution, DocumentMediaProcessing processing) {
            this("document", data, null, mimeType, resolution, processing != null ? DocumentProcessingConfiguration.of(processing) : null);
        }

        /**
         * Creates a new DocumentContent from raw bytes with resolution and a preset processing mode string (e.g. {@code "static"}).
         *
         * @param data       The document data bytes.
         * @param mimeType   The MIME type of the document.
         * @param resolution Optional media resolution.
         * @param processing Optional preset processing mode string.
         */
        public DocumentContent(byte[] data, String mimeType, Resolution resolution, String processing) {
            this("document", data, null, mimeType, resolution, processing != null ? DocumentProcessingConfiguration.of(processing) : null);
        }

        /**
         * Creates a new DocumentContent from a URI with default type "document".
         *
         * @param uri      The URI of the document.
         * @param mimeType The MIME type of the document.
         */
        public DocumentContent(String uri, String mimeType) {
            this("document", null, uri, mimeType, null, null);
        }

        /**
         * Creates a new DocumentContent from a URI with resolution and {@link DocumentProcessingConfiguration}.
         *
         * @param uri        The URI of the document.
         * @param mimeType   The MIME type of the document.
         * @param resolution Optional media resolution.
         * @param processing Optional document processing configuration.
         */
        public DocumentContent(String uri, String mimeType, Resolution resolution, DocumentProcessingConfiguration processing) {
            this("document", null, uri, mimeType, resolution, processing);
        }

        /**
         * Creates a new DocumentContent from a URI with resolution and {@link DocumentMediaProcessing}.
         *
         * @param uri        The URI of the document.
         * @param mimeType   The MIME type of the document.
         * @param resolution Optional media resolution.
         * @param processing Optional document media processing configuration.
         */
        public DocumentContent(String uri, String mimeType, Resolution resolution, DocumentMediaProcessing processing) {
            this("document", null, uri, mimeType, resolution, processing != null ? DocumentProcessingConfiguration.of(processing) : null);
        }

        /**
         * Creates a new DocumentContent from a URI with resolution and a preset processing mode string (e.g. {@code "static"}).
         *
         * @param uri        The URI of the document.
         * @param mimeType   The MIME type of the document.
         * @param resolution Optional media resolution.
         * @param processing Optional preset processing mode string.
         */
        public DocumentContent(String uri, String mimeType, Resolution resolution, String processing) {
            this("document", null, uri, mimeType, resolution, processing != null ? DocumentProcessingConfiguration.of(processing) : null);
        }

        @Override
        public String toString() {
            return "DocumentContent[type=" + type + ", data=" + (data == null ? "null" : "<" + data.length + " bytes>") + ", uri=" + uri + ", mimeType=" + mimeType + ", resolution=" + resolution + ", processing=" + processing + "]";
        }
    }

    /**
     * Content layer to extract from a video.
     */
    public enum VideoContentLayer {
        /** Frames of the video. */
        @JsonProperty("frames") FRAMES,
        /** Audio of the video. */
        @JsonProperty("audio") AUDIO,
        /** Transcript of the audio track. */
        @JsonProperty("transcript") TRANSCRIPT
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
     * @param type          The type of media processing (must be "static").
     * @param startOffset   Optional segment start time (e.g. "10.5s").
     * @param endOffset     Optional segment end time (e.g. "30s").
     * @param fps           Optional video frame-rate sampling density.
     * @param contentLayers Optional content layers to extract from the video ({@link VideoContentLayer}).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    record StaticMediaProcessing(
        String type,
        @JsonProperty("start_offset") String startOffset,
        @JsonProperty("end_offset") String endOffset,
        Double fps,
        @JsonProperty("content_layers") List<VideoContentLayer> contentLayers
    ) implements MediaProcessing {
        /** Compact constructor providing default type {@code "static"}. */
        public StaticMediaProcessing {
            if (type == null) {
                type = "static";
            }
        }

        /**
         * Backward-compatible constructor without contentLayers.
         *
         * @param type        The type of media processing (must be "static").
         * @param startOffset Optional segment start time (e.g. "10.5s").
         * @param endOffset   Optional segment end time (e.g. "30s").
         * @param fps         Optional video frame-rate sampling density.
         */
        public StaticMediaProcessing(String type, String startOffset, String endOffset, Double fps) {
            this(type, startOffset, endOffset, fps, null);
        }

        /**
         * Creates a new StaticMediaProcessing configuration.
         *
         * @param startOffset Optional segment start time (e.g. "10.5s").
         * @param endOffset   Optional segment end time (e.g. "30s").
         * @param fps         Optional video frame-rate sampling density.
         */
        public StaticMediaProcessing(String startOffset, String endOffset, Double fps) {
            this("static", startOffset, endOffset, fps, null);
        }

        /**
         * Creates a new StaticMediaProcessing configuration with video content layers.
         *
         * @param startOffset   Optional segment start time (e.g. "10.5s").
         * @param endOffset     Optional segment end time (e.g. "30s").
         * @param fps           Optional video frame-rate sampling density.
         * @param contentLayers Optional content layers to extract from the video.
         */
        public StaticMediaProcessing(String startOffset, String endOffset, Double fps, List<VideoContentLayer> contentLayers) {
            this("static", startOffset, endOffset, fps, contentLayers);
        }
    }

    /**
     * Content containing video.
     *
     * @param type       The type of content (must be "video").
     * @param data       Base64 encoded video data.
     * @param uri        URI of the video.
     * @param mimeType   MIME type of the video.
     * @param name       Optional user-defined name for this content block.
     * @param resolution Resolution of the video.
     * @param processing How the model processes this video for understanding (MediaProcessing or String like "static" / "agentic").
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record VideoContent(
        String type,
        byte[] data,
        String uri,
        @JsonProperty("mime_type") String mimeType,
        String name,
        Resolution resolution,
        MediaProcessingConfiguration processing
    ) implements Content {
        /** Compact constructor providing default type. */
        public VideoContent {
            if (type == null) {
                type = "video";
            }
        }

        /**
         * Creates a VideoContent instance with all parameters except name.
         *
         * @param type       The type of content (must be "video").
         * @param data       Base64 encoded video data.
         * @param uri        URI of the video.
         * @param mimeType   MIME type of the video.
         * @param resolution Resolution of the video.
         * @param processing Media processing configuration.
         */
        public VideoContent(String type, byte[] data, String uri, String mimeType, Resolution resolution, MediaProcessingConfiguration processing) {
            this(type, data, uri, mimeType, null, resolution, processing);
        }

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
            this(type, data, uri, mimeType, null, resolution, (MediaProcessingConfiguration) null);
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
            this(type, data, uri, mimeType, null, resolution, processing != null ? MediaProcessingConfiguration.of(processing) : null);
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
            this(type, data, uri, mimeType, null, resolution, processing != null ? MediaProcessingConfiguration.of(processing) : null);
        }

        @Override
        public String toString() {
            return "VideoContent[type=" + type + ", data=" + (data == null ? "null" : "<" + data.length + " bytes>") + ", uri=" + uri + ", mimeType=" + mimeType + ", name=" + name + ", resolution=" + resolution + ", processing=" + processing + "]";
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

    /**
     * Fallback content representation for unknown or newly introduced content types.
     *
     * @param type The content type identifier.
     * @param raw  Raw unrecognized properties in the content payload.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record UnknownContent(
        String type,
        @JsonAnySetter
        @JsonAnyGetter
        Map<String, Object> raw
    ) implements Content {
        /**
         * Creates an UnknownContent.
         *
         * @param type The content type identifier.
         * @param raw  Raw unrecognized properties in the content payload.
         */
        public UnknownContent {
            if (raw == null) {
                raw = new HashMap<>();
            }
        }
    }

    /**
     * Creates a text content with speech style delivery metadata.
     *
     * @param text  The verbatim text to synthesize.
     * @param style The speech style/delivery instruction.
     * @return A TextContent instance configured with speech metadata.
     */
    static TextContent speech(String text, String style) {
        return new TextContent(text, List.of(new SpeechAnnotation(style)));
    }

    /**
     * Creates a text content with a speaker name and speech style delivery metadata.
     *
     * @param text    The verbatim text to synthesize.
     * @param speaker The speaker name.
     * @param style   The speech style/delivery instruction.
     * @return A TextContent instance configured with speech metadata.
     */
    static TextContent speech(String text, String speaker, String style) {
        return new TextContent(text, List.of(new SpeechAnnotation(speaker, style)));
    }
}
