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
 * Configuration records for interactions.
 */
public class Config {

    /** Private constructor to prevent instantiation. */
    private Config() {}

    /**
     * Configuration options for model generation.
     *
     * @param temperature       Controls randomness in generation.
     * @param topP             The maximum cumulative probability of tokens to consider when sampling.
     * @param seed             Seed for random number generation.
     * @param stopSequences    List of strings that stop generation.
     * @param toolChoice       Configuration for tool use.
     * @param thinkingLevel    Level of thinking to use for the model.
     * @param thinkingSummaries Configuration for thinking summaries.
     * @param maxOutputTokens  The maximum number of tokens to include in a candidate.
     * @param speechConfig     Type-safe speech generation configuration.
     * @param presencePenalty  The presence penalty.
     * @param frequencyPenalty The frequency penalty.
     * @param videoConfig      The video configuration.
     * @param transcriptionConfig Configuration for speech recognition (transcription).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GenerationConfig(
        Double temperature,
        @JsonProperty("top_p") Double topP,
        Integer seed,
        @JsonProperty("stop_sequences") List<String> stopSequences,
        @JsonProperty("tool_choice") Tool.ToolChoiceConfig toolChoice,
        @JsonProperty("thinking_level") ThinkingLevel thinkingLevel,
        @JsonProperty("thinking_summaries") ThinkingSummaries thinkingSummaries,
        @JsonProperty("max_output_tokens") Integer maxOutputTokens,
        @JsonProperty("speech_config") SpeechConfiguration speechConfig,
        @JsonProperty("presence_penalty") Double presencePenalty,
        @JsonProperty("frequency_penalty") Double frequencyPenalty,
        @JsonProperty("video_config") VideoConfig videoConfig,
        @JsonProperty("transcription_config") TranscriptionConfig transcriptionConfig
    ) {
        /**
         * Returns a new builder for GenerationConfig.
         * @return a new builder for GenerationConfig.
         */
        public static Builder builder() {
            return new Builder();
        }

        /** Builder for {@link GenerationConfig}. */
        public static class Builder {
            private Double temperature;
            private Double topP;
            private Integer seed;
            private List<String> stopSequences;
            private Tool.ToolChoiceConfig toolChoice;
            private ThinkingLevel thinkingLevel;
            private ThinkingSummaries thinkingSummaries;
            private Integer maxOutputTokens;
            private SpeechConfiguration speechConfig;
            private Double presencePenalty;
            private Double frequencyPenalty;
            private VideoConfig videoConfig;
            private TranscriptionConfig transcriptionConfig;

            /** Creates a new Builder. */
            public Builder() {}

            /**
             * Sets the temperature.
             *
             * @param temperature The temperature.
             * @return This builder.
             */
            public Builder temperature(Double temperature) { this.temperature = temperature; return this; }

            /**
             * Sets top_p.
             *
             * @param topP The top_p value.
             * @return This builder.
             */
            public Builder topP(Double topP) { this.topP = topP; return this; }

            /**
             * Sets the seed.
             *
             * @param seed The seed.
             * @return This builder.
             */
            public Builder seed(Integer seed) { this.seed = seed; return this; }

            /**
             * Sets the stop sequences.
             *
             * @param stopSequences The stop sequences.
             * @return This builder.
             */
            public Builder stopSequences(List<String> stopSequences) { this.stopSequences = stopSequences; return this; }

            /**
             * Sets the tool choice.
             *
             * @param toolChoice The tool choice.
             * @return This builder.
             */
            public Builder toolChoice(Tool.ToolChoiceConfig toolChoice) { this.toolChoice = toolChoice; return this; }

            /**
             * Sets the thinking level.
             *
             * @param thinkingLevel The thinking level.
             * @return This builder.
             */
            public Builder thinkingLevel(ThinkingLevel thinkingLevel) { this.thinkingLevel = thinkingLevel; return this; }

            /**
             * Sets the thinking summaries.
             *
             * @param thinkingSummaries The thinking summaries.
             * @return This builder.
             */
            public Builder thinkingSummaries(ThinkingSummaries thinkingSummaries) { this.thinkingSummaries = thinkingSummaries; return this; }

            /**
             * Sets max output tokens.
             *
             * @param maxOutputTokens Max output tokens.
             * @return This builder.
             */
            public Builder maxOutputTokens(Integer maxOutputTokens) { this.maxOutputTokens = maxOutputTokens; return this; }

