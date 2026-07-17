package io.github.glaforge.gemini.interactions;

import io.github.glaforge.gemini.interactions.model.Trigger;
import io.github.glaforge.gemini.interactions.model.TriggerCreateParams;
import io.github.glaforge.gemini.interactions.model.InteractionParams;
import io.github.glaforge.gemini.interactions.model.Config;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TriggersTest {

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
    void testCreateTrigger() throws Exception {
        String mockResponseJson = """
                {
                  "id": "trigger-123",
                  "display_name": "Daily Audit",
                  "schedule": "0 0 * * *",
                  "status": "active"
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(mockResponseJson)
                .addHeader("Content-Type", "application/json"));

        TriggerCreateParams params = TriggerCreateParams.builder()
                .displayName("Daily Audit")
                .schedule("0 0 * * *")
                .interaction(InteractionParams.AgentInteractionParams.builder()
                        .agent("antigravity-preview")
                        .input("audit modules")
                        .agentConfig(new Config.AntigravityAgentConfig(10000L))
                        .build())
                .build();

        Trigger trigger = client.createTrigger(params);

        assertNotNull(trigger);
        assertEquals("trigger-123", trigger.id());
        assertEquals("Daily Audit", trigger.displayName());
        assertEquals("0 0 * * *", trigger.schedule());
        assertEquals(Trigger.Status.ACTIVE, trigger.status());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/v1beta/triggers", recordedRequest.getPath());
        assertEquals("test-api-key", recordedRequest.getHeader("x-goog-api-key"));
        
        String requestBody = recordedRequest.getBody().readUtf8();
        assertTrue(requestBody.contains("Daily Audit"));
        assertTrue(requestBody.contains("0 0 * * *"));
        assertTrue(requestBody.contains("antigravity-preview"));
    }

    @Test
    void testGetTrigger() throws Exception {
        String mockResponseJson = """
                {
                  "id": "trigger-123",
                  "display_name": "Daily Audit",
                  "status": "active"
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(mockResponseJson)
                .addHeader("Content-Type", "application/json"));

        Trigger trigger = client.getTrigger("trigger-123");

        assertNotNull(trigger);
        assertEquals("trigger-123", trigger.id());

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/v1beta/triggers/trigger-123", recordedRequest.getPath());
    }
}
