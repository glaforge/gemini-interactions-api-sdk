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

package io.github.glaforge.gemini.interactions;

import io.github.glaforge.gemini.interactions.model.Content;
import io.github.glaforge.gemini.interactions.model.Content.TextContent;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams;
import io.github.glaforge.gemini.interactions.model.Tool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class McpServerTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
    void testMcpServerWeather() throws IOException, InterruptedException {
        GeminiInteractionsClient client = GeminiInteractionsClient.builder()
            .apiKey(System.getenv("GEMINI_API_KEY"))
            .build();

        // 1. Define the MCP Server tool
        String serverName = "moon-server";
        // String serverUrl = "https://mn-mcp-server-1029513523185.europe-west1.run.app/mcp";
        // String serverUrl = "https://moonphases-1029513523185.europe-west1.run.app/mcp";
        String serverUrl = "https://context7.liam.sh/mcp";

        Tool mcpServer = new Tool.McpServer(serverName, serverUrl);
        List<Tool> tools = List.of(mcpServer);

        // 2. Create Interaction
        InteractionParams.ModelInteractionParams createParams = InteractionParams.ModelInteractionParams.builder()
            .model("gemini-2.5-flash")
            .input("What is the phase of the moon?")
            .tools(tools)
            .build();

        System.out.println("Sending MCP server request...");
        Interaction interaction = client.create(createParams);
        System.out.println("Response status: " + interaction.status());
        assertNotNull(interaction.outputs(), "Interaction outputs should not be null");

        // 3. Verify Response
        System.out.println("Outputs count: " + interaction.outputs().size());
        for (Content content : interaction.outputs()) {
            System.out.println("Output Item Type: " + content.getClass().getSimpleName());
            if (content instanceof TextContent text) {
                System.out.println("  Text: " + text.text());
                // Check for moon related keywords
                if (text.text().length() > 0) {
                     assertTrue(text.text().toLowerCase().contains("moon")
                         || text.text().toLowerCase().contains("waxing")
                         || text.text().toLowerCase().contains("waning")
                         || text.text().toLowerCase().contains("new")
                         || text.text().toLowerCase().contains("full"),
                         "Response should contain moon phase info");
                }
            } else {
                System.out.println("  Content: " + content);
            }
        }
    }
}
