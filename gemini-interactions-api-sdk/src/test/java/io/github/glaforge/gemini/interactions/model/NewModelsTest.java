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

package io.github.glaforge.gemini.interactions.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NewModelsTest {

  private final ObjectMapper objectMapper = JsonMapper.builder().build();

  @Test
  void deserializeThoughtContent() throws Exception {
    String json = """
        {
          "type": "thought",
          "signature": "abcdef123",
          "summary": [
            {
              "type": "text",
              "text": "Thinking..."
            }
          ]
        }
        """;

    Content content = objectMapper.readValue(json, Content.class);
    assertTrue(content instanceof Content.ThoughtContent);
    Content.ThoughtContent thought = (Content.ThoughtContent) content;
    assertEquals("thought", thought.type());
    assertEquals("abcdef123", thought.signature());
    assertEquals(1, thought.summary().size());
    assertTrue(thought.summary().get(0) instanceof Content.TextContent);
    assertEquals("Thinking...", ((Content.TextContent) thought.summary().get(0)).text());
  }

  @Test
  void deserializeWebhook() throws Exception {
    String json = """
        {
          "id": "wh-123",
          "name": "My Webhook",
          "uri": "https://example.com/hook",
          "subscribed_events": ["interaction.completed", "error"],
          "state": "enabled"
        }
        """;

    Webhook webhook = objectMapper.readValue(json, Webhook.class);
    assertEquals("wh-123", webhook.id());
    assertEquals("My Webhook", webhook.name());
    assertEquals("https://example.com/hook", webhook.uri());
    assertEquals(Webhook.State.ENABLED, webhook.state());
    assertEquals(2, webhook.subscribedEvents().size());
  }
}
