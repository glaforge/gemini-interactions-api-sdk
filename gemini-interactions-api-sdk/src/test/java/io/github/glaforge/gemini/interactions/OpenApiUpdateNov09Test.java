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

import io.github.glaforge.gemini.interactions.model.CodeExecutionResult;
import io.github.glaforge.gemini.interactions.model.Config.SmartTranscriptionMode;
import io.github.glaforge.gemini.interactions.model.Config.TranscriptionConfig;
import io.github.glaforge.gemini.interactions.model.Content;
import io.github.glaforge.gemini.interactions.model.Content.DocumentContent;
import io.github.glaforge.gemini.interactions.model.Content.ImageContent;
import io.github.glaforge.gemini.interactions.model.Content.TextContent;
import io.github.glaforge.gemini.interactions.model.Events;
import io.github.glaforge.gemini.interactions.model.Step;
import io.github.glaforge.gemini.interactions.model.Step.CodeExecutionResultStep;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for updates introduced in the November 9, 2026 OpenAPI specification sync.
 */
class OpenApiUpdateNov09Test {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder().build();
    }

    @Test
    void testCodeExecutionResultStepWithTextResult() throws Exception {
        String json = """
            {
                "type": "code_execution_result",
                "call_id": "code_call_71021",
                "result": "55\\n",
                "is_error": false,
                "signature": "sig_abc"
            }
            """;

        Step step = mapper.readValue(json, Step.class);
        CodeExecutionResultStep resultStep = assertInstanceOf(CodeExecutionResultStep.class, step);
        assertEquals("code_call_71021", resultStep.callId());
        assertFalse(resultStep.isError());
        assertEquals("sig_abc", resultStep.signature());

        assertNotNull(resultStep.result());
        assertTrue(resultStep.result().isText());
        assertFalse(resultStep.result().isContents());
        assertEquals("55\n", resultStep.result().text());
        assertEquals("55\n", resultStep.resultText());
        assertNull(resultStep.resultContents());
        assertEquals("55\n", resultStep.result().value());

        // Round-trip serialization
        String serialized = mapper.writeValueAsString(resultStep);
        assertTrue(serialized.contains("\"result\":\"55\\n\""));
        CodeExecutionResultStep roundTripped = mapper.readValue(serialized, CodeExecutionResultStep.class);
        assertEquals("55\n", roundTripped.resultText());
    }

    @Test
    void testCodeExecutionResultStepWithMultimodalContentList() throws Exception {
        String json = """
            {
                "type": "code_execution_result",
                "call_id": "code_call_plot_1",
                "result": [
                    {
                        "type": "text",
                        "text": "Generated chart successfully.\\n"
                    },
                    {
                        "type": "image",
                        "uri": "https://example.com/plot.png",
                        "mime_type": "image/png"
                    },
                    {
                        "type": "document",
                        "uri": "https://example.com/report.xlsx",
                        "mime_type": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    }
                ],
                "is_error": false
            }
            """;

        Step step = mapper.readValue(json, Step.class);
        CodeExecutionResultStep resultStep = assertInstanceOf(CodeExecutionResultStep.class, step);
        assertEquals("code_call_plot_1", resultStep.callId());
        assertNotNull(resultStep.result());
        assertTrue(resultStep.result().isContents());
        assertFalse(resultStep.result().isText());
        assertNull(resultStep.resultText());

        List<Content> contents = resultStep.resultContents();
        assertNotNull(contents);
        assertEquals(3, contents.size());

        TextContent textPart = assertInstanceOf(TextContent.class, contents.get(0));
        assertEquals("Generated chart successfully.\n", textPart.text());

        ImageContent imagePart = assertInstanceOf(ImageContent.class, contents.get(1));
        assertEquals("https://example.com/plot.png", imagePart.uri());
        assertEquals("image/png", imagePart.mimeType());

        DocumentContent docPart = assertInstanceOf(DocumentContent.class, contents.get(2));
        assertEquals("https://example.com/report.xlsx", docPart.uri());
        assertEquals(DocumentContent.APPLICATION_XLSX, docPart.mimeType());

        // Round-trip serialization
        String serialized = mapper.writeValueAsString(resultStep);
        CodeExecutionResultStep roundTripped = mapper.readValue(serialized, CodeExecutionResultStep.class);
        assertTrue(roundTripped.result().isContents());
        assertEquals(3, roundTripped.resultContents().size());
    }

    @Test
    void testCodeExecutionResultConstructorsAndFactories() {
        // String convenience constructor
        CodeExecutionResultStep textStep = new CodeExecutionResultStep(
            "code_execution_result",
            "call_1",
            "Hello world\n",
            false,
            null
        );
        assertTrue(textStep.result().isText());
        assertEquals("Hello world\n", textStep.resultText());

        // Content list convenience constructor
        List<Content> parts = List.of(new TextContent("Output line"));
        CodeExecutionResultStep contentStep = new CodeExecutionResultStep(
            "code_execution_result",
            "call_2",
            parts,
            false,
            null
        );
        assertTrue(contentStep.result().isContents());
        assertEquals(1, contentStep.resultContents().size());

        // Factory methods on CodeExecutionResult
        CodeExecutionResult fromVarargs = CodeExecutionResult.of(new TextContent("A"), new TextContent("B"));
        assertTrue(fromVarargs.isContents());
        assertEquals(2, fromVarargs.contents().size());

        CodeExecutionResult fromObjString = CodeExecutionResult.of((Object) "plain");
        assertTrue(fromObjString.isText());
        assertEquals("plain", fromObjString.text());

        CodeExecutionResult fromObjContent = CodeExecutionResult.of((Object) new TextContent("single"));
        assertTrue(fromObjContent.isContents());
        assertEquals(1, fromObjContent.contents().size());
    }

    @Test
    void testSmartTranscriptionModeWithSourceText() throws Exception {
        SmartTranscriptionMode smartMode = SmartTranscriptionMode.withSourceText(
            "The quick brown fox jumps over the lazy dog."
        );
        assertEquals("smart", smartMode.type());
        assertEquals("The quick brown fox jumps over the lazy dog.", smartMode.sourceText());

        TranscriptionConfig config = TranscriptionConfig.builder()
            .languageCodes(List.of("en-US"))
            .mode(smartMode)
            .build();

        String json = mapper.writeValueAsString(config);
        assertTrue(json.contains("\"type\":\"smart\""));
        assertTrue(json.contains("\"source_text\":\"The quick brown fox jumps over the lazy dog.\""));

        TranscriptionConfig deserialized = mapper.readValue(json, TranscriptionConfig.class);
        assertNotNull(deserialized.mode());
        assertTrue(deserialized.mode().isCustom());
        SmartTranscriptionMode deserializedSmart = assertInstanceOf(
            SmartTranscriptionMode.class,
            deserialized.mode().config()
        );
        assertEquals("smart", deserializedSmart.type());
        assertEquals("The quick brown fox jumps over the lazy dog.", deserializedSmart.sourceText());

        // Also test constructor without source_text for backward compatibility
        SmartTranscriptionMode defaultSmart = new SmartTranscriptionMode();
        assertEquals("smart", defaultSmart.type());
        assertNull(defaultSmart.sourceText());
    }

    @Test
    void testDocumentContentExpandedMimeTypesAndConvenienceConstructors() throws Exception {
        byte[] sampleBytes = "col1,col2\n1,2".getBytes(StandardCharsets.UTF_8);
        DocumentContent csvDoc = new DocumentContent(sampleBytes, DocumentContent.TEXT_CSV);
        assertEquals("document", csvDoc.type());
        assertArrayEquals(sampleBytes, csvDoc.data());
        assertNull(csvDoc.uri());
        assertEquals("text/csv", csvDoc.mimeType());

        DocumentContent docxDoc = new DocumentContent(
            "https://example.com/spec.docx",
            DocumentContent.APPLICATION_DOCX
        );
        assertEquals("document", docxDoc.type());
        assertNull(docxDoc.data());
        assertEquals("https://example.com/spec.docx", docxDoc.uri());
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", docxDoc.mimeType());

        // Verify all 12 constants match the OpenAPI enum values
        List<String> expectedMimeTypes = List.of(
            DocumentContent.APPLICATION_PDF,
            DocumentContent.TEXT_CSV,
            DocumentContent.APPLICATION_PPTX,
            DocumentContent.APPLICATION_PPT,
            DocumentContent.APPLICATION_ODP,
            DocumentContent.APPLICATION_DOCX,
            DocumentContent.APPLICATION_DOC,
            DocumentContent.APPLICATION_DOTX,
            DocumentContent.APPLICATION_ODT,
            DocumentContent.APPLICATION_XLSX,
            DocumentContent.APPLICATION_XLS,
            DocumentContent.APPLICATION_ODS
        );
        assertEquals(12, expectedMimeTypes.size());

        for (String mime : expectedMimeTypes) {
            DocumentContent doc = new DocumentContent("https://example.com/file", mime);
            String json = mapper.writeValueAsString(doc);
            Content deserialized = mapper.readValue(json, Content.class);
            DocumentContent deserializedDoc = assertInstanceOf(DocumentContent.class, deserialized);
            assertEquals(mime, deserializedDoc.mimeType());
        }

        // Verify DocumentDelta deserialization with new Office MIME type
        String deltaJson = """
            {
                "event_type": "content.delta",
                "event_id": "evt_doc_1",
                "index": 0,
                "delta": {
                    "type": "document",
                    "uri": "https://example.com/slides.pptx",
                    "mime_type": "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                }
            }
            """;
        Events event = mapper.readValue(deltaJson, Events.class);
        Events.ContentDelta contentDelta = assertInstanceOf(Events.ContentDelta.class, event);
        Events.DocumentDelta docDelta = assertInstanceOf(Events.DocumentDelta.class, contentDelta.delta());
        assertEquals(DocumentContent.APPLICATION_PPTX, docDelta.mimeType());
        assertEquals("https://example.com/slides.pptx", docDelta.uri());
    }
}
