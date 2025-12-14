/*
 * Copyright 2025 Google LLC
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

package com.google.ai.gemini.interactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.ai.gemini.interactions.model.Interaction;
import com.google.ai.gemini.interactions.model.InteractionParams;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class GeminiInteractionsClientTest {

    private MockWebServer mockWebServer;
    private GeminiInteractionsClient client;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        client = GeminiInteractionsClient.builder()
            .apiKey("test-api-key")
            .baseUrl(mockWebServer.url("/").toString().replaceAll("/$", "")) // Remove trailing slash as client appends paths
            .build();

        objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testCreateInteraction() throws Exception {
        // Mock response
        String interactionJson = """
            {
              "id": "interaction-123",
              "model": "gemini-2.5-flash",
              "status": "completed",
              "outputs": [
                {
                  "type": "text",
                  "text": "Hello world"
                }
              ]
            }
            """;
        mockWebServer.enqueue(new MockResponse()
            .setBody(interactionJson)
            .addHeader("Content-Type", "application/json"));

        // Create request
        var params = InteractionParams.ModelInteractionParams.builder()
            .model("gemini-2.5-flash")
            .input("Hi")
            .build();

        // Execute
        Interaction interaction = client.create(params);

        // Verify result
        assertNotNull(interaction);
        assertEquals("interaction-123", interaction.id());
        assertEquals(Interaction.Status.COMPLETED, interaction.status());
        assertEquals("Hello world", ((com.google.ai.gemini.interactions.model.Content.TextContent) interaction.outputs().get(0)).text());

        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/v1beta/interactions", recordedRequest.getPath());
        assertEquals("test-api-key", recordedRequest.getHeader("x-goog-api-key"));

        // Check body contains expected fields
        String requestBody = recordedRequest.getBody().readUtf8();
        assertTrue(requestBody.contains("gemini-2.5-flash"));
        assertTrue(requestBody.contains("Hi"));
    }

    @Test
    void testGetInteraction() throws Exception {
        // Mock response
        String interactionJson = """
            {
              "id": "interaction-456",
              "model": "gemini-2.5-flash",
              "status": "in_progress"
            }
            """;
        mockWebServer.enqueue(new MockResponse()
            .setBody(interactionJson)
            .addHeader("Content-Type", "application/json"));

        // Execute
        Interaction interaction = client.get("interaction-456");

        // Verify result
        assertNotNull(interaction);
        assertEquals("interaction-456", interaction.id());
        assertEquals(Interaction.Status.IN_PROGRESS, interaction.status());

        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/v1beta/interactions/interaction-456", recordedRequest.getPath());
        assertEquals("test-api-key", recordedRequest.getHeader("x-goog-api-key"));
    }

    @Test
    void testCancelInteraction() throws Exception {
        // Mock response
        String interactionJson = """
            {
              "id": "interaction-789",
              "model": "gemini-2.5-flash",
              "status": "cancelled"
            }
            """;
        mockWebServer.enqueue(new MockResponse()
            .setBody(interactionJson)
            .addHeader("Content-Type", "application/json"));

        // Execute
        Interaction interaction = client.cancel("interaction-789");

        // Verify result
        assertNotNull(interaction);
        assertEquals("interaction-789", interaction.id());
        assertEquals(Interaction.Status.CANCELLED, interaction.status());

        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/v1beta/interactions/interaction-789/cancel", recordedRequest.getPath());
        assertEquals("test-api-key", recordedRequest.getHeader("x-goog-api-key"));
    }

    @Test
    void testDeleteInteraction() throws Exception {
        // Mock response
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{}"));

        // Execute
        client.delete("interaction-000");

        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("DELETE", recordedRequest.getMethod());
        assertEquals("/v1beta/interactions/interaction-000", recordedRequest.getPath());
        assertEquals("test-api-key", recordedRequest.getHeader("x-goog-api-key"));
    }

    @Test
    void testErrorHandling() {
        // Mock response
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(404)
            .setBody("{\"error\": \"Not Found\"}"));

        // Execute and Verify
        IOException exception = assertThrows(IOException.class, () -> {
            client.get("non-existent-id");
        });

        assertTrue(exception.getMessage().contains("404"));
        assertTrue(exception.getMessage().contains("Not Found"));
    }

    @Test
    void testBuilderValidation() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            GeminiInteractionsClient.builder()
                .baseUrl("http://localhost")
                .build();
        });

        assertEquals("API Key must be provided", exception.getMessage());
    }
}