            /**
             * Sets the speech configuration.
             *
             * @param speechConfig The speech configuration.
             * @return This builder.
             */
            public Builder speechConfig(SpeechConfiguration speechConfig) { this.speechConfig = speechConfig; return this; }

            /**
             * Sets single-speaker speech configs.
             *
             * @param speechConfig List of speech configs.
             * @return This builder.
             */
            public Builder speechConfig(List<SpeechConfig> speechConfig) { this.speechConfig = speechConfig != null ? SpeechConfiguration.of(speechConfig) : null; return this; }

            /**
             * Sets multi-speaker config.
             *
             * @param speakerConfig Speaker config.
             * @return This builder.
             */
            public Builder speechConfig(SpeakerConfig speakerConfig) { this.speechConfig = speakerConfig != null ? SpeechConfiguration.of(speakerConfig) : null; return this; }

            /**
             * Sets presence penalty.
             *
             * @param presencePenalty Presence penalty.
             * @return This builder.
             */
            public Builder presencePenalty(Double presencePenalty) { this.presencePenalty = presencePenalty; return this; }

            /**
             * Sets frequency penalty.
             *
             * @param frequencyPenalty Frequency penalty.
             * @return This builder.
             */
            public Builder frequencyPenalty(Double frequencyPenalty) { this.frequencyPenalty = frequencyPenalty; return this; }

            /**
             * Sets video config.
             *
             * @param videoConfig Video config.
             * @return This builder.
             */
            public Builder videoConfig(VideoConfig videoConfig) { this.videoConfig = videoConfig; return this; }

            /**
             * Sets transcription config.
             *
             * @param transcriptionConfig Transcription config.
             * @return This builder.
             */
            public Builder transcriptionConfig(TranscriptionConfig transcriptionConfig) { this.transcriptionConfig = transcriptionConfig; return this; }

