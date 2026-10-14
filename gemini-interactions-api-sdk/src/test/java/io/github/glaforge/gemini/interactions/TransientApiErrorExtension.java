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

import io.github.glaforge.gemini.interactions.model.Events;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.opentest4j.TestAbortedException;

/**
 * JUnit 5 extension that aborts (skips) integration tests ({@code *IT}) instead of failing them
 * when transient server errors or API rate limits occur:
 * <ul>
 *   <li>HTTP 429 ({@code too_many_requests} / rate limit exceeded)</li>
 *   <li>HTTP 503 ({@code service_unavailable} / temporary high demand)</li>
 *   <li>HTTP 504 ({@code deadline_exceeded} / gateway timeout)</li>
 *   <li>HTTP 400 with {@code content_blocked} (transient false-positive policy filter on media generation)</li>
 * </ul>
 */
public class TransientApiErrorExtension implements TestExecutionExceptionHandler {

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        boolean isIntegrationTest = context.getTestClass()
                .map(c -> c.getSimpleName().endsWith("IT"))
                .orElse(false);
        if (isIntegrationTest) {
            GeminiInteractionsException apiException = findApiException(throwable);
            if (apiException != null && isRateLimitOrTransient(apiException)) {
                String msg = String.format(
                        "Skipping test %s due to rate limit or transient API error (HTTP %d): %s",
                        context.getDisplayName(),
                        apiException.getStatusCode(),
                        apiException.getBody() != null ? apiException.getBody() : apiException.getMessage());
                System.out.println(msg);
                throw new TestAbortedException(msg, apiException);
            }
        }
        throw throwable;
    }

    /**
     * Aborts (skips) the current test if an SSE {@link Events.ErrorEvent} indicates a rate limit
     * or transient server error.
     *
     * @param errorEvent The SSE error event.
     */
    public static void abortIfRateLimitOrTransient(Events.ErrorEvent errorEvent) {
        if (errorEvent == null || errorEvent.error() == null) {
            return;
        }
        String code = errorEvent.error().code() != null ? errorEvent.error().code().toLowerCase() : "";
        String message = errorEvent.error().message() != null ? errorEvent.error().message().toLowerCase() : "";
        if (code.contains("too_many_requests")
                || code.contains("rate_limit")
                || code.contains("resource_exhausted")
                || code.contains("service_unavailable")
                || code.contains("deadline_exceeded")
                || message.contains("rate limit")
                || message.contains("too many requests")
                || message.contains("high demand")) {
            String msg = "Skipping streaming test due to rate limit or transient SSE error: "
                    + errorEvent.error().code() + " - " + errorEvent.error().message();
            System.out.println(msg);
            throw new TestAbortedException(msg);
        }
    }

    /**
     * Checks whether the given {@link GeminiInteractionsException} represents a rate-limit (429)
     * or transient server/policy error (503, 504, or 400 {@code content_blocked}).
     *
     * @param e The exception to inspect.
     * @return {@code true} if the test should be skipped rather than failed.
     */
    public static boolean isRateLimitOrTransient(GeminiInteractionsException e) {
        int status = e.getStatusCode();
        String body = e.getBody() != null ? e.getBody() : "";
        String message = e.getMessage() != null ? e.getMessage() : "";

        if (status == 429 || status == 503 || status == 504) {
            return true;
        }
        if (status == 400 && body.contains("content_blocked")) {
            return true;
        }
        return body.contains("too_many_requests")
                || body.contains("Rate limit exceeded")
                || body.contains("service_unavailable")
                || body.contains("deadline_exceeded")
                || message.contains("too_many_requests")
                || message.contains("Rate limit exceeded");
    }

    private static GeminiInteractionsException findApiException(Throwable t) {
        Throwable curr = t;
        while (curr != null) {
            if (curr instanceof GeminiInteractionsException gie) {
                return gie;
            }
            curr = curr.getCause();
        }
        return null;
    }
}
