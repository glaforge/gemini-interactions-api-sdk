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
import java.time.Instant;
import java.util.List;

/**
 * A voice persona that can be synthesized using supported Gemini TTS models.
 *
 * @param id          Unique identifier for the voice (e.g. {@code "voice_abc123"}), assigned when stored.
 * @param key         Self-contained encrypted key for an unstored voice (e.g. {@code "voicekey_..."}).
 * @param name        The resource name of the voice (e.g. {@code "voices/voice_abc123"}).
 * @param type        The type of voice ({@link VoiceType#PROMPTED}, {@link VoiceType#REPLICATED}, or {@link VoiceType#PREBUILT}).
 * @param displayName Human-readable display name for the voice.
 * @param description Natural-language description of character or tone.
 * @param model       The model used to design or replicate the voice.
 * @param gender      Perceived gender presentation ("female", "male", "neutral").
 * @param pitch       Voice pitch classification ({@link Pitch}).
 * @param languageCode Primary BCP-47 language tag (e.g. "en-US", "fr-FR").
 * @param accent      Regional or dialect accent (e.g. "en-scotland", "fr-be").
 * @param persona     Intended persona or character archetype (e.g. "Warm, Friendly", "Narrator").
 * @param regionCode  ISO 3166-1 alpha-2 or UN M.49 geographic region code (e.g. "US", "GB", "001").
 * @param context     Target domains or interaction contexts (e.g. ["Audiobook", "Conversational"]).
 * @param sampleAudio Output-only sample audio preview generated for prompted voices.
 * @param prompted    Parameters for prompted voice generation.
 * @param replicated  Input-only parameters for replicated voice generation.
 * @param usage       Token usage statistics for voice creation.
 * @param createTime  Creation timestamp.
 * @param expireTime  Expiration timestamp.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Voice(
    String id,
    String key,
    String name,
    VoiceType type,
    @JsonProperty("display_name") String displayName,
    String description,
    String model,
    String gender,
    Pitch pitch,
    @JsonProperty("language_code") String languageCode,
    String accent,
    String persona,
    @JsonProperty("region_code") String regionCode,
    List<String> context,
    @JsonProperty("sample_audio") AudioData sampleAudio,
    PromptedVoice prompted,
    ReplicatedVoice replicated,
    Interaction.Usage usage,
    @JsonProperty("create_time") Instant createTime,
    @JsonProperty("expire_time") Instant expireTime
) {
    /**
     * Creates a new Builder for a Voice.
     *
     * @return A new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a prompted Voice (Voice Design) from a text prompt.
     *
     * @param prompt The natural-language prompt describing the desired voice.
     * @return A Voice instance configured for prompted voice design.
     */
    public static Voice prompted(String prompt) {
        return builder()
            .type(VoiceType.PROMPTED)
            .prompted(PromptedVoice.of(prompt))
            .build();
    }

    /**
     * Creates a prompted Voice (Voice Design) with model, display name, and prompt.
     *
     * @param model       The TTS model to use.
     * @param displayName The human-readable name.
     * @param prompt      The natural-language prompt describing the voice persona.
     * @return A Voice instance configured for prompted voice design.
     */
    public static Voice prompted(String model, String displayName, String prompt) {
        return builder()
            .model(model)
            .displayName(displayName)
            .type(VoiceType.PROMPTED)
            .prompted(PromptedVoice.of(prompt))
            .build();
    }

    /**
     * Creates a replicated Voice (Voice Replication) from source and consent audio.
     *
     * @param sourceAudio  The reference audio sample of the voice.
     * @param consentAudio The recording reciting the consent statement.
     * @return A Voice instance configured for voice replication.
     */
    public static Voice replicated(AudioData sourceAudio, AudioData consentAudio) {
        return builder()
            .type(VoiceType.REPLICATED)
            .replicated(ReplicatedVoice.of(sourceAudio, consentAudio))
            .build();
    }

    /**
     * Creates a replicated Voice with model, display name, source audio, and consent audio.
     *
     * @param model        The TTS model.
     * @param displayName  The human-readable name.
     * @param sourceAudio  The reference audio sample.
     * @param consentAudio The consent audio recording.
     * @return A Voice instance configured for voice replication.
     */
    public static Voice replicated(String model, String displayName, AudioData sourceAudio, AudioData consentAudio) {
        return builder()
            .model(model)
            .displayName(displayName)
            .type(VoiceType.REPLICATED)
            .replicated(ReplicatedVoice.of(sourceAudio, consentAudio))
            .build();
    }

    /**
     * Builder for {@link Voice}.
     */
    public static class Builder {
        private String id;
        private String key;
        private String name;
        private VoiceType type;
        private String displayName;
        private String description;
        private String model;
        private String gender;
        private Pitch pitch;
        private String languageCode;
        private String accent;
        private String persona;
        private String regionCode;
        private List<String> context;
        private AudioData sampleAudio;
        private PromptedVoice prompted;
        private ReplicatedVoice replicated;
        private Interaction.Usage usage;
        private Instant createTime;
        private Instant expireTime;

        /** Creates a new Builder. */
        public Builder() {}

        /**
         * Sets the voice ID.
         *
         * @param id The voice ID.
         * @return This builder.
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the client-managed voice key.
         *
         * @param key The voice key.
         * @return This builder.
         */
        public Builder key(String key) {
            this.key = key;
            return this;
        }

        /**
         * Sets the resource name.
         *
         * @param name The resource name.
         * @return This builder.
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the voice type.
         *
         * @param type The voice type.
         * @return This builder.
         */
        public Builder type(VoiceType type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the display name.
         *
         * @param displayName The display name.
         * @return This builder.
         */
        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * Sets the description.
         *
         * @param description The description.
         * @return This builder.
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the model.
         *
         * @param model The model.
         * @return This builder.
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * Sets the gender.
         *
         * @param gender The gender.
         * @return This builder.
         */
        public Builder gender(String gender) {
            this.gender = gender;
            return this;
        }

        /**
         * Sets the pitch.
         *
         * @param pitch The pitch.
         * @return This builder.
         */
        public Builder pitch(Pitch pitch) {
            this.pitch = pitch;
            return this;
        }

        /**
         * Sets the language code.
         *
         * @param languageCode The language code.
         * @return This builder.
         */
        public Builder languageCode(String languageCode) {
            this.languageCode = languageCode;
            return this;
        }

        /**
         * Sets the accent.
         *
         * @param accent The accent.
         * @return This builder.
         */
        public Builder accent(String accent) {
            this.accent = accent;
            return this;
        }

        /**
         * Sets the persona.
         *
         * @param persona The persona.
         * @return This builder.
         */
        public Builder persona(String persona) {
            this.persona = persona;
            return this;
        }

        /**
         * Sets the region code.
         *
         * @param regionCode The region code.
         * @return This builder.
         */
        public Builder regionCode(String regionCode) {
            this.regionCode = regionCode;
            return this;
        }

        /**
         * Sets the domain contexts.
         *
         * @param context The contexts list.
         * @return This builder.
         */
        public Builder context(List<String> context) {
            this.context = context;
            return this;
        }

        /**
         * Sets the domain contexts.
         *
         * @param context The contexts.
         * @return This builder.
         */
        public Builder context(String... context) {
            this.context = context != null ? List.of(context) : null;
            return this;
        }

        /**
         * Sets the sample audio preview.
         *
         * @param sampleAudio The sample audio.
         * @return This builder.
         */
        public Builder sampleAudio(AudioData sampleAudio) {
            this.sampleAudio = sampleAudio;
            return this;
        }

        /**
         * Sets prompted voice configuration.
         *
         * @param prompted The prompted voice config.
         * @return This builder.
         */
        public Builder prompted(PromptedVoice prompted) {
            this.prompted = prompted;
            return this;
        }

        /**
         * Sets prompted voice configuration from prompt text.
         *
         * @param input The prompt text.
         * @return This builder.
         */
        public Builder prompted(String input) {
            this.prompted = input != null ? PromptedVoice.of(input) : null;
            return this;
        }

        /**
         * Sets replicated voice configuration.
         *
         * @param replicated The replicated voice config.
         * @return This builder.
         */
        public Builder replicated(ReplicatedVoice replicated) {
            this.replicated = replicated;
            return this;
        }

        /**
         * Sets replicated voice configuration from audio samples.
         *
         * @param sourceAudio  The reference audio sample.
         * @param consentAudio The consent recording.
         * @return This builder.
         */
        public Builder replicated(AudioData sourceAudio, AudioData consentAudio) {
            this.replicated = ReplicatedVoice.of(sourceAudio, consentAudio);
            return this;
        }

        /**
         * Sets the usage statistics.
         *
         * @param usage The usage.
         * @return This builder.
         */
        public Builder usage(Interaction.Usage usage) {
            this.usage = usage;
            return this;
        }

        /**
         * Sets the creation timestamp.
         *
         * @param createTime The creation timestamp.
         * @return This builder.
         */
        public Builder createTime(Instant createTime) {
            this.createTime = createTime;
            return this;
        }

        /**
         * Sets the expiration timestamp.
         *
         * @param expireTime The expiration timestamp.
         * @return This builder.
         */
        public Builder expireTime(Instant expireTime) {
            this.expireTime = expireTime;
            return this;
        }

        /**
         * Builds the {@link Voice} instance.
         *
         * @return A new Voice instance.
         */
        public Voice build() {
            return new Voice(
                id,
                key,
                name,
                type,
                displayName,
                description,
                model,
                gender,
                pitch,
                languageCode,
                accent,
                persona,
                regionCode,
                context != null ? List.copyOf(context) : null,
                sampleAudio,
                prompted,
                replicated,
                usage,
                createTime,
                expireTime
            );
        }
    }
}
