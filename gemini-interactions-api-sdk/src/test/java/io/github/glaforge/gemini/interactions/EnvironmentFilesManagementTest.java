package io.github.glaforge.gemini.interactions;

import io.github.glaforge.gemini.interactions.model.EnvironmentFile;
import io.github.glaforge.gemini.interactions.model.GetEnvironmentFilesResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnvironmentFilesManagementTest {

    private MockWebServer mockWebServer;
    private GeminiInteractionsClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        client = GeminiInteractionsClient.builder()
                .apiKey("test-api-key")
                .baseUrl(mockWebServer.url("/").toString().replaceAll("/$", ""))
                .build();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testDownloadSingleFileStream() throws Exception {
        byte[] expectedContent = "print('Hello world!')\n".getBytes(StandardCharsets.UTF_8);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/x-python")
                .setBody(new Buffer().write(expectedContent)));

        try (InputStream in = client.downloadEnvironmentFile("env_123", "src/main.py")) {
            byte[] bytes = in.readAllBytes();
            assertArrayEquals(expectedContent, bytes);
        }

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/v1beta/environments/env_123/files/src/main.py?alt=media", request.getPath());
    }

    @Test
    void testDownloadDirectoryArchiveRecursive() throws Exception {
        byte[] mockTarBytes = new byte[]{1, 2, 3, 4, 5};
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/x-tar")
                .setBody(new Buffer().write(mockTarBytes)));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        client.downloadEnvironmentFile("env_123", "src", true, out);

        assertArrayEquals(mockTarBytes, out.toByteArray());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/v1beta/environments/env_123/files/src?alt=media&recursive=true", request.getPath());
    }

    @Test
    void testDownloadFileBytesAndSaveToPath(@TempDir Path tempDir) throws Exception {
        byte[] expectedBytes = "console.log('hi');".getBytes(StandardCharsets.UTF_8);
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new Buffer().write(expectedBytes)));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new Buffer().write(expectedBytes)));

        byte[] downloadedBytes = client.downloadEnvironmentFileBytes("env_123", "index.js");
        assertArrayEquals(expectedBytes, downloadedBytes);

        Path destFile = tempDir.resolve("sub/index.js");
        client.downloadEnvironmentFile("env_123", "index.js", destFile);
        assertTrue(Files.exists(destFile));
        assertEquals("console.log('hi');", Files.readString(destFile));
    }

    @Test
    void testGetEnvironmentFileConvenience() throws Exception {
        String json = """
                {
                  "files": [
                    {
                      "name": "main.py",
                      "path": "src/main.py",
                      "type": "FILE",
                      "size_bytes": "22",
                      "mime_type": "text/x-python"
                    }
                  ]
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(json));

        Optional<EnvironmentFile> file = client.getEnvironmentFile("env_123", "src/main.py");
        assertTrue(file.isPresent());
        assertEquals("main.py", file.get().name());
        assertEquals("src/main.py", file.get().path());
        assertTrue(file.get().isFile());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/v1beta/environments/env_123/files/src/main.py", request.getPath());
    }

    @Test
    void testUploadSingleFileText() throws Exception {
        String jsonResponse = """
                {
                  "files": [
                    {
                      "name": "data.txt",
                      "path": "workspace/data.txt",
                      "type": "FILE",
                      "size_bytes": "12",
                      "mime_type": "text/plain; charset=utf-8"
                    }
                  ]
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(jsonResponse));

        GetEnvironmentFilesResponse response = client.uploadEnvironmentFile("env_123", "workspace/data.txt", "sample text", true);

        assertNotNull(response);
        assertEquals(1, response.files().size());
        assertEquals("data.txt", response.files().get(0).name());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("PUT", request.getMethod());
        assertEquals("/upload/v1beta/environments/env_123/files/workspace/data.txt?overwrite=true", request.getPath());
        assertEquals("text/plain; charset=utf-8", request.getHeader("Content-Type"));
        assertEquals("sample text", request.getBody().readUtf8());
    }

    @Test
    void testUploadArchiveExtract() throws Exception {
        String jsonResponse = """
                {
                  "files": [
                    {
                      "name": "app.py",
                      "path": "workspace/src/app.py",
                      "type": "FILE",
                      "size_bytes": "100"
                    },
                    {
                      "name": "requirements.txt",
                      "path": "workspace/src/requirements.txt",
                      "type": "FILE",
                      "size_bytes": "50"
                    }
                  ]
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(jsonResponse));

        byte[] fakeTar = new byte[]{10, 20, 30};
        GetEnvironmentFilesResponse response = client.uploadEnvironmentArchive("env_123", "workspace/src/", fakeTar, true);

        assertNotNull(response);
        assertEquals(2, response.files().size());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("PUT", request.getMethod());
        assertEquals("/upload/v1beta/environments/env_123/files/workspace/src/?overwrite=true&extract=true", request.getPath());
        assertEquals("application/x-tar", request.getHeader("Content-Type"));
        assertArrayEquals(fakeTar, request.getBody().readByteArray());
    }

    @Test
    void testUploadConflictException() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(409)
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "error": {
                            "code": "aborted",
                            "message": "Requested entity already exists"
                          }
                        }
                        """));

        GeminiInteractionsException ex = assertThrows(GeminiInteractionsException.class, () ->
                client.uploadEnvironmentFile("env_123", "file.txt", "new text", false));

        assertEquals(409, ex.getStatusCode());
        assertTrue(ex.getBody().contains("Requested entity already exists"));
    }

    @Test
    void testDownloadEnvironmentModernEndpoint() throws Exception {
        byte[] expectedTar = new byte[]{1, 2, 3};
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new Buffer().write(expectedTar)));

        try (InputStream in = client.downloadEnvironment("env_123")) {
            assertArrayEquals(expectedTar, in.readAllBytes());
        }

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/v1beta/environments/env_123/files?alt=media", request.getPath());
    }

    @Test
    void testDownloadEnvironmentFallbackToLegacyEndpoint() throws Exception {
        // Modern endpoint returns 404 (e.g. environment not found on modern route)
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Not found"));

        // Legacy endpoint returns 200
        byte[] expectedTar = new byte[]{9, 8, 7};
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new Buffer().write(expectedTar)));

        try (InputStream in = client.downloadEnvironment("env_legacy_999")) {
            assertArrayEquals(expectedTar, in.readAllBytes());
        }

        RecordedRequest req1 = mockWebServer.takeRequest();
        assertEquals("GET", req1.getMethod());
        assertEquals("/v1beta/environments/env_legacy_999/files?alt=media", req1.getPath());

        RecordedRequest req2 = mockWebServer.takeRequest();
        assertEquals("GET", req2.getMethod());
        assertEquals("/v1beta/files/environment-env_legacy_999:download?alt=media", req2.getPath());
    }

    @Test
    void testDownloadEnvironmentInteractionIdDirectToLegacy() throws Exception {
        byte[] expectedTar = new byte[]{4, 5, 6};
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new Buffer().write(expectedTar)));

        try (InputStream in = client.downloadEnvironment("interaction-123")) {
            assertArrayEquals(expectedTar, in.readAllBytes());
        }

        RecordedRequest req = mockWebServer.takeRequest();
        assertEquals("GET", req.getMethod());
        assertEquals("/v1beta/files/environment-interaction-123:download?alt=media", req.getPath());
    }
}
