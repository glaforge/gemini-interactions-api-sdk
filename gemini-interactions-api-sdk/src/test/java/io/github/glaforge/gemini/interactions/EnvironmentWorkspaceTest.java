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

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnvironmentWorkspaceTest {

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
    void testTarParserAndEnvironmentWorkspaceLazyLoading(@TempDir Path tempDir) throws Exception {
        String filename = "docs/report.md";
        String content = "# My Report\nHello world from remote agent sandbox!";
        byte[] tarBytes = createMockTar(filename, content);

        // Save mock tar to verify TarParser directly
        Path mockTarPath = tempDir.resolve("mock.tar");
        Files.write(mockTarPath, tarBytes);

        // Test scanTar
        Map<String, TarParser.TarEntryMetadata> index = TarParser.scanTar(mockTarPath);
        assertTrue(index.containsKey(filename));
        TarParser.TarEntryMetadata meta = index.get(filename);
        assertEquals(content.getBytes(StandardCharsets.UTF_8).length, meta.size());
        assertEquals(512, meta.offset()); // Content starts right after 512-byte header

        // Mock download endpoint
        mockWebServer.enqueue(new MockResponse()
                .setBody(new okio.Buffer().write(tarBytes))
                .setResponseCode(200)
                .addHeader("Content-Type", "application/x-tar"));

        // Retrieve and verify EnvironmentWorkspace
        try (EnvironmentWorkspace env = client.getWorkspace("interaction-123")) {
            assertFalse(env.fileExists(filename));

            // Load from server
            env.refresh();

            // Verify requests
            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            assertEquals("GET", recordedRequest.getMethod());
            assertEquals("/v1beta/files/environment-interaction-123:download?alt=media", recordedRequest.getPath());

            // Verify metadata
            assertTrue(env.fileExists(filename));
            assertFalse(env.fileExists("nonexistent.txt"));
            Set<String> files = env.listFiles();
            assertEquals(1, files.size());
            assertTrue(files.contains(filename));

            // Verify text contents
            String text = env.readTextFile(filename);
            assertEquals(content, text);

            // Verify binary contents
            byte[] binary = env.readBinaryFile(filename);
            assertArrayEquals(content.getBytes(StandardCharsets.UTF_8), binary);

            // Verify empty values for non-existent files
            assertEquals("", env.readTextFile("nonexistent.txt"));
            assertArrayEquals(new byte[0], env.readBinaryFile("nonexistent.txt"));

            // Verify file extraction download
            Path localTarget = tempDir.resolve("extracted-report.md");
            env.downloadFile(filename, localTarget);
            assertTrue(Files.exists(localTarget));
            assertEquals(content, Files.readString(localTarget));
        }
    }

    private byte[] createMockTar(String filename, String content) throws Exception {
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        byte[] header = new byte[512];

        // Filename
        byte[] nameBytes = filename.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(nameBytes, 0, header, 0, Math.min(nameBytes.length, 99));

        // Size in octal (12 bytes)
        String sizeStr = String.format("%011o", contentBytes.length);
        byte[] sizeBytes = sizeStr.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(sizeBytes, 0, header, 124, 11);

        // Type indicator '0' (regular file)
        header[156] = '0';

        // USTAR magic
        System.arraycopy("ustar".getBytes(StandardCharsets.UTF_8), 0, header, 257, 5);

        // Total block calculation
        int contentBlocks = (contentBytes.length + 511) / 512;
        int totalLength = 512 + contentBlocks * 512 + 1024; // header + content + 2 EOF blocks
        byte[] tarBytes = new byte[totalLength];

        System.arraycopy(header, 0, tarBytes, 0, 512);
        System.arraycopy(contentBytes, 0, tarBytes, 512, contentBytes.length);

        return tarBytes;
    }
}
