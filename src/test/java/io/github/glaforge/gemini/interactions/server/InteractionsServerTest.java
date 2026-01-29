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

package io.github.glaforge.gemini.interactions.server;

import com.sun.net.httpserver.HttpServer;
import io.github.glaforge.gemini.interactions.GeminiInteractionsClient;
import io.github.glaforge.gemini.interactions.model.Interaction;
import io.github.glaforge.gemini.interactions.model.InteractionParams;
import io.github.glaforge.gemini.interactions.model.Events;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.time.Instant;
import java.util.stream.Stream;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InteractionsServerTest {

    private HttpServer server;
    private int port;
    private GeminiInteractionsClient client;
    private ExecutorService executor;

    @BeforeEach
    void setUp() throws IOException {
        executor = Executors.newSingleThreadExecutor();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.setExecutor(executor);
        port = server.getAddress().getPort();

        // v1beta handling
        server.createContext("/v1beta", new InteractionsHandler() {
            @Override
            public Interaction create(InteractionParams.Request request) {
                return new Interaction(
                    "interaction-123",
                    "gemini-pro",
                    null,
                    "interaction",
                    Instant.parse("2025-01-01T00:00:00Z"),
                    Instant.parse("2025-01-01T00:00:00Z"),
                    Interaction.Role.MODEL,
                    Interaction.Status.COMPLETED,
                    Collections.emptyList(),
                    null,
                    null
                );
            }

            @Override
            public Interaction get(String id) {
                 return new Interaction(
                    id,
                    "gemini-pro",
                    null,
                    "interaction",
                    Instant.parse("2025-01-01T00:00:00Z"),
                    Instant.parse("2025-01-01T00:00:00Z"),
                    Interaction.Role.MODEL,
                    Interaction.Status.COMPLETED,
                    Collections.emptyList(),
                    null,
                    null
                );
            }

            @Override
            public void delete(String id) {
                // no-op
            }

            @Override
            public Interaction cancel(String id) {
                 return new Interaction(
                    id,
                    "gemini-pro",
                    null,
                    "interaction",
                    Instant.parse("2025-01-01T00:00:00Z"),
                    Instant.parse("2025-01-01T00:00:00Z"),
                    Interaction.Role.MODEL,
                    Interaction.Status.CANCELLED,
                    Collections.emptyList(),
                    null,
                    null
                );
            }


            @Override
            public Stream<Events> stream(InteractionParams.Request request) {
                return Stream.of(
                    new Events.InteractionEvent(
                        Events.EventType.INTERACTION_START,
                        "evt-1",
                        new Interaction("interaction-123", "gemini-pro", null, "interaction", Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-01-01T00:00:00Z"), Interaction.Role.MODEL, Interaction.Status.IN_PROGRESS, Collections.emptyList(), null, null)
                    ),
                    new Events.ContentDelta(
                        Events.EventType.CONTENT_DELTA,
                        "evt-2",
                        0,
                        new Events.TextDelta(Events.DeltaType.TEXT, "Hello world", null)
                    )
                );
            }
        });
        server.start();

        client = GeminiInteractionsClient.builder()
            .baseUrl("http://localhost:" + port)
            .apiKey("test-key")
            .build();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
        executor.shutdownNow();
    }

    @Test
    void testCreateInteraction() throws IOException, InterruptedException {
        InteractionParams.Request request = InteractionParams.ModelInteractionParams.builder()
            .model("gemini-pro")
            .input("Hello")
            .build();

        Interaction interaction = client.create(request);
        assertNotNull(interaction);
        assertEquals("interaction-123", interaction.id());
    }

    @Test
    void testGetInteraction() throws IOException, InterruptedException {
        Interaction interaction = client.get("foo-bar");
        assertEquals("foo-bar", interaction.id());
        assertEquals(Interaction.Status.COMPLETED, interaction.status());
    }

    @Test
    void testCancelInteraction() throws IOException, InterruptedException {
        Interaction interaction = client.cancel("foo-bar");
        assertEquals("foo-bar", interaction.id());
        assertEquals(Interaction.Status.CANCELLED, interaction.status());
    }

    @Test
    void testStreamInteraction() throws IOException, InterruptedException {
        InteractionParams.Request request = InteractionParams.ModelInteractionParams.builder()
            .model("gemini-pro")
            .input("Hello")
            .build();

        Stream<Events> eventsStream = client.stream(request);
        assertNotNull(eventsStream);
        long count = eventsStream.count();
        assertEquals(2, count);
    }
}
