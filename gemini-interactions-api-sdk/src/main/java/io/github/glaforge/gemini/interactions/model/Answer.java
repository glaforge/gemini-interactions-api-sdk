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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a typed answer to a {@link Question} returned by the Gemini Decisions API.
 * <p>
 * Every answer matches the type of its corresponding question:
 * <ul>
 *   <li>{@link NoulAnswer} ({@code "noul"}) — Calibrated probability of yes ({@code 0.01..0.99}) and confidence.</li>
 *   <li>{@link ChoiceAnswer} ({@code "choice"}) — Top option, per-option probabilities, and confidence.</li>
 *   <li>{@link ScoreAnswer} ({@code "score"}) — Probability-weighted expected score, level legend, per-level probabilities, and confidence.</li>
 * </ul>
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true,
    defaultImpl = Answer.UnknownAnswer.class
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Answer.NoulAnswer.class, name = "noul"),
    @JsonSubTypes.Type(value = Answer.ChoiceAnswer.class, name = "choice"),
    @JsonSubTypes.Type(value = Answer.ScoreAnswer.class, name = "score")
})
public sealed interface Answer permits
    Answer.NoulAnswer,
    Answer.ChoiceAnswer,
    Answer.ScoreAnswer,
    Answer.UnknownAnswer {

    /**
     * Returns the answer type ({@code "noul"}, {@code "choice"}, or {@code "score"}).
     *
     * @return The answer type identifier.
     */
    String type();

    /**
     * Answer to a yes/no ({@code "noul"}) question.
     *
     * @param type       The answer type ({@code "noul"}).
     * @param noul       Calibrated probability ({@code 0.01} to {@code 0.99}) that the answer is yes.
     * @param confidence Estimated probability ({@code 0} to {@code 1}) that the top answer is right ({@code max(noul, 1 - noul)}).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record NoulAnswer(
        String type,
        double noul,
        double confidence
    ) implements Answer {
        /**
         * Compact constructor normalizing {@code type} to {@code "noul"}.
         *
         * @param type       The answer type.
         * @param noul       Calibrated probability of yes.
         * @param confidence Confidence score.
         */
        public NoulAnswer {
            if (type == null) {
                type = "noul";
            }
        }

        /**
         * Creates a NoulAnswer.
         *
         * @param noul       Calibrated probability of yes ({@code 0.01} to {@code 0.99}).
         * @param confidence Confidence score.
         */
        public NoulAnswer(double noul, double confidence) {
            this("noul", noul, confidence);
        }

        /**
         * Returns the calibrated probability that the answer is yes (alias for {@link #noul()}).
         *
         * @return Probability of yes ({@code 0.01} to {@code 0.99}).
         */
        public double yesProbability() {
            return noul;
        }

        /**
         * Returns the calibrated probability that the answer is no ({@code 1.0 - noul}).
         *
         * @return Probability of no.
         */
        public double noProbability() {
            return 1.0 - noul;
        }
    }

    /**
     * Answer to a categorical ({@code "choice"}) question.
     *
     * @param type          The answer type ({@code "choice"}).
     * @param choice        The option key with the highest calibrated probability.
     * @param probabilities Calibrated probabilities across all options (summing to 1).
     * @param confidence    Estimated probability ({@code 0} to {@code 1}) that the top choice is right.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ChoiceAnswer(
        String type,
        String choice,
        Map<String, Double> probabilities,
        double confidence
    ) implements Answer {
        /**
         * Compact constructor normalizing {@code type} to {@code "choice"}.
         *
         * @param type          The answer type.
         * @param choice        The top option key.
         * @param probabilities Per-option probabilities.
         * @param confidence    Confidence score.
         */
        public ChoiceAnswer {
            if (type == null) {
                type = "choice";
            }
        }

        /**
         * Creates a ChoiceAnswer.
         *
         * @param choice        The top option key.
         * @param probabilities Per-option probabilities.
         * @param confidence    Confidence score.
         */
        public ChoiceAnswer(String choice, Map<String, Double> probabilities, double confidence) {
            this("choice", choice, probabilities, confidence);
        }
    }

    /**
     * Answer to a rubric rating ({@code "score"}) question.
     *
     * @param type          The answer type ({@code "score"}).
     * @param score         Probability-weighted expected level index (starting from {@code 0}).
     * @param legend        Map of level index string (e.g. {@code "0"}, {@code "1"}) to its rubric description.
     * @param probabilities Calibrated probabilities across all level indices (summing to 1).
     * @param confidence    Estimated probability ({@code 0} to {@code 1}) of the most probable discrete level.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ScoreAnswer(
        String type,
        double score,
        Map<String, String> legend,
        Map<String, Double> probabilities,
        double confidence
    ) implements Answer {
        /**
         * Compact constructor normalizing {@code type} to {@code "score"}.
         *
         * @param type          The answer type.
         * @param score         Probability-weighted expected level index.
         * @param legend        Map of level index string to description.
         * @param probabilities Per-level probabilities.
         * @param confidence    Confidence score.
         */
        public ScoreAnswer {
            if (type == null) {
                type = "score";
            }
        }

        /**
         * Creates a ScoreAnswer.
         *
         * @param score         Probability-weighted expected level index.
         * @param legend        Map of level index string to description.
         * @param probabilities Per-level probabilities.
         * @param confidence    Confidence score.
         */
        public ScoreAnswer(
            double score,
            Map<String, String> legend,
            Map<String, Double> probabilities,
            double confidence
        ) {
            this("score", score, legend, probabilities, confidence);
        }

        /**
         * Returns the discrete level index key (e.g. {@code "1"}) with the highest probability.
         *
         * @return The top level key, or null if {@code probabilities} is null or empty.
         */
        public String topLevelKey() {
            if (probabilities == null || probabilities.isEmpty()) {
                return null;
            }
            return probabilities.entrySet().stream()
                .max(Comparator.<Map.Entry<String, Double>, Double>comparing(Map.Entry::getValue)
                    .thenComparing(Map.Entry.comparingByKey(Comparator.reverseOrder())))
                .map(Map.Entry::getKey)
                .orElse(null);
        }

        /**
         * Returns the rubric description from {@link #legend()} for the highest-probability discrete level.
         *
         * @return The description of the top discrete level, or null if unavailable.
         */
        public String topLevelDescription() {
            String key = topLevelKey();
            if (key == null || legend == null) {
                return null;
            }
            return legend.get(key);
        }

        /**
         * Normalizes {@link #score()} into the {@code [0.0, 1.0]} range by dividing by the highest level index
         * ({@code legend.size() - 1}), making scales with different numbers of levels comparable.
         *
         * @return The normalized score between {@code 0.0} and {@code 1.0}, or {@code 0.0} if fewer than 2 levels exist.
         */
        public double normalizedScore() {
            int levelCount = legend != null && !legend.isEmpty()
                ? legend.size()
                : (probabilities != null ? probabilities.size() : 0);
            if (levelCount <= 1) {
                return 0.0;
            }
            return score / (levelCount - 1);
        }
    }

    /**
     * Fallback representation for unknown or future decision answer types.
     *
     * @param type The answer type identifier.
     * @param raw  Raw unrecognized properties in the answer payload.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record UnknownAnswer(
        String type,
        @JsonAnySetter
        @JsonAnyGetter
        Map<String, Object> raw
    ) implements Answer {
        /**
         * Creates an UnknownAnswer.
         *
         * @param type The answer type identifier.
         * @param raw  Raw unrecognized properties.
         */
        public UnknownAnswer {
            if (raw == null) {
                raw = new HashMap<>();
            }
        }
    }
}
