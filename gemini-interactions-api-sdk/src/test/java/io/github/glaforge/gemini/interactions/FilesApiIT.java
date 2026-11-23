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

import io.github.glaforge.gemini.interactions.model.Content.DocumentContent;
import io.github.glaforge.gemini.interactions.model.Content.DocumentStaticMediaProcessing;
import io.github.glaforge.gemini.interactions.model.Content.PageContentLayer;
import io.github.glaforge.gemini.interactions.model.Content.PageProcessingOptions;
import io.github.glaforge.gemini.interactions.model.Content.Resolution;
import io.github.glaforge.gemini.interactions.model.Content.TextContent;
import io.github.glaforge.gemini.interactions.model.GeminiFile;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams.ModelInteractionParams;
import io.github.glaforge.gemini.interactions.model.ListFilesResponse;
import io.github.glaforge.gemini.interactions.model.ModelOption;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class FilesApiIT {

    private final GeminiInteractionsClient client = GeminiInteractionsClient.builder()
        .apiKey(System.getenv("GEMINI_API_KEY"))
        .build();

    @Test
    void testUploadSliceAndDeletePdfFile() {
        byte[] pdfBytes = createMinimalTwoPagePdf();
        GeminiFile uploaded = client.uploadFile(pdfBytes, DocumentContent.APPLICATION_PDF, "Two Page Test PDF");
        try {
            assertNotNull(uploaded.name());
            assertNotNull(uploaded.uri());
            assertEquals(DocumentContent.APPLICATION_PDF, uploaded.mimeType());

            GeminiFile active = client.waitForFileActive(uploaded.name());
            assertTrue(active.isActive());

            GeminiFile fetched = client.getFile(uploaded.name());
            assertEquals(uploaded.name(), fetched.name());

            ListFilesResponse listed = client.listFiles(10, null);
            assertNotNull(listed.files());
            assertTrue(listed.files().stream().anyMatch(f -> uploaded.name().equals(f.name())));

            // Slice only page 2 as extracted text using Gemini 4 Hydrogen
            DocumentStaticMediaProcessing pageTwoOnly = DocumentStaticMediaProcessing.forPages(
                PageProcessingOptions.builder()
                    .startPage(2)
                    .endPage(2)
                    .contentLayers(PageContentLayer.PAGE_TEXT)
                    .build()
            );

            Interaction response = client.create(ModelInteractionParams.builder()
                .model(ModelOption.GEMINI_4_HYDROGEN)
                .input(
                    active.toDocumentContent(Resolution.LOW, pageTwoOnly),
                    new TextContent("What secret code appears on this page? Reply with only the code.")
                )
                .build());

            String answer = response.outputText();
            assertNotNull(answer);
            assertTrue(answer.contains("ORBIT-742"), "Expected page 2 secret code ORBIT-742, got: " + answer);
        } finally {
            client.deleteFile(uploaded.name());
        }
    }

    /**
     * Generates a valid minimal 2-page PDF in memory with distinct text on page 1 and page 2.
     */
    private static byte[] createMinimalTwoPagePdf() {
        String p1Stream = "BT /F1 18 Tf 72 720 Td (Page One: ALPHA-101) Tj ET";
        String p2Stream = "BT /F1 18 Tf 72 720 Td (Page Two: ORBIT-742) Tj ET";

        StringBuilder pdf = new StringBuilder();
        pdf.append("%PDF-1.4\n");

        int[] offsets = new int[8];
        offsets[1] = pdf.length();
        pdf.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        offsets[2] = pdf.length();
        pdf.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R 4 0 R] /Count 2 >>\nendobj\n");

        offsets[3] = pdf.length();
        pdf.append("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 5 0 R >> >> /Contents 6 0 R >>\nendobj\n");

        offsets[4] = pdf.length();
        pdf.append("4 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 5 0 R >> >> /Contents 7 0 R >>\nendobj\n");

        offsets[5] = pdf.length();
        pdf.append("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        offsets[6] = pdf.length();
        pdf.append("6 0 obj\n<< /Length ").append(p1Stream.length()).append(" >>\nstream\n")
            .append(p1Stream).append("\nendstream\nendobj\n");

        offsets[7] = pdf.length();
        pdf.append("7 0 obj\n<< /Length ").append(p2Stream.length()).append(" >>\nstream\n")
            .append(p2Stream).append("\nendstream\nendobj\n");

        int xrefOffset = pdf.length();
        pdf.append("xref\n0 8\n");
        pdf.append("0000000000 65535 f \n");
        for (int i = 1; i <= 7; i++) {
            pdf.append(String.format("%010d 00000 n \n", offsets[i]));
        }
        pdf.append("trailer\n<< /Size 8 /Root 1 0 R >>\nstartxref\n")
            .append(xrefOffset).append("\n%%EOF\n");

        return pdf.toString().getBytes(StandardCharsets.US_ASCII);
    }
}