            /**
             * Builds the GenerationConfig.
             *
             * @return The GenerationConfig.
             */
            public GenerationConfig build() {
                return new GenerationConfig(
                    temperature, topP, seed, stopSequences, toolChoice,
                    thinkingLevel, thinkingSummaries, maxOutputTokens, speechConfig,
                    presencePenalty, frequencyPenalty, videoConfig, transcriptionConfig
                );
            }
        }
    }

    /**
     * Level of thinking to use for the model.
     */
    public enum ThinkingLevel {
        /** Low thinking level. */
        @JsonProperty("low") LOW,
        /** High thinking level. */
        @JsonProperty("high") HIGH
    }

    /**
     * Configuration for thinking summaries.
     */
    public enum ThinkingSummaries {
        /** Auto thinking summaries. */
        @JsonProperty("auto") AUTO,
        /** No thinking summaries. */
        @JsonProperty("none") NONE
    }

    /**
     * Configuration for speech generation.
     *
     * @param voice    The voice to use.
     * @param language The language of the speech.
     * @param speaker  The speaker identity.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SpeechConfig(
        String voice,
        String language,
        String speaker
    ) {
        /**
         * Creates a new SpeechConfig with voice and language.
         *
         * @param voice    The voice to use.
         * @param language The language of the speech.
         */
        public SpeechConfig(String voice, String language) {
            this(voice, language, null);
        }
    }

    /**
     * Configuration for multi-speaker and speech generation.
     *
     * @param speakers Individual speaker configurations.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SpeakerConfig(
        List<SpeechConfig> speakers
    ) {}

    /**
     * Sealed interface for response formats.
     */
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = TextResponseFormat.class, name = "text"),
        @JsonSubTypes.Type(value = ImageResponseFormat.class, name = "image"),
        @JsonSubTypes.Type(value = AudioResponseFormat.class, name = "audio"),
        @JsonSubTypes.Type(value = VideoResponseFormat.class, name = "video")
    })
    public sealed interface ResponseFormat permits TextResponseFormat, ImageResponseFormat, AudioResponseFormat, VideoResponseFormat {
        /**
         * Returns the type.
         * @return the type.
         */
        String type();
    }

    /**
     * Configuration for text output format.
     *
     * @param type     The type of format.
     * @param mimeType The MIME type.
     * @param schema   The JSON schema.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TextResponseFormat(
        String type,
        @JsonProperty("mime_type") String mimeType,
        Map<String, Object> schema
    ) implements ResponseFormat {}

    /**
     * Configuration for image output format.
     *
     * @param type        The type of format.
     * @param mimeType    The MIME type.
     * @param delivery    The delivery method.
     * @param aspectRatio The aspect ratio.
     * @param imageSize   The image size.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ImageResponseFormat(
        String type,
        @JsonProperty("mime_type") String mimeType,
        String delivery,
        @JsonProperty("aspect_ratio") AspectRatio aspectRatio,
        @JsonProperty("image_size") ImageSize imageSize
    ) implements ResponseFormat {}

    /**
     * Configuration for audio output format.
     *
     * @param type       The type of format.
     * @param mimeType   The MIME type.
     * @param delivery   The delivery method.
     * @param sampleRate The sample rate.
     * @param bitRate    The bit rate.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AudioResponseFormat(
        String type,
        @JsonProperty("mime_type") String mimeType,
        String delivery,
        @JsonProperty("sample_rate") Integer sampleRate,
        @JsonProperty("bit_rate") Integer bitRate
    ) implements ResponseFormat {}

    /**
     * Configuration for video output format.
     *
     * @param type       The type of format ("video").
     * @param gcsUri     Cloud Storage URI to store the video output.
     * @param resolution Video output resolution ("1080p", "360p", "4k", "720p").
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VideoResponseFormat(
        String type,
        @JsonProperty("gcs_uri") String gcsUri,
        String resolution
    ) implements ResponseFormat {
        /** Creates a VideoResponseFormat with default type. */
        public VideoResponseFormat() {
            this("video", null, null);
        }
        /**
         * Creates a VideoResponseFormat with GCS URI and resolution.
         *
         * @param gcsUri     GCS URI.
         * @param resolution Output resolution.
         */
        public VideoResponseFormat(String gcsUri, String resolution) {
            this("video", gcsUri, resolution);
        }
    }

    /**
     * Aspect ratio for generated images.
     */
    public enum AspectRatio {
        /** 1:1. */
        @JsonProperty("1:1") RATIO_1_1,
        /** 2:3. */
        @JsonProperty("2:3") RATIO_2_3,
        /** 3:2. */
        @JsonProperty("3:2") RATIO_3_2,
        /** 3:4. */
        @JsonProperty("3:4") RATIO_3_4,
        /** 4:3. */
        @JsonProperty("4:3") RATIO_4_3,
        /** 4:5. */
        @JsonProperty("4:5") RATIO_4_5,
        /** 5:4. */
        @JsonProperty("5:4") RATIO_5_4,
        /** 9:16. */
        @JsonProperty("9:16") RATIO_9_16,
        /** 16:9. */
        @JsonProperty("16:9") RATIO_16_9,
        /** 21:9. */
        @JsonProperty("21:9") RATIO_21_9,
        /** 1:8. */
        @JsonProperty("1:8") RATIO_1_8,
        /** 8:1. */
        @JsonProperty("8:1") RATIO_8_1,
        /** 1:4. */
        @JsonProperty("1:4") RATIO_1_4,
        /** 4:1. */
        @JsonProperty("4:1") RATIO_4_1
    }

    /**
     * Size for generated images.
     */
    public enum ImageSize {
        /** 1K. */
        @JsonProperty("1K") SIZE_1K,
        /** 2K. */
        @JsonProperty("2K") SIZE_2K,
        /** 4K. */
        @JsonProperty("4K") SIZE_4K,
        /** 512. */
        @JsonProperty("512") SIZE_512
    }

    /**
     * Sealed interface for agent configurations.
     */
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = DynamicAgentConfig.class, name = "dynamic"),
        @JsonSubTypes.Type(value = DeepResearchAgentConfig.class, name = "deep-research"), // mapping key from spec
        @JsonSubTypes.Type(value = CodeMenderAgentConfig.class, name = "code_mender"),
        @JsonSubTypes.Type(value = AntigravityAgentConfig.class, name = "antigravity")
    })
    public sealed interface AgentConfig permits DynamicAgentConfig, DeepResearchAgentConfig, CodeMenderAgentConfig, AntigravityAgentConfig {
        /**
         * Returns the type of the agent.
         *
         * @return The agent type.
         */
        String type();

        /**
         * Returns the maximum total tokens for the agent run.
         *
         * @return the max total tokens.
         */
        Long maxTotalTokens();
    }

    /**
     * Configuration for dynamic agents.
     *
     * @param type           The type of agent (must be "dynamic").
     * @param maxTotalTokens Max total tokens.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DynamicAgentConfig(
        String type,
        @JsonProperty("max_total_tokens") Long maxTotalTokens
    ) implements AgentConfig {
        /** Creates a new DynamicAgentConfig with default type "dynamic". */
        public DynamicAgentConfig() {
            this("dynamic", null);
        }

        /** 
         * Creates a new DynamicAgentConfig with a token limit. 
         * @param maxTotalTokens Max total tokens.
         */
        public DynamicAgentConfig(Long maxTotalTokens) {
            this("dynamic", maxTotalTokens);
        }
    }

    /**
     * Configuration for visualization in Deep Research agent.
     */
    public enum Visualization {
        /** Off. */
        @JsonProperty("off") OFF,
        /** Auto. */
        @JsonProperty("auto") AUTO
    }

    /**
     * Configuration for deep research agents.
     *
     * @param type                  The type of agent (must be "deep-research").
     * @param maxTotalTokens        Max total tokens.
     * @param thinkingSummaries     Configuration for thinking summaries.
     * @param visualization         Configuration for visualization.
     * @param collaborativePlanning Whether human-in-the-loop planning is enabled.
     * @param enableBigqueryTool    Whether the BigQuery tool is enabled.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DeepResearchAgentConfig(
        String type,
        @JsonProperty("max_total_tokens") Long maxTotalTokens,
        @JsonProperty("thinking_summaries") ThinkingSummaries thinkingSummaries,
        @JsonProperty("visualization") Visualization visualization,
        @JsonProperty("collaborative_planning") Boolean collaborativePlanning,
        @JsonProperty("enable_bigquery_tool") Boolean enableBigqueryTool
    ) implements AgentConfig {
        /** Creates a new DeepResearchAgentConfig with default type and no summaries. */
        public DeepResearchAgentConfig() {
            this("deep-research", null, null, null, null, null);
        }
        /**
         * Creates a new DeepResearchAgentConfig with default type.
         *
         * @param thinkingSummaries The thinking summaries configuration.
         */
        public DeepResearchAgentConfig(ThinkingSummaries thinkingSummaries) {
            this("deep-research", null, thinkingSummaries, null, null, null);
        }



        /**
         * Returns a new builder for deep research agent config.
         * @return a new builder for deep research agent config.
         */
        public static Builder builder() { return new Builder(); }

        /** Builder for DeepResearchAgentConfig. */
        public static class Builder {
            private String type = "deep-research";
            private Long maxTotalTokens;
            private ThinkingSummaries thinkingSummaries;
            private Visualization visualization;
            private Boolean collaborativePlanning;
            private Boolean enableBigqueryTool;

            /** Creates a new Builder. */
            public Builder() {}

            /**
             * Sets max total tokens.
             *
             * @param maxTotalTokens max tokens.
             * @return This builder.
             */
            public Builder maxTotalTokens(Long maxTotalTokens) { this.maxTotalTokens = maxTotalTokens; return this; }

            /**
             * Sets the thinking summaries configuration.
             *
             * @param thinkingSummaries The thinking summaries configuration.
             * @return This builder.
             */
            public Builder thinkingSummaries(ThinkingSummaries thinkingSummaries) { this.thinkingSummaries = thinkingSummaries; return this; }

            /**
             * Sets the visualization configuration.
             *
             * @param visualization The visualization configuration.
             * @return This builder.
             */
            public Builder visualization(Visualization visualization) { this.visualization = visualization; return this; }

            /**
             * Sets the collaborative planning flag.
             *
             * @param collaborativePlanning Whether human-in-the-loop planning is enabled.
             * @return This builder.
             */
            public Builder collaborativePlanning(Boolean collaborativePlanning) { this.collaborativePlanning = collaborativePlanning; return this; }

            /**
             * Sets the enable BigQuery tool flag.
             *
             * @param enableBigqueryTool Whether the BigQuery tool is enabled.
             * @return This builder.
             */
            public Builder enableBigqueryTool(Boolean enableBigqueryTool) { this.enableBigqueryTool = enableBigqueryTool; return this; }

            /**
             * Builds the DeepResearchAgentConfig.
             *
             * @return The DeepResearchAgentConfig.
             */
            public DeepResearchAgentConfig build() {
                return new DeepResearchAgentConfig(type, maxTotalTokens, thinkingSummaries, visualization, collaborativePlanning, enableBigqueryTool);
            }
        }
    }

    /**
     * Configuration for the Code Mender agent runtime.
     *
     * @param type           The type ("code_mender").
     * @param maxTotalTokens Max total tokens.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CodeMenderAgentConfig(
        String type,
        @JsonProperty("max_total_tokens") Long maxTotalTokens
    ) implements AgentConfig {
        /** Creates a CodeMenderAgentConfig. */
        public CodeMenderAgentConfig() {
            this("code_mender", null);
        }
        /** 
         * Creates a CodeMenderAgentConfig with token limit. 
         * @param maxTotalTokens Max total tokens.
         */
        public CodeMenderAgentConfig(Long maxTotalTokens) {
            this("code_mender", maxTotalTokens);
        }
    }

    /**
     * Configuration for speech recognition (transcription).
     *
     * @param languageCodes        Optional BCP-47 language codes providing hints about the languages present in the audio.
     * @param languageHints        Deprecated: use {@link #languageCodes()}.
     * @param adaptationPhrases    Optional list of phrases to bias the ASR model towards (deprecated).
     * @param customVocabulary     Optional list of custom vocabulary phrases to bias speech recognition.
     * @param diarizationMode      Optional speaker diarization configuration (e.g. "speaker").
     * @param timestampGranularities Granularity of timestamps to include in transcription output (e.g. "word").
     */
    /**
     * Marker interface for transcription modes.
     */
    public sealed interface TranscriptionMode permits SmartTranscriptionMode, VerbatimTranscriptionMode {}

    /**
     * Smart transcription mode configuration.
     *
     * @param type Mode type (always "smart").
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SmartTranscriptionMode(
        String type
    ) implements TranscriptionMode {
        /** Creates a SmartTranscriptionMode. */
        public SmartTranscriptionMode() {
            this("smart");
        }
    }

    /**
     * Verbatim transcription mode configuration.
     *
     * @param diarizationMode        Optional speaker diarization configuration ("speaker").
     * @param timestampGranularities Granularity of timestamps ("word").
     * @param type                   Mode type (always "verbatim").
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VerbatimTranscriptionMode(
        @JsonProperty("diarization_mode") String diarizationMode,
        @JsonProperty("timestamp_granularities") List<String> timestampGranularities,
        String type
    ) implements TranscriptionMode {
        /** Creates a VerbatimTranscriptionMode with default type. */
        public VerbatimTranscriptionMode() {
            this(null, null, "verbatim");
        }
        /**
         * Creates a VerbatimTranscriptionMode with diarization and timestamp options.
         *
         * @param diarizationMode        Diarization mode.
         * @param timestampGranularities Timestamp granularities.
         */
        public VerbatimTranscriptionMode(String diarizationMode, List<String> timestampGranularities) {
            this(diarizationMode, timestampGranularities, "verbatim");
        }
    }

    /**
     * Configuration for speech recognition (transcription).
     *
     * @param languageCodes        Optional list of BCP-47 language codes providing hints about languages.
     * @param languageHints        Deprecated alias for languageCodes.
     * @param adaptationPhrases    Optional list of phrases to bias the ASR model towards (deprecated).
     * @param customVocabulary     Optional list of custom vocabulary phrases to bias speech recognition.
     * @param diarizationMode      Optional speaker diarization configuration (e.g. "speaker").
     * @param timestampGranularities Granularity of timestamps to include in transcription output (e.g. "word").
     * @param mode                 Optional transcription mode (smart, verbatim, or custom config).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TranscriptionConfig(
        @JsonProperty("language_codes") List<String> languageCodes,
        @Deprecated @JsonProperty("language_hints") List<String> languageHints,
        @Deprecated @JsonProperty("adaptation_phrases") List<String> adaptationPhrases,
        @JsonProperty("custom_vocabulary") List<String> customVocabulary,
        @JsonProperty("diarization_mode") String diarizationMode,
        @JsonProperty("timestamp_granularities") List<String> timestampGranularities,
        @JsonProperty("mode") TranscriptionModeConfiguration mode
    ) {
        /**
         * Creates a new TranscriptionConfig.
         *
         * @param languageCodes        Optional BCP-47 language codes.
         * @param adaptationPhrases    Optional adaptation phrases (deprecated).
         * @param customVocabulary     Optional custom vocabulary.
         * @param diarizationMode      Optional diarization mode.
         * @param timestampGranularities Optional timestamp granularities.
         */
        public TranscriptionConfig(List<String> languageCodes, List<String> adaptationPhrases, List<String> customVocabulary, String diarizationMode, List<String> timestampGranularities) {
            this(languageCodes, languageCodes, adaptationPhrases, customVocabulary, diarizationMode, timestampGranularities, null);
        }

        /**
         * Returns a new builder for TranscriptionConfig.
         * @return a new builder for TranscriptionConfig.
         */
        public static Builder builder() {
            return new Builder();
        }

        /** Builder for {@link TranscriptionConfig}. */
        public static class Builder {
            private List<String> languageCodes;
            private List<String> languageHints;
            private List<String> adaptationPhrases;
            private List<String> customVocabulary;
            private String diarizationMode;
            private List<String> timestampGranularities;
            private TranscriptionModeConfiguration mode;

            /** Creates a new Builder. */
            public Builder() {}

            /**
             * Sets language codes.
             * @param languageCodes BCP-47 language codes.
             * @return This builder.
             */
            public Builder languageCodes(List<String> languageCodes) { this.languageCodes = languageCodes; return this; }

            /**
             * Sets language hints.
             * @param languageHints Language hints.
             * @return This builder.
             * @deprecated Use {@link #languageCodes(List)} instead.
             */
            @Deprecated
            public Builder languageHints(List<String> languageHints) {
                this.languageHints = languageHints;
                if (this.languageCodes == null) {
                    this.languageCodes = languageHints;
                }
                return this;
            }

            /**
             * Sets adaptation phrases.
             * @param adaptationPhrases Adaptation phrases.
             * @return This builder.
             */
            public Builder adaptationPhrases(List<String> adaptationPhrases) { this.adaptationPhrases = adaptationPhrases; return this; }

            /**
             * Sets custom vocabulary.
             * @param customVocabulary Custom vocabulary.
             * @return This builder.
             */
            public Builder customVocabulary(List<String> customVocabulary) { this.customVocabulary = customVocabulary; return this; }

            /**
             * Sets diarization mode.
             * @param diarizationMode Diarization mode.
             * @return This builder.
             */
            public Builder diarizationMode(String diarizationMode) { this.diarizationMode = diarizationMode; return this; }

            /**
             * Sets timestamp granularities.
             * @param timestampGranularities Timestamp granularities.
             * @return This builder.
             */
            public Builder timestampGranularities(List<String> timestampGranularities) { this.timestampGranularities = timestampGranularities; return this; }

            /**
             * Sets transcription mode configuration.
             * @param mode Transcription mode configuration.
             * @return This builder.
             */
            public Builder mode(TranscriptionModeConfiguration mode) { this.mode = mode; return this; }

            /**
             * Sets transcription mode as a structured object.
             * @param mode Transcription mode object.
             * @return This builder.
             */
            public Builder mode(TranscriptionMode mode) { this.mode = TranscriptionModeConfiguration.of(mode); return this; }

            /**
             * Sets transcription mode preset string.
             * @param preset Preset string ("smart", "verbatim").
             * @return This builder.
             */
            public Builder mode(String preset) { this.mode = TranscriptionModeConfiguration.of(preset); return this; }

            /**
             * Builds the TranscriptionConfig.
             * @return The TranscriptionConfig.
             */
            public TranscriptionConfig build() {
                List<String> codes = languageCodes != null ? languageCodes : languageHints;
                return new TranscriptionConfig(codes, languageHints != null ? languageHints : codes, adaptationPhrases, customVocabulary, diarizationMode, timestampGranularities, mode);
            }
        }
    }

    /**
     * Configuration for the Antigravity agent runtime.
     *
     * @param type           The type ("antigravity").
     * @param maxTotalTokens Max total tokens.
     * @param model          The model to use for agent reasoning.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AntigravityAgentConfig(
        String type,
        @JsonProperty("max_total_tokens") Long maxTotalTokens,
        String model
    ) implements AgentConfig {
        /** Creates a AntigravityAgentConfig. */
        public AntigravityAgentConfig() {
            this("antigravity", null, null);
        }
        /** 
         * Creates a AntigravityAgentConfig with token limit. 
         * @param maxTotalTokens Max total tokens.
         */
        public AntigravityAgentConfig(Long maxTotalTokens) {
            this("antigravity", maxTotalTokens, null);
        }
        /** 
         * Creates a AntigravityAgentConfig with token limit and model. 
         * @param maxTotalTokens Max total tokens.
         * @param model Model name.
         */
        public AntigravityAgentConfig(Long maxTotalTokens, String model) {
            this("antigravity", maxTotalTokens, model);
        }
    }
}
