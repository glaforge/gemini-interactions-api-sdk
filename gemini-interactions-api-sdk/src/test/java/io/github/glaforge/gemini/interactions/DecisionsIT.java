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

package io.github.glaforge.gemini.interactions;

import io.github.glaforge.gemini.interactions.model.Answer;
import io.github.glaforge.gemini.interactions.model.Decision;
import io.github.glaforge.gemini.interactions.model.DecisionParams;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class DecisionsIT {

    private final GeminiInteractionsClient client = GeminiInteractionsClient.builder()
        .apiKey(System.getenv("GEMINI_API_KEY"))
        .build();

    @Test
    void testCreateDecisionAllQuestionTypes() {
        DecisionParams params = DecisionParams.builder()
            .input("The blue ceramic mug I ordered arrived shattered in pieces, and I was charged twice.")
            .noul("is_damaged", "Did the item arrive physically damaged?")
            .choice("department", "Which department should handle the duplicate charge?", Map.of(
                "billing", "Payments, invoices, duplicate charges, and refunds.",
                "technical", "Software or technical product usage issues.",
                "other", "Anything outside these categories."
            ))
            .score(
                "severity",
                "How severe is the physical condition of the delivered mug?",
                "Intact: no visible damage.",
                "Minor blemish: small scratch, still usable.",
                "Destroyed: broken or shattered, completely unusable."
            )
            .build();

        Decision decision = client.createDecision(params);
        assertNotNull(decision);
        assertNotNull(decision.usage());
        assertEquals(0, decision.usage().totalOutputTokens());

        Answer.NoulAnswer damaged = decision.noulAnswer("is_damaged");
        assertNotNull(damaged);
        assertTrue(damaged.noul() > 0.7, "Expected high probability of damage, got: " + damaged.noul());

        Answer.ChoiceAnswer department = decision.choiceAnswer("department");
        assertNotNull(department);
        assertEquals("billing", department.choice());

        Answer.ScoreAnswer severity = decision.scoreAnswer("severity");
        assertNotNull(severity);
        assertTrue(severity.score() > 1.0, "Expected high severity score, got: " + severity.score());
    }
}
