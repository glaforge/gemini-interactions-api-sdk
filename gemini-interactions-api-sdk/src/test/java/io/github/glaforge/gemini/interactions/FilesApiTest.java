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

import com.google.auth.oauth2.GoogleCredentials;
import io.github.glaforge.gemini.interactions.model.Content.AudioContent;
import io.github.glaforge.gemini.interactions.model.Content.AudioStaticMediaProcessing;
import io.github.glaforge.gemini.interactions.model.Content.DocumentContent;
import io.github.glaforge.gemini.interactions.model.Content.DocumentStaticMediaProcessing;
import io.github.glaforge.gemini.interactions.model.Content.ImageContent;
import io.github.glaforge.gemini.interactions.model.Content.PageContentLayer;
import io.github.glaforge.gemini.interactions.model.Content.PageProcessingOptions;
import io.github.glaforge.gemini.interactions.model.Content.Resolution;
import io.github.glaforge.gemini.interactions.model.Content.StaticMediaProcessing;
import io.github.glaforge.gemini.interactions.model.Content.VideoContent;
import io.github.glaforge.gemini.interactions.model.Content.VideoContentLayer;
import io.github.glaforge.gemini.interactions.model.GeminiFile;
import io.github.glaforge.gemini.interactions.model.ListFilesResponse;
import io.github.glaforge.gemini.interactions.model.UploadFileConfig;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilesApiTest {

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
    void testUploadFileFromPathAutoDetectsMimeType(@TempDir Path tempDir) throws Exception {
        Path docxFile = tempDir.resolve("quarterly_report.docx");
        byte[] content = "fake-docx-bytes".getBytes(StandardCharsets.UTF_8);
        Files.write(docxFile, content);

        String uploadSessionUrl = mockServer.url("/upload/v1beta/files?upload_id=session-123").toString();

        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("X-Goog-Upload-URL", uploadSessionUrl)
            .setBody(""));

        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody("""
                {
                  "file": {
                    "name": "files/report-123",
                    "displayName": "Q4 Report",
                    "mimeType": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "sizeBytes": "15",
                    "createTime": "2026-11-23T10:00:00Z",
                    "updateTime": "2026-11-23T10:00:00Z",
                    "expirationTime": "2026-11-25T10:00:00Z",
                    "sha256Hash": "YWJj",
                    "uri": "https://generativelanguage.googleapis.com/v1beta/files/report-123",
                    "state": "ACTIVE",
                    "source": "UPLOADED"
                  }
                }
                """));

        GeminiFile uploaded = client.uploadFile(
            docxFile,
            UploadFileConfig.builder()
                .displayName("Q4 Report")
                .name("report-123")
                .build()
        );

        assertEquals("files/report-123", uploaded.name());
        assertEquals("Q4 Report", uploaded.displayName());
        assertEquals(DocumentContent.APPLICATION_DOCX, uploaded.mimeType());
        assertEquals(15L, uploaded.sizeBytes());
        assertEquals("https://generativelanguage.googleapis.com/v1beta/files/report-123", uploaded.uri());
        assertEquals(GeminiFile.State.ACTIVE, uploaded.state());
        assertEquals(GeminiFile.Source.UPLOADED, uploaded.source());
        assertTrue(uploaded.isActive());
        assertFalse(uploaded.isProcessing());
        assertFalse(uploaded.isFailed());

        RecordedRequest startReq = mockServer.takeRequest();
        assertEquals("POST", startReq.getMethod());
        assertEquals("/upload/v1beta/files", startReq.getPath());
        assertEquals("test-api-key", startReq.getHeader("x-goog-api-key"));
        assertNull(startReq.getHeader("Api-Revision"));
        assertEquals("resumable", startReq.getHeader("X-Goog-Upload-Protocol"));
        assertEquals("start", startReq.getHeader("X-Goog-Upload-Command"));
        assertEquals(String.valueOf(content.length), startReq.getHeader("X-Goog-Upload-Header-Content-Length"));
        assertEquals(DocumentContent.APPLICATION_DOCX, startReq.getHeader("X-Goog-Upload-Header-Content-Type"));

        JsonNode startBody = mapper.readTree(startReq.getBody().readUtf8());
        assertEquals("Q4 Report", startBody.get("file").get("displayName").asText());
        assertEquals("files/report-123", startBody.get("file").get("name").asText());

        RecordedRequest uploadReq = mockServer.takeRequest();
        assertEquals("POST", uploadReq.getMethod());
        assertEquals("/upload/v1beta/files?upload_id=session-123", uploadReq.getPath());
        assertEquals("0", uploadReq.getHeader("X-Goog-Upload-Offset"));
        assertEquals("upload, finalize", uploadReq.getHeader("X-Goog-Upload-Command"));
        assertArrayEquals(content, uploadReq.getBody().readByteArray());
    }

    @Test
    void testUploadFileFromBytes() throws Exception {
        byte[] pdfBytes = "%PDF-1.4 sample".getBytes(StandardCharsets.UTF_8);
        String uploadSessionUrl = mockServer.url("/upload/v1beta/files?upload_id=pdf-1").toString();

        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("X-Goog-Upload-URL", uploadSessionUrl));
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                  "file": {
                    "name": "files/pdf-1",
                    "mimeType": "application/pdf",
                    "uri": "https://generativelanguage.googleapis.com/v1beta/files/pdf-1",
                    "state": "ACTIVE"
                  }
                }
                """));

        GeminiFile file = client.uploadFile(pdfBytes, DocumentContent.APPLICATION_PDF, "Sample PDF");
        assertEquals("files/pdf-1", file.name());
        assertEquals("application/pdf", file.mimeType());

        RecordedRequest startReq = mockServer.takeRequest();
        assertEquals("application/pdf", startReq.getHeader("X-Goog-Upload-Header-Content-Type"));
        JsonNode meta = mapper.readTree(startReq.getBody().readUtf8());
        assertEquals("Sample PDF", meta.get("file").get("displayName").asText());

        RecordedRequest dataReq = mockServer.takeRequest();
        assertArrayEquals(pdfBytes, dataReq.getBody().readByteArray());
    }

    @Test
    void testUploadFileFromInputStream() throws Exception {
        byte[] payload = "streamed audio payload".getBytes(StandardCharsets.UTF_8);
        String uploadSessionUrl = mockServer.url("/upload/v1beta/files?upload_id=audio-1").toString();

        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("X-Goog-Upload-URL", uploadSessionUrl));
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                  "file": {
                    "name": "files/audio-1",
                    "mimeType": "audio/mpeg",
                    "uri": "https://generativelanguage.googleapis.com/v1beta/files/audio-1",
                    "state": "ACTIVE"
                  }
                }
                """));

        GeminiFile file = client.uploadFile(
            new ByteArrayInputStream(payload),
            payload.length,
            "audio/mpeg",
            "Podcast Clip"
        );
        assertEquals("files/audio-1", file.name());
        assertEquals("audio/mpeg", file.mimeType());

        RecordedRequest startReq = mockServer.takeRequest();
        assertEquals(String.valueOf(payload.length), startReq.getHeader("X-Goog-Upload-Header-Content-Length"));
        RecordedRequest dataReq = mockServer.takeRequest();
        assertEquals("0", dataReq.getHeader("X-Goog-Upload-Offset"));
        assertEquals("upload, finalize", dataReq.getHeader("X-Goog-Upload-Command"));
        assertArrayEquals(payload, dataReq.getBody().readByteArray());
    }

    @Test
    void testGetListAndDeleteFile() throws Exception {
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                  "name": "files/vid-42",
                  "mimeType": "video/mp4",
                  "uri": "https://generativelanguage.googleapis.com/v1beta/files/vid-42",
                  "state": "ACTIVE",
                  "videoMetadata": {
                    "videoDuration": "12.5s"
                  }
                }
                """));

        GeminiFile fetched = client.getFile("vid-42");
        assertEquals("files/vid-42", fetched.name());
        assertNotNull(fetched.videoMetadata());
        assertEquals("12.5s", fetched.videoMetadata().videoDuration());

        RecordedRequest getReq = mockServer.takeRequest();
        assertEquals("GET", getReq.getMethod());
        assertEquals("/v1beta/files/vid-42", getReq.getPath());

        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                  "files": [
                    {"name": "files/vid-42", "mimeType": "video/mp4", "state": "ACTIVE"}
                  ],
                  "nextPageToken": "tok+2"
                }
                """));

        ListFilesResponse listed = client.listFiles(25, "tok 1");
        assertEquals(1, listed.files().size());
        assertEquals("files/vid-42", listed.files().get(0).name());
        assertEquals("tok+2", listed.nextPageToken());

        RecordedRequest listReq = mockServer.takeRequest();
        assertEquals("GET", listReq.getMethod());
        assertEquals("/v1beta/files?pageSize=25&pageToken=tok+1", listReq.getPath());

        // Empty list response defaults files to empty list
        mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        ListFilesResponse empty = client.listFiles();
        assertNotNull(empty.files());
        assertTrue(empty.files().isEmpty());
        assertNull(empty.nextPageToken());
        mockServer.takeRequest();

        // Delete
        mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        client.deleteFile("files/vid-42");
        RecordedRequest deleteReq = mockServer.takeRequest();
        assertEquals("DELETE", deleteReq.getMethod());
        assertEquals("/v1beta/files/vid-42", deleteReq.getPath());
    }

    @Test
    void testWaitForFileActiveTransitionsAndErrors() throws Exception {
        // Transition PROCESSING -> ACTIVE
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("""
                {"name": "files/v1", "mimeType": "video/mp4", "state": "PROCESSING"}
                """));
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("""
                {"name": "files/v1", "mimeType": "video/mp4", "state": "ACTIVE", "uri": "https://example.com/files/v1"}
                """));

        GeminiFile ready = client.waitForFileActive("files/v1", Duration.ofMillis(10), Duration.ofSeconds(5));
        assertTrue(ready.isActive());
        assertEquals("https://example.com/files/v1", ready.uri());

        // FAILED state throws immediately with message
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                  "name": "files/v2",
                  "state": "FAILED",
                  "error": {
                    "code": 3,
                    "message": "Unsupported codec"
                  }
                }
                """));

        GeminiInteractionsException failedEx = assertThrows(
            GeminiInteractionsException.class,
            () -> client.waitForFileActive("files/v2", Duration.ofMillis(10), Duration.ofSeconds(5))
        );
        assertTrue(failedEx.getMessage().contains("Unsupported codec"));
    }

    @Test
    void testRegisterAndDownloadFile(@TempDir Path tempDir) throws Exception {
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("""
                {
                  "files": [
                    {
                      "name": "files/gcs-1",
                      "uri": "https://generativelanguage.googleapis.com/v1beta/files/gcs-1",
                      "mimeType": "application/pdf",
                      "source": "REGISTERED",
                      "state": "ACTIVE"
                    }
                  ]
                }
                """));

        List<GeminiFile> registered = client.registerFiles(List.of("gs://my-bucket/report.pdf"));
        assertEquals(1, registered.size());
        assertEquals(GeminiFile.Source.REGISTERED, registered.get(0).source());

        RecordedRequest regReq = mockServer.takeRequest();
        assertEquals("POST", regReq.getMethod());
        assertEquals("/v1beta/files:register", regReq.getPath());
        JsonNode regBody = mapper.readTree(regReq.getBody().readUtf8());
        assertEquals("gs://my-bucket/report.pdf", regBody.get("uris").get(0).asText());

        byte[] generatedBytes = "generated-video-bytes".getBytes(StandardCharsets.UTF_8);
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(new String(generatedBytes, StandardCharsets.UTF_8)));

        Path out = tempDir.resolve("nested/video.mp4");
        client.downloadFile("files/gen-1", out);
        assertArrayEquals(generatedBytes, Files.readAllBytes(out));

        RecordedRequest dlReq = mockServer.takeRequest();
        assertEquals("GET", dlReq.getMethod());
        assertEquals("/v1beta/files/gen-1:download?alt=media", dlReq.getPath());

        // InputStream variant
        mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("ok"));
        try (InputStream in = client.downloadFile("gen-1")) {
            assertEquals("ok", new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void testGeminiFileContentConversionHelpers() {
        GeminiFile docFile = new GeminiFile(
            "files/doc-1", "Report", DocumentContent.APPLICATION_PDF, 1024L,
            null, null, null, null,
            "https://generativelanguage.googleapis.com/v1beta/files/doc-1",
            null, GeminiFile.State.ACTIVE, GeminiFile.Source.UPLOADED, null, null
        );
        DocumentStaticMediaProcessing slicing = DocumentStaticMediaProcessing.forPages(
            PageProcessingOptions.builder()
                .startPage(3)
                .endPage(5)
                .contentLayers(PageContentLayer.PAGE_IMAGE)
                .build()
        );
        DocumentContent docContent = docFile.toDocumentContent(Resolution.HIGH, slicing);
        assertEquals(docFile.uri(), docContent.uri());
        assertEquals(DocumentContent.APPLICATION_PDF, docContent.mimeType());
        assertEquals(Resolution.HIGH, docContent.resolution());
        assertNotNull(docContent.processing());

        GeminiFile imgFile = new GeminiFile(
            "files/img-1", null, "image/png", 512L,
            null, null, null, null,
            "https://generativelanguage.googleapis.com/v1beta/files/img-1",
            null, GeminiFile.State.ACTIVE, GeminiFile.Source.UPLOADED, null, null
        );
        ImageContent imgContent = imgFile.toImageContent(Resolution.MEDIUM);
        assertEquals(imgFile.uri(), imgContent.uri());
        assertEquals(Resolution.MEDIUM, imgContent.resolution());

        GeminiFile audioFile = new GeminiFile(
            "files/aud-1", null, "audio/mpeg", 2048L,
            null, null, null, null,
            "https://generativelanguage.googleapis.com/v1beta/files/aud-1",
            null, GeminiFile.State.ACTIVE, GeminiFile.Source.UPLOADED, null, null
        );
        AudioContent audioContent = audioFile.toAudioContent(new AudioStaticMediaProcessing("5s", "20s"));
        assertEquals(audioFile.uri(), audioContent.uri());
        assertNotNull(audioContent.processing());

        GeminiFile videoFile = new GeminiFile(
            "files/vid-1", null, "video/mp4", 4096L,
            null, null, null, null,
            "https://generativelanguage.googleapis.com/v1beta/files/vid-1",
            null, GeminiFile.State.ACTIVE, GeminiFile.Source.UPLOADED, null,
            new GeminiFile.VideoMetadata("60s")
        );
        VideoContent videoContent = videoFile.toVideoContent(
            Resolution.LOW,
            new StaticMediaProcessing("0s", "10s", 1.0, List.of(VideoContentLayer.FRAMES, VideoContentLayer.TRANSCRIPT))
        );
        assertEquals(videoFile.uri(), videoContent.uri());
        assertEquals(Resolution.LOW, videoContent.resolution());
        assertNotNull(videoContent.processing());
    }

    @Test
    void testMissingUploadUrlHeaderThrows() {
        mockServer.enqueue(new MockResponse().setResponseCode(200).setBody(""));
        GeminiInteractionsException ex = assertThrows(
            GeminiInteractionsException.class,
            () -> client.uploadFile("hello".getBytes(StandardCharsets.UTF_8), "text/plain")
        );
        assertTrue(ex.getMessage().contains("X-Goog-Upload-URL"));
    }

    @Test
    void testVertexAiClientRejectsFilesApi() {
        GeminiInteractionsClient vertexClient = GeminiInteractionsClient.builder()
            .project("my-gcp-project")
            .location("us-central1")
            .credentials(GoogleCredentials.create(null))
            .build();

        assertThrows(UnsupportedOperationException.class, vertexClient::listFiles);
        assertThrows(UnsupportedOperationException.class, () -> vertexClient.getFile("files/abc"));
        assertThrows(UnsupportedOperationException.class, () -> vertexClient.deleteFile("files/abc"));
        assertThrows(
            UnsupportedOperationException.class,
            () -> vertexClient.uploadFile("hi".getBytes(StandardCharsets.UTF_8), "text/plain")
        );
    }
}
