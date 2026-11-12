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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a structured decision question evaluated against a shared input in the Gemini Decisions API.
 * <p>
 * Every question uses one of three question types:
 * <ul>
 *   <li>{@link NoulQuestion} ({@code "noul"}) — Yes/no question returning calibrated probability {@code 0.01..0.99}.</li>
 *   <li>{@link ChoiceQuestion} ({@code "choice"}) — Picks one option from a fixed map of options.</li>
 *   <li>{@link ScoreQuestion} ({@code "score"}) — Rates input across ordered rubric levels (indexed starting at 0).</li>
 * </ul>
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true,
    defaultImpl = Question.UnknownQuestion.class
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Question.NoulQuestion.class, name = "noul"),
    @JsonSubTypes.Type(value = Question.ChoiceQuestion.class, name = "choice"),
    @JsonSubTypes.Type(value = Question.ScoreQuestion.class, name = "score")
})
public sealed interface Question permits
    Question.NoulQuestion,
    Question.ChoiceQuestion,
    Question.ScoreQuestion,
    Question.UnknownQuestion {

    /**
     * Returns the question type ({@code "noul"}, {@code "choice"}, or {@code "score"}).
     *
     * @return The question type identifier.
     */
    String type();

    /**
     * Creates a yes/no ({@code "noul"}) question with plain text instructions.
     *
     * @param instructions The yes/no question to evaluate against the input.
     * @return A {@link NoulQuestion}.
     */
    static NoulQuestion noul(String instructions) {
        return new NoulQuestion(instructions);
    }

    /**
     * Creates a yes/no ({@code "noul"}) question with structured {@link DecisionInstructions}.
     *
     * @param instructions The yes/no question instructions.
     * @return A {@link NoulQuestion}.
     */
    static NoulQuestion noul(DecisionInstructions instructions) {
        return new NoulQuestion(instructions, null);
    }

    /**
     * Creates a yes/no ({@code "noul"}) question with explicit true/false criteria descriptions.
     *
     * @param instructions     The yes/no question to evaluate against the input.
     * @param trueDescription  Description of when the answer is yes (true).
     * @param falseDescription Description of when the answer is no (false).
     * @return A {@link NoulQuestion}.
     */
    static NoulQuestion noul(String instructions, String trueDescription, String falseDescription) {
        return new NoulQuestion(instructions, new NoulCriteria(trueDescription, falseDescription));
    }

    /**
     * Creates a yes/no ({@code "noul"}) question with {@link NoulCriteria}.
     *
     * @param instructions The yes/no question to evaluate against the input.
     * @param criteria     Explicit criteria describing true and false cases.
     * @return A {@link NoulQuestion}.
     */
    static NoulQuestion noul(String instructions, NoulCriteria criteria) {
        return new NoulQuestion(instructions, criteria);
    }

    /**
     * Creates a yes/no ({@code "noul"}) question with {@link DecisionInstructions} and {@link NoulCriteria}.
     *
     * @param instructions The yes/no question instructions.
     * @param criteria     Explicit criteria describing true and false cases.
     * @return A {@link NoulQuestion}.
     */
    static NoulQuestion noul(DecisionInstructions instructions, NoulCriteria criteria) {
        return new NoulQuestion(instructions, criteria);
    }

    /**
     * Creates a choice ({@code "choice"}) question from an option-to-description map (2 to 255 options).
     * Use an empty string ({@code ""}) as the description when an option ID is self-explanatory.
     *
     * @param instructions The choice question to evaluate against the input.
     * @param criteria     Map of option ID to option description.
     * @return A {@link ChoiceQuestion}.
     */
    static ChoiceQuestion choice(String instructions, Map<String, String> criteria) {
        return new ChoiceQuestion(instructions, criteria);
    }

    /**
     * Creates a choice ({@code "choice"}) question with {@link DecisionInstructions} and an option-to-description map.
     *
     * @param instructions The choice question instructions.
     * @param criteria     Map of option ID to option description.
     * @return A {@link ChoiceQuestion}.
     */
    static ChoiceQuestion choice(DecisionInstructions instructions, Map<String, String> criteria) {
        return new ChoiceQuestion(instructions, criteria);
    }

    /**
     * Creates a choice ({@code "choice"}) question from self-explanatory option IDs (mapped to {@code ""}).
     *
     * @param instructions The choice question to evaluate against the input.
     * @param options      List of option IDs (2 to 255 options).
     * @return A {@link ChoiceQuestion}.
     */
    static ChoiceQuestion choice(String instructions, List<String> options) {
        Map<String, String> criteriaMap = new LinkedHashMap<>();
        if (options != null) {
            for (String option : options) {
                criteriaMap.put(option, "");
            }
        }
        return new ChoiceQuestion(instructions, criteriaMap);
    }

    /**
     * Creates a choice ({@code "choice"}) question from self-explanatory option IDs (mapped to {@code ""}).
     *
     * @param instructions The choice question to evaluate against the input.
     * @param options      Option IDs (2 to 255 options).
     * @return A {@link ChoiceQuestion}.
     */
    static ChoiceQuestion choice(String instructions, String... options) {
        return choice(instructions, options != null ? List.of(options) : List.of());
    }

    /**
     * Creates a score ({@code "score"}) question from ordered rubric level descriptions (indexed starting at 0).
     *
     * @param instructions The rating question to evaluate against the input.
     * @param criteria     Ordered non-empty level descriptions from lowest (index 0) to highest (2 to 255 levels).
     * @return A {@link ScoreQuestion}.
     */
    static ScoreQuestion score(String instructions, List<String> criteria) {
        return new ScoreQuestion(instructions, criteria);
    }

    /**
     * Creates a score ({@code "score"}) question with {@link DecisionInstructions} and ordered rubric levels.
     *
     * @param instructions The rating question instructions.
     * @param criteria     Ordered non-empty level descriptions from lowest (index 0) to highest (2 to 255 levels).
     * @return A {@link ScoreQuestion}.
     */
    static ScoreQuestion score(DecisionInstructions instructions, List<String> criteria) {
        return new ScoreQuestion(instructions, criteria);
    }

    /**
     * Creates a score ({@code "score"}) question from ordered rubric level descriptions (indexed starting at 0).
     *
     * @param instructions The rating question to evaluate against the input.
     * @param criteria     Ordered non-empty level descriptions from lowest (index 0) to highest (2 to 255 levels).
     * @return A {@link ScoreQuestion}.
     */
    static ScoreQuestion score(String instructions, String... criteria) {
        return new ScoreQuestion(instructions, criteria != null ? List.of(criteria) : List.of());
    }

    /**
     * Optional criteria for a {@link NoulQuestion} defining what true (yes) and false (no) mean.
     *
     * @param trueDescription  Description of when the answer is yes (true).
     * @param falseDescription Description of when the answer is no (false).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record NoulCriteria(
        @JsonProperty("true_description") String trueDescription,
        @JsonProperty("false_description") String falseDescription
    ) {
        /**
         * Creates a NoulCriteria instance.
         *
         * @param trueDescription  Description of when the answer is yes (true).
         * @param falseDescription Description of when the answer is no (false).
         * @return A NoulCriteria instance.
         */
        public static NoulCriteria of(String trueDescription, String falseDescription) {
            return new NoulCriteria(trueDescription, falseDescription);
        }
    }

    /**
     * A binary yes/no ({@code "noul"}) decision question.
     *
     * @param type         The question type ({@code "noul"}).
     * @param instructions The question instructions (plain string or text content blocks).
     * @param criteria     Optional true/false descriptions when the question alone is ambiguous.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record NoulQuestion(
        String type,
        DecisionInstructions instructions,
        NoulCriteria criteria
    ) implements Question {
        /**
         * Compact constructor normalizing {@code type} to {@code "noul"}.
         *
         * @param type         The question type.
         * @param instructions The question instructions.
         * @param criteria     Optional true/false criteria.
         */
        public NoulQuestion {
            if (type == null) {
                type = "noul";
            }
        }

        /**
         * Creates a NoulQuestion from plain text instructions.
         *
         * @param instructions The question instructions.
         */
        public NoulQuestion(String instructions) {
            this("noul", DecisionInstructions.of(instructions), null);
        }

        /**
         * Creates a NoulQuestion from plain text instructions and {@link NoulCriteria}.
         *
         * @param instructions The question instructions.
         * @param criteria     Optional true/false criteria.
         */
        public NoulQuestion(String instructions, NoulCriteria criteria) {
            this("noul", DecisionInstructions.of(instructions), criteria);
        }

        /**
         * Creates a NoulQuestion from {@link DecisionInstructions} and {@link NoulCriteria}.
         *
         * @param instructions The question instructions.
         * @param criteria     Optional true/false criteria.
         */
        public NoulQuestion(DecisionInstructions instructions, NoulCriteria criteria) {
            this("noul", instructions, criteria);
        }
    }

    /**
     * A categorical ({@code "choice"}) decision question that selects one option from a fixed map.
     *
     * @param type         The question type ({@code "choice"}).
     * @param instructions The question instructions (plain string or text content blocks).
     * @param criteria     Map of option ID to option description (2 to 255 options).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ChoiceQuestion(
        String type,
        DecisionInstructions instructions,
        Map<String, String> criteria
    ) implements Question {
        /**
         * Compact constructor normalizing {@code type} to {@code "choice"}.
         *
         * @param type         The question type.
         * @param instructions The question instructions.
         * @param criteria     Map of option ID to option description.
         */
        public ChoiceQuestion {
            if (type == null) {
                type = "choice";
            }
        }

        /**
         * Creates a ChoiceQuestion from plain text instructions and an option map.
         *
         * @param instructions The question instructions.
         * @param criteria     Map of option ID to option description.
         */
        public ChoiceQuestion(String instructions, Map<String, String> criteria) {
            this("choice", DecisionInstructions.of(instructions), criteria);
        }

        /**
         * Creates a ChoiceQuestion from {@link DecisionInstructions} and an option map.
         *
         * @param instructions The question instructions.
         * @param criteria     Map of option ID to option description.
         */
        public ChoiceQuestion(DecisionInstructions instructions, Map<String, String> criteria) {
            this("choice", instructions, criteria);
        }
    }

    /**
     * A rubric rating ({@code "score"}) decision question across ordered levels (indexed starting at 0).
     *
     * @param type         The question type ({@code "score"}).
     * @param instructions The question instructions (plain string or text content blocks).
     * @param criteria     Ordered non-empty level descriptions from lowest (index 0) to highest (2 to 255 levels).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ScoreQuestion(
        String type,
        DecisionInstructions instructions,
        List<String> criteria
    ) implements Question {
        /**
         * Compact constructor normalizing {@code type} to {@code "score"}.
         *
         * @param type         The question type.
         * @param instructions The question instructions.
         * @param criteria     Ordered non-empty level descriptions.
         */
        public ScoreQuestion {
            if (type == null) {
                type = "score";
            }
        }

        /**
         * Creates a ScoreQuestion from plain text instructions and ordered level descriptions.
         *
         * @param instructions The question instructions.
         * @param criteria     Ordered non-empty level descriptions.
         */
        public ScoreQuestion(String instructions, List<String> criteria) {
            this("score", DecisionInstructions.of(instructions), criteria);
        }

        /**
         * Creates a ScoreQuestion from {@link DecisionInstructions} and ordered level descriptions.
         *
         * @param instructions The question instructions.
         * @param criteria     Ordered non-empty level descriptions.
         */
        public ScoreQuestion(DecisionInstructions instructions, List<String> criteria) {
            this("score", instructions, criteria);
        }
    }

    /**
     * Fallback representation for unknown or future decision question types.
     *
     * @param type The question type identifier.
     * @param raw  Raw unrecognized properties in the question payload.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record UnknownQuestion(
        String type,
        @JsonAnySetter
        @JsonAnyGetter
        Map<String, Object> raw
    ) implements Question {
        /**
         * Creates an UnknownQuestion.
         *
         * @param type The question type identifier.
         * @param raw  Raw unrecognized properties.
         */
        public UnknownQuestion {
            if (raw == null) {
                raw = new HashMap<>();
            }
        }
    }
}
