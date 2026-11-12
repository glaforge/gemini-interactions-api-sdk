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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Request parameters for evaluating structured decision questions via the Gemini Decisions API
 * ({@code POST /v1beta/decisions}).
 *
 * @param model     The decision model ID (defaults to {@link ModelOption#GEMINI_DECISION_1}).
 * @param input     Shared context evaluated once across all questions (text or multimodal {@link Content} blocks).
 * @param questions Map of caller-defined question keys ({@code [A-Za-z0-9_-]+}, 1 to 1,000 questions) to {@link Question} definitions.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DecisionParams(
    String model,
    InteractionInput input,
    Map<String, Question> questions
) {
    /**
     * Compact constructor validating that {@code input}, if present, uses supported Decision input variants.
     *
     * @param model     The decision model ID.
     * @param input     Shared context input.
     * @param questions Map of questions to evaluate.
     */
    public DecisionParams {
        if (input != null && (input.isTurns() || input.isSteps())) {
            throw new IllegalArgumentException(
                "DecisionParams input supports plain text or Content blocks only; conversation turns and steps are not supported."
            );
        }
        if (questions != null) {
            questions = Collections.unmodifiableMap(new LinkedHashMap<>(questions));
        }
    }

    /**
     * Creates a new builder for {@link DecisionParams}, pre-configured with {@link ModelOption#GEMINI_DECISION_1}.
     *
     * @return A new {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link DecisionParams}.
     */
    public static class Builder {
        private String model = ModelOption.GEMINI_DECISION_1;
        private InteractionInput input;
        private final Map<String, Question> questions = new LinkedHashMap<>();

        /** Creates a new Builder. */
        public Builder() {}

        /**
         * Sets the decision model ID (defaults to {@link ModelOption#GEMINI_DECISION_1}).
         *
         * @param model The model ID (e.g. {@code "gemini-decision-1"}).
         * @return This builder.
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * Sets the shared input context as plain text.
         *
         * @param text The plain text input.
         * @return This builder.
         */
        public Builder input(String text) {
            this.input = InteractionInput.of(text);
            return this;
        }

        /**
         * Sets the shared input context as one or more multimodal {@link Content} blocks
         * ({@link Content.TextContent}, {@link Content.ImageContent}, {@link Content.AudioContent},
         * {@link Content.VideoContent}, or {@link Content.DocumentContent}).
         *
         * @param contents The content blocks.
         * @return This builder.
         */
        public Builder input(Content... contents) {
            this.input = InteractionInput.ofContents(contents);
            return this;
        }

        /**
         * Sets the shared input context as a list of multimodal {@link Content} blocks.
         *
         * @param contents The list of content blocks.
         * @return This builder.
         */
        public Builder input(List<Content> contents) {
            this.input = InteractionInput.ofContents(contents);
            return this;
        }

        /**
         * Sets the shared input context directly from an {@link InteractionInput}.
         * Only plain text and {@link Content} block inputs are supported by the Decisions API.
         *
         * @param input The interaction input.
         * @return This builder.
         * @throws IllegalArgumentException If {@code input} contains turns or steps.
         */
        public Builder input(InteractionInput input) {
            if (input != null && (input.isTurns() || input.isSteps())) {
                throw new IllegalArgumentException(
                    "DecisionParams input supports plain text or Content blocks only; conversation turns and steps are not supported."
                );
            }
            this.input = input;
            return this;
        }

        /**
         * Replaces all questions with the provided map.
         *
         * @param questions Map of question key to {@link Question}.
         * @return This builder.
         */
        public Builder questions(Map<String, Question> questions) {
            this.questions.clear();
            if (questions != null) {
                this.questions.putAll(questions);
            }
            return this;
        }

        /**
         * Adds or replaces a single {@link Question} under the given key ({@code [A-Za-z0-9_-]+}, up to 64 chars).
         *
         * @param key      Unique question identifier.
         * @param question The question definition.
         * @return This builder.
         */
        public Builder question(String key, Question question) {
            this.questions.put(key, question);
            return this;
        }

        /**
         * Adds a binary yes/no ({@code "noul"}) question under the given key.
         *
         * @param key          Unique question identifier.
         * @param instructions The yes/no question to evaluate against the input.
         * @return This builder.
         */
        public Builder noul(String key, String instructions) {
            return question(key, Question.noul(instructions));
        }

        /**
         * Adds a binary yes/no ({@code "noul"}) question with explicit true/false criteria descriptions.
         *
         * @param key              Unique question identifier.
         * @param instructions     The yes/no question to evaluate against the input.
         * @param trueDescription  Description of when the answer is yes (true).
         * @param falseDescription Description of when the answer is no (false).
         * @return This builder.
         */
        public Builder noul(String key, String instructions, String trueDescription, String falseDescription) {
            return question(key, Question.noul(instructions, trueDescription, falseDescription));
        }

        /**
         * Adds a binary yes/no ({@code "noul"}) question with {@link Question.NoulCriteria}.
         *
         * @param key          Unique question identifier.
         * @param instructions The yes/no question to evaluate against the input.
         * @param criteria     Explicit true/false criteria.
         * @return This builder.
         */
        public Builder noul(String key, String instructions, Question.NoulCriteria criteria) {
            return question(key, Question.noul(instructions, criteria));
        }

        /**
         * Adds a categorical ({@code "choice"}) question from an option-to-description map.
         *
         * @param key          Unique question identifier.
         * @param instructions The choice question to evaluate against the input.
         * @param criteria     Map of option ID to option description (2 to 255 options).
         * @return This builder.
         */
        public Builder choice(String key, String instructions, Map<String, String> criteria) {
            return question(key, Question.choice(instructions, criteria));
        }

        /**
         * Adds a categorical ({@code "choice"}) question from self-explanatory option IDs (mapped to {@code ""}).
         *
         * @param key          Unique question identifier.
         * @param instructions The choice question to evaluate against the input.
         * @param options      Option IDs (2 to 255 options).
         * @return This builder.
         */
        public Builder choice(String key, String instructions, String... options) {
            return question(key, Question.choice(instructions, options));
        }

        /**
         * Adds a rubric rating ({@code "score"}) question across ordered level descriptions (indexed starting at 0).
         *
         * @param key          Unique question identifier.
         * @param instructions The rating question to evaluate against the input.
         * @param criteria     Ordered non-empty level descriptions from lowest (index 0) to highest (2 to 255 levels).
         * @return This builder.
         */
        public Builder score(String key, String instructions, List<String> criteria) {
            return question(key, Question.score(instructions, criteria));
        }

        /**
         * Adds a rubric rating ({@code "score"}) question across ordered level descriptions (indexed starting at 0).
         *
         * @param key          Unique question identifier.
         * @param instructions The rating question to evaluate against the input.
         * @param criteria     Ordered non-empty level descriptions from lowest (index 0) to highest (2 to 255 levels).
         * @return This builder.
         */
        public Builder score(String key, String instructions, String... criteria) {
            return question(key, Question.score(instructions, criteria));
        }

        /**
         * Builds the {@link DecisionParams} instance.
         *
         * @return A new {@link DecisionParams}.
         */
        public DecisionParams build() {
            return new DecisionParams(model, input, questions);
        }
    }
}
