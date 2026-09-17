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

import io.github.glaforge.gemini.interactions.model.BearerTokenConfig;
import io.github.glaforge.gemini.interactions.model.Credential;
import io.github.glaforge.gemini.interactions.model.CredentialReference;
import io.github.glaforge.gemini.interactions.model.CredentialType;
import io.github.glaforge.gemini.interactions.model.CredentialUpdate;
import io.github.glaforge.gemini.interactions.model.EnvironmentConfig;
import io.github.glaforge.gemini.interactions.model.EnvironmentVariableConfig;
import io.github.glaforge.gemini.interactions.model.ListCredentialsResponse;
import io.github.glaforge.gemini.interactions.model.OAuth2Config;
import io.github.glaforge.gemini.interactions.model.Tool;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CredentialsTest {

    private MockWebServer mockWebServer;
    private GeminiInteractionsClient client;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        client = GeminiInteractionsClient.builder()
            .apiKey("test-api-key")
            .baseUrl(mockWebServer.url("/").toString().replaceAll("/$", ""))
            .build();

        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testCreateBearerTokenCredential() throws Exception {
        String responseJson = """
            {
              "id": "jira-api-token",
              "type": "bearer_token",
              "status": "active",
              "create_time": "2026-03-30T00:00:00Z",
              "update_time": "2026-03-30T00:00:00Z"
            }
            """;
        mockWebServer.enqueue(new MockResponse()
            .setBody(responseJson)
            .addHeader("Content-Type", "application/json"));

        Credential credential = Credential.builder()
            .id("jira-api-token")
            .bearerToken(BearerTokenConfig.builder()
                .token("secret-jira-token-12345")
                .headerName("Authorization")
                .prefix("Bearer")
                .build())
            .build();

        Credential created = client.createCredential(credential);

        assertNotNull(created);
        assertEquals("jira-api-token", created.id());
        assertEquals(CredentialType.BEARER_TOKEN, created.type());
        assertEquals(Credential.Status.ACTIVE, created.status());

        RecordedRequest req = mockWebServer.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/v1beta/credentials", req.getPath());
        assertEquals("test-api-key", req.getHeader("x-goog-api-key"));

        String body = req.getBody().readUtf8();
        assertTrue(body.contains("\"id\":\"jira-api-token\""));
        assertTrue(body.contains("\"type\":\"bearer_token\""));
        assertTrue(body.contains("\"token\":\"secret-jira-token-12345\""));
        assertTrue(body.contains("\"header_name\":\"Authorization\""));
    }

    @Test
    void testCreateOAuth2Credential() throws Exception {
        String responseJson = """
            {
              "id": "github-oauth",
              "type": "oauth2",
              "status": "active"
            }
            """;
        mockWebServer.enqueue(new MockResponse()
            .setBody(responseJson)
            .addHeader("Content-Type", "application/json"));

        Credential credential = Credential.oauth2("github-oauth", OAuth2Config.builder()
            .clientId("my-client-id")
            .clientSecret("my-client-secret")
            .refreshToken("my-refresh-token")
            .tokenUrl("https://github.com/login/oauth/access_token")
            .scopes("repo", "read:user")
            .build());

        Credential created = client.createCredential(credential);

        assertNotNull(created);
        assertEquals("github-oauth", created.id());
        assertEquals(CredentialType.OAUTH2, created.type());

        RecordedRequest req = mockWebServer.takeRequest();
        assertEquals("POST", req.getMethod());
        String body = req.getBody().readUtf8();
        assertTrue(body.contains("\"client_id\":\"my-client-id\""));
        assertTrue(body.contains("\"client_secret\":\"my-client-secret\""));
        assertTrue(body.contains("\"token_url\":\"https://github.com/login/oauth/access_token\""));
        assertTrue(body.contains("\"scopes\":[\"repo\",\"read:user\"]"));
    }

    @Test
    void testCreateEnvironmentVariableCredential() throws Exception {
        String responseJson = """
            {
              "id": "slack-token",
              "type": "environment_variable",
              "status": "active"
            }
            """;
        mockWebServer.enqueue(new MockResponse()
            .setBody(responseJson)
            .addHeader("Content-Type", "application/json"));

        Credential credential = Credential.environmentVariable("slack-token", EnvironmentVariableConfig.builder()
            .value("xoxb-secret-slack-token")
            .injectionLocation("header")
            .trustedDomains("*.slack.com", "slack.com")
            .build());

        Credential created = client.createCredential(credential);

        assertNotNull(created);
        assertEquals("slack-token", created.id());
        assertEquals(CredentialType.ENVIRONMENT_VARIABLE, created.type());

        RecordedRequest req = mockWebServer.takeRequest();
        String body = req.getBody().readUtf8();
        assertTrue(body.contains("\"value\":\"xoxb-secret-slack-token\""));
        assertTrue(body.contains("\"injection_location\":\"header\""));
        assertTrue(body.contains("\"trusted_domains\":[\"*.slack.com\",\"slack.com\"]"));
    }

    @Test
    void testGetCredential() throws Exception {
        String responseJson = """
            {
              "id": "jira-api-token",
              "type": "bearer_token",
              "status": "active"
            }
            """;
        mockWebServer.enqueue(new MockResponse()
            .setBody(responseJson)
            .addHeader("Content-Type", "application/json"));

        Credential credential = client.getCredential("jira-api-token");

        assertNotNull(credential);
        assertEquals("jira-api-token", credential.id());
        assertEquals(CredentialType.BEARER_TOKEN, credential.type());

        RecordedRequest req = mockWebServer.takeRequest();
        assertEquals("GET", req.getMethod());
        assertEquals("/v1beta/credentials/jira-api-token", req.getPath());
    }

    @Test
    void testListCredentialsWithPagination() throws Exception {
        String responseJson = """
            {
              "credentials": [
                {
                  "id": "jira-api-token",
                  "type": "bearer_token",
                  "status": "active"
                }
              ],
              "next_page_token": "page-2"
            }
            """;
        mockWebServer.enqueue(new MockResponse()
            .setBody(responseJson)
            .addHeader("Content-Type", "application/json"));

        ListCredentialsResponse response = client.listCredentials(10, "page-1");

        assertNotNull(response);
        assertEquals(1, response.credentials().size());
        assertEquals("page-2", response.nextPageToken());

        RecordedRequest req = mockWebServer.takeRequest();
        assertEquals("GET", req.getMethod());
        assertEquals("/v1beta/credentials?page_size=10&page_token=page-1", req.getPath());
    }

    @Test
    void testUpdateRotateCredential() throws Exception {
        String responseJson = """
            {
              "id": "jira-api-token",
              "type": "bearer_token",
              "status": "active"
            }
            """;
        mockWebServer.enqueue(new MockResponse()
            .setBody(responseJson)
            .addHeader("Content-Type", "application/json"));

        Credential updated = client.updateCredential("jira-api-token", CredentialUpdate.ofBearerToken("rotated-secret-token"));

        assertNotNull(updated);
        assertEquals("jira-api-token", updated.id());

        RecordedRequest req = mockWebServer.takeRequest();
        assertEquals("PATCH", req.getMethod());
        assertEquals("/v1beta/credentials/jira-api-token", req.getPath());
        String body = req.getBody().readUtf8();
        assertTrue(body.contains("\"token\":\"rotated-secret-token\""));
    }

    @Test
    void testDeleteCredential() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));

        client.deleteCredential("jira-api-token");

        RecordedRequest req = mockWebServer.takeRequest();
        assertEquals("DELETE", req.getMethod());
        assertEquals("/v1beta/credentials/jira-api-token", req.getPath());
    }

    @Test
    void testMcpServerWithCredentialSerialization() throws Exception {
        Tool.McpServer mcp = Tool.McpServer.builder()
            .name("jira-mcp")
            .url("https://jira.internal.net/sse")
            .credential("jira-api-token")
            .build();

        assertEquals("jira-api-token", mcp.credential());

        String json = objectMapper.writeValueAsString(mcp);
        assertTrue(json.contains("\"credential\":\"jira-api-token\""));
        assertTrue(json.contains("\"url\":\"https://jira.internal.net/sse\""));
    }

    @Test
    void testEnvironmentConfigWithCredentialReference() throws Exception {
        EnvironmentConfig config = EnvironmentConfig.builder()
            .env("DEBUG", "true")
            .envCredential("SLACK_BOT_TOKEN", "slack-token-id")
            .build();

        assertNotNull(config.env());
        assertEquals("true", config.env().get("DEBUG"));
        assertTrue(config.env().get("SLACK_BOT_TOKEN") instanceof CredentialReference);
        assertEquals("slack-token-id", ((CredentialReference) config.env().get("SLACK_BOT_TOKEN")).credential());

        String json = objectMapper.writeValueAsString(config);
        assertTrue(json.contains("\"DEBUG\":\"true\""));
        assertTrue(json.contains("\"SLACK_BOT_TOKEN\":{\"credential\":\"slack-token-id\"}"));
    }
}
