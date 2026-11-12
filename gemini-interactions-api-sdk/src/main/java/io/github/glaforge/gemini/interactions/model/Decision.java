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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Response returned by the Gemini Decisions API ({@code POST /v1beta/decisions}).
 *
 * @param answers        Decision answers keyed by the question identifiers from {@link DecisionParams#questions()}.
 * @param promptFeedback Optional feedback from input content filters if the input was blocked.
 * @param usage          Token usage statistics (with {@code totalOutputTokens} always {@code 0}).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Decision(
    Map<String, Answer> answers,
    @JsonProperty("prompt_feedback") PromptFeedback promptFeedback,
    Interaction.Usage usage
) {
    /**
     * Convenience constructor without {@code promptFeedback}.
     *
     * @param answers Decision answers map.
     * @param usage   Token usage statistics.
     */
    public Decision(Map<String, Answer> answers, Interaction.Usage usage) {
        this(answers, null, usage);
    }

    /**
     * Retrieves the {@link Answer} for the given question key, or {@code null} if not present.
     *
     * @param key The question identifier.
     * @return The {@link Answer}, or {@code null}.
     */
    public Answer answer(String key) {
        return answers != null ? answers.get(key) : null;
    }

    /**
     * Retrieves the answer for the given question key as a {@link Answer.NoulAnswer}.
     *
     * @param key The question identifier.
     * @return The {@link Answer.NoulAnswer}, or {@code null} if absent.
     * @throws IllegalStateException If an answer exists under {@code key} with a non-{@code noul} type.
     */
    public Answer.NoulAnswer noulAnswer(String key) {
        Answer answer = answer(key);
        if (answer == null) {
            return null;
        }
        if (answer instanceof Answer.NoulAnswer noulAnswer) {
            return noulAnswer;
        }
        throw new IllegalStateException("Answer for key '" + key + "' is of type '" + answer.type() + "', expected 'noul'");
    }

    /**
     * Retrieves the answer for the given question key as a {@link Answer.ChoiceAnswer}.
     *
     * @param key The question identifier.
     * @return The {@link Answer.ChoiceAnswer}, or {@code null} if absent.
     * @throws IllegalStateException If an answer exists under {@code key} with a non-{@code choice} type.
     */
    public Answer.ChoiceAnswer choiceAnswer(String key) {
        Answer answer = answer(key);
        if (answer == null) {
            return null;
        }
        if (answer instanceof Answer.ChoiceAnswer choiceAnswer) {
            return choiceAnswer;
        }
        throw new IllegalStateException("Answer for key '" + key + "' is of type '" + answer.type() + "', expected 'choice'");
    }

    /**
     * Retrieves the answer for the given question key as a {@link Answer.ScoreAnswer}.
     *
     * @param key The question identifier.
     * @return The {@link Answer.ScoreAnswer}, or {@code null} if absent.
     * @throws IllegalStateException If an answer exists under {@code key} with a non-{@code score} type.
     */
    public Answer.ScoreAnswer scoreAnswer(String key) {
        Answer answer = answer(key);
        if (answer == null) {
            return null;
        }
        if (answer instanceof Answer.ScoreAnswer scoreAnswer) {
            return scoreAnswer;
        }
        throw new IllegalStateException("Answer for key '" + key + "' is of type '" + answer.type() + "', expected 'score'");
    }

    /**
     * Checks whether the input was blocked by safety or content filters.
     *
     * @return {@code true} if {@link #promptFeedback()} indicates a {@link BlockReason}.
     */
    public boolean isBlocked() {
        return promptFeedback != null && promptFeedback.blockReason() != null;
    }

    /**
     * Feedback from input content filters when a decision request is blocked.
     *
     * @param blockReason        Reason why the input was blocked, if blocked.
     * @param blockReasonMessage Optional human-readable message detailing the reason for blocking.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PromptFeedback(
        @JsonProperty("block_reason") BlockReason blockReason,
        @JsonProperty("block_reason_message") String blockReasonMessage
    ) {}

    /**
     * Reason why a decision input was blocked by content filters.
     */
    public enum BlockReason {
        /** Blocked due to safety violations. */
        SAFETY("safety"),
        /** Blocked for another reason. */
        OTHER("other"),
        /** Blocked due to blocklist match. */
        BLOCKLIST("blocklist"),
        /** Blocked due to prohibited content. */
        PROHIBITED_CONTENT("prohibited_content"),
        /** Blocked due to image safety violations. */
        IMAGE_SAFETY("image_safety"),
        /** Blocked due to general content policy filter. */
        CONTENT_BLOCKED("content_blocked"),
        /** Fallback for unrecognized future block reason values. */
        UNKNOWN("unknown");

        private final String value;

        BlockReason(String value) {
            this.value = value;
        }

        /**
         * Returns the wire string value of this block reason.
         *
         * @return The JSON string value.
         */
        @JsonValue
        public String value() {
            return value;
        }

        /**
         * Parses a {@link BlockReason} from its JSON wire value, falling back to {@link #UNKNOWN}
         * for unrecognized future values.
         *
         * @param value The JSON string value.
         * @return The matching {@link BlockReason}, or {@code null} if {@code value} is {@code null}.
         */
        @JsonCreator
        public static BlockReason fromValue(String value) {
            if (value == null) {
                return null;
            }
            for (BlockReason reason : values()) {
                if (reason.value.equalsIgnoreCase(value)) {
                    return reason;
                }
            }
            return UNKNOWN;
        }
    }

    /**
     * Creates a new builder for {@link Decision}.
     *
     * @return A new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link Decision}.
     */
    public static class Builder {
        private final Map<String, Answer> answers = new LinkedHashMap<>();
        private PromptFeedback promptFeedback;
        private Interaction.Usage usage;

        /** Creates a new Builder. */
        public Builder() {}

        /**
         * Sets the answers map.
         *
         * @param answers Map of question key to {@link Answer}.
         * @return This builder.
         */
        public Builder answers(Map<String, Answer> answers) {
            this.answers.clear();
            if (answers != null) {
                this.answers.putAll(answers);
            }
            return this;
        }

        /**
         * Adds a single answer entry.
         *
         * @param key    Question identifier.
         * @param answer The evaluated {@link Answer}.
         * @return This builder.
         */
        public Builder answer(String key, Answer answer) {
            this.answers.put(key, answer);
            return this;
        }

        /**
         * Sets the prompt feedback.
         *
         * @param promptFeedback The prompt feedback.
         * @return This builder.
         */
        public Builder promptFeedback(PromptFeedback promptFeedback) {
            this.promptFeedback = promptFeedback;
            return this;
        }

        /**
         * Sets the token usage statistics.
         *
         * @param usage The token usage statistics.
         * @return This builder.
         */
        public Builder usage(Interaction.Usage usage) {
            this.usage = usage;
            return this;
        }

        /**
         * Builds the {@link Decision}.
         *
         * @return A new {@link Decision}.
         */
        public Decision build() {
            return new Decision(
                answers.isEmpty() && promptFeedback != null ? null : new LinkedHashMap<>(answers),
                promptFeedback,
                usage
            );
        }
    }
}
