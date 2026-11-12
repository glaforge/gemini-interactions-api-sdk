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
import io.github.glaforge.gemini.interactions.model.Content;
import io.github.glaforge.gemini.interactions.model.Decision;
import io.github.glaforge.gemini.interactions.model.DecisionInstructions;
import io.github.glaforge.gemini.interactions.model.DecisionParams;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionInput;
import io.github.glaforge.gemini.interactions.model.ModelOption;
import io.github.glaforge.gemini.interactions.model.Question;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecisionsTest {

    private final ObjectMapper mapper = JsonMapper.builder().build();
    private MockWebServer mockServer;
    private GeminiInteractionsClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
        String baseUrl = mockServer.url("").toString();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        client = GeminiInteractionsClient.builder()
            .baseUrl(baseUrl)
            .apiKey("test-api-key")
            .build();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void testDecisionParamsDefaultModelAndAllQuestionTypesSerialization() throws Exception {
        Map<String, String> choiceCriteria = new LinkedHashMap<>();
        choiceCriteria.put("billing", "Payments, invoices, and refunds.");
        choiceCriteria.put("technical", "Problems using the product.");
        choiceCriteria.put("other", "Anything outside these categories.");

        DecisionParams params = DecisionParams.builder()
            .input("I was charged twice and the app crashed.")
            .noul(
                "is_defect",
                "Is the customer reporting a product defect?",
                "The product itself is broken or faulty.",
                "Preference, sizing, shipping, or pricing complaint."
            )
            .choice("department", "Which department should handle this request?", choiceCriteria)
            .choice("priority_tag", "Pick urgency tag", "urgent", "normal")
            .score(
                "severity",
                "How severe is this issue?",
                "Cosmetic: appearance only.",
                "Workaround available.",
                "Fully blocked."
            )
            .build();

        assertEquals(ModelOption.GEMINI_DECISION_1, params.model());
        assertTrue(params.input().isText());
        assertEquals(4, params.questions().size());

        String json = mapper.writeValueAsString(params);
        JsonNode root = mapper.readTree(json);

        assertEquals("gemini-decision-1", root.get("model").asText());
        assertEquals("I was charged twice and the app crashed.", root.get("input").asText());

        JsonNode isDefect = root.get("questions").get("is_defect");
        assertEquals("noul", isDefect.get("type").asText());
        assertEquals("Is the customer reporting a product defect?", isDefect.get("instructions").asText());
        assertEquals("The product itself is broken or faulty.", isDefect.get("criteria").get("true_description").asText());
        assertEquals("Preference, sizing, shipping, or pricing complaint.", isDefect.get("criteria").get("false_description").asText());

        JsonNode dept = root.get("questions").get("department");
        assertEquals("choice", dept.get("type").asText());
        assertEquals("Payments, invoices, and refunds.", dept.get("criteria").get("billing").asText());

        JsonNode priorityTag = root.get("questions").get("priority_tag");
        assertEquals("choice", priorityTag.get("type").asText());
        assertEquals("", priorityTag.get("criteria").get("urgent").asText());
        assertEquals("", priorityTag.get("criteria").get("normal").asText());

        JsonNode severity = root.get("questions").get("severity");
        assertEquals("score", severity.get("type").asText());
        assertEquals(3, severity.get("criteria").size());
        assertEquals("Cosmetic: appearance only.", severity.get("criteria").get(0).asText());

        // Round-trip deserialization
        DecisionParams roundTrip = mapper.readValue(json, DecisionParams.class);
        assertEquals("gemini-decision-1", roundTrip.model());
        assertInstanceOf(Question.NoulQuestion.class, roundTrip.questions().get("is_defect"));
        assertInstanceOf(Question.ChoiceQuestion.class, roundTrip.questions().get("department"));
        assertInstanceOf(Question.ScoreQuestion.class, roundTrip.questions().get("severity"));
    }

    @Test
    void testMultimodalInputAndBlockInstructions() throws Exception {
        byte[] jpegBytes = "fake-jpeg".getBytes(StandardCharsets.UTF_8);
        Question.NoulQuestion blockQuestion = Question.noul(
            DecisionInstructions.ofBlocks(
                new Content.TextContent("Does the photo show "),
                new Content.TextContent("physical damage?")
            )
        );
        assertEquals("Does the photo show physical damage?", blockQuestion.instructions().asText());
        assertTrue(blockQuestion.instructions().isBlocks());

        DecisionParams params = DecisionParams.builder()
            .input(
                new Content.TextContent("Customer photo for return #8821."),
                new Content.ImageContent(jpegBytes, "image/jpeg", Content.Resolution.LOW)
            )
            .question("visible_damage", blockQuestion)
            .build();

        String json = mapper.writeValueAsString(params);
        JsonNode root = mapper.readTree(json);
        assertTrue(root.get("input").isArray());
        assertEquals(2, root.get("input").size());
        assertEquals("image", root.get("input").get(1).get("type").asText());
        assertEquals("low", root.get("input").get(1).get("resolution").asText());
        assertTrue(root.get("questions").get("visible_damage").get("instructions").isArray());

        // Also verify single-object TextContent instructions deserialization
        String singleBlockJson = """
            {
              "type": "noul",
              "instructions": { "type": "text", "text": "Single block instruction" }
            }
            """;
        Question parsed = mapper.readValue(singleBlockJson, Question.class);
        Question.NoulQuestion noulParsed = assertInstanceOf(Question.NoulQuestion.class, parsed);
        assertEquals("Single block instruction", noulParsed.instructions().asText());
    }

    @Test
    void testTurnsInputRejected() {
        InteractionInput turnsInput = InteractionInput.ofTurns(
            new Interaction.Turn(Interaction.Role.USER, "Hello")
        );
        assertThrows(IllegalArgumentException.class, () -> DecisionParams.builder().input(turnsInput));
    }

    @Test
    void testDecisionResponseDeserializationAndHelpers() throws Exception {
        String responseJson = """
            {
              "answers": {
                "is_defect": { "type": "noul", "noul": 0.94, "confidence": 0.94 },
                "next_action": {
                  "type": "choice",
                  "choice": "human_escalation",
                  "probabilities": {
                    "human_escalation": 0.76,
                    "replacement_shipment": 0.18,
                    "troubleshooting_flow": 0.04,
                    "instant_refund": 0.02
                  },
                  "confidence": 0.76
                },
                "churn_risk": {
                  "type": "score",
                  "score": 2.40,
                  "legend": {
                    "0": "No churn signal",
                    "1": "Mild frustration",
                    "2": "Mentions alternatives",
                    "3": "Threatens to cancel"
                  },
                  "probabilities": { "0": 0.0, "1": 0.05, "2": 0.50, "3": 0.45 },
                  "confidence": 0.50
                }
              },
              "usage": {
                "total_input_tokens": 324,
                "input_tokens_by_modality": [{ "modality": "text", "tokens": 324 }],
                "total_output_tokens": 0,
                "total_tokens": 324
              }
            }
            """;

        Decision decision = mapper.readValue(responseJson, Decision.class);
        assertFalse(decision.isBlocked());
        assertNull(decision.promptFeedback());
        assertNotNull(decision.usage());
        assertEquals(324, decision.usage().totalInputTokens());
        assertEquals(0, decision.usage().totalOutputTokens());

        Answer.NoulAnswer isDefect = decision.noulAnswer("is_defect");
        assertNotNull(isDefect);
        assertEquals(0.94, isDefect.noul(), 1e-6);
        assertEquals(0.94, isDefect.yesProbability(), 1e-6);
        assertEquals(0.06, isDefect.noProbability(), 1e-6);
        assertEquals(0.94, isDefect.confidence(), 1e-6);

        Answer.ChoiceAnswer nextAction = decision.choiceAnswer("next_action");
        assertNotNull(nextAction);
        assertEquals("human_escalation", nextAction.choice());
        assertEquals(0.76, nextAction.confidence(), 1e-6);
        assertEquals(0.18, nextAction.probabilities().get("replacement_shipment"), 1e-6);

        Answer.ScoreAnswer churnRisk = decision.scoreAnswer("churn_risk");
        assertNotNull(churnRisk);
        assertEquals(2.40, churnRisk.score(), 1e-6);
        assertEquals("2", churnRisk.topLevelKey());
        assertEquals("Mentions alternatives", churnRisk.topLevelDescription());
        assertEquals(2.40 / 3.0, churnRisk.normalizedScore(), 1e-6);

        // Wrong accessor type throws IllegalStateException
        assertThrows(IllegalStateException.class, () -> decision.choiceAnswer("is_defect"));
        assertNull(decision.noulAnswer("missing_key"));
    }

    @Test
    void testPromptFeedbackAndUnknownFallbacks() throws Exception {
        String blockedJson = """
            {
              "prompt_feedback": {
                "block_reason": "safety",
                "block_reason_message": "Blocked due to safety policy."
              }
            }
            """;
        Decision blocked = mapper.readValue(blockedJson, Decision.class);
        assertTrue(blocked.isBlocked());
        assertEquals(Decision.BlockReason.SAFETY, blocked.promptFeedback().blockReason());
        assertEquals("Blocked due to safety policy.", blocked.promptFeedback().blockReasonMessage());

        String futureReasonJson = """
            {
              "prompt_feedback": {
                "block_reason": "brand_new_future_reason"
              },
              "answers": {
                "future_q": {
                  "type": "ranking",
                  "order": ["a", "b"]
                }
              }
            }
            """;
        Decision futureDecision = mapper.readValue(futureReasonJson, Decision.class);
        assertTrue(futureDecision.isBlocked());
        assertEquals(Decision.BlockReason.UNKNOWN, futureDecision.promptFeedback().blockReason());
        Answer unknownAnswer = futureDecision.answer("future_q");
        Answer.UnknownAnswer ua = assertInstanceOf(Answer.UnknownAnswer.class, unknownAnswer);
        assertEquals("ranking", ua.type());
        assertEquals(List.of("a", "b"), ua.raw().get("order"));
    }

    @Test
    void testClientCreateDecisionSuccessAndError() throws Exception {
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody("""
                {
                  "answers": {
                    "is_damaged": { "type": "noul", "noul": 0.99, "confidence": 0.99 }
                  },
                  "usage": {
                    "total_input_tokens": 100,
                    "input_tokens_by_modality": [{ "modality": "text", "tokens": 100 }],
                    "total_output_tokens": 0,
                    "total_tokens": 100
                  }
                }
                """));

        DecisionParams params = DecisionParams.builder()
            .input("The blue mug I ordered on Monday arrived cracked on Wednesday.")
            .noul("is_damaged", "Did the item arrive damaged?")
            .build();

        Decision response = client.createDecision(params);
        assertEquals(0.99, response.noulAnswer("is_damaged").noul(), 1e-6);
        assertEquals(100, response.usage().totalInputTokens());

        RecordedRequest recorded = mockServer.takeRequest();
        assertEquals("POST", recorded.getMethod());
        assertEquals("/v1beta/decisions", recorded.getPath());
        assertEquals("test-api-key", recorded.getHeader("x-goog-api-key"));
        assertTrue(recorded.getBody().readUtf8().contains("\"gemini-decision-1\""));

        // Verify HTTP 400 safety block surfaces as GeminiInteractionsException
        mockServer.enqueue(new MockResponse()
            .setResponseCode(400)
            .addHeader("Content-Type", "application/json")
            .setBody("""
                {
                  "error": {
                    "code": "safety",
                    "message": "Request blocked due to safety violations."
                  }
                }
                """));

        GeminiInteractionsException ex = assertThrows(
            GeminiInteractionsException.class,
            () -> client.createDecision(params)
        );
        assertEquals(400, ex.getStatusCode());
        assertTrue(ex.getBody().contains("safety"));
    }
}
