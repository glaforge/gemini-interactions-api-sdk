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

/**
 * Exception thrown when the Gemini Interactions API returns an error response.
 */
public class GeminiInteractionsException extends RuntimeException {

    private final int statusCode;
    private final String body;

    public GeminiInteractionsException(String message, int statusCode, String body) {
        super(message);
        this.statusCode = statusCode;
        this.body = body;
    }

    /**
     * Returns the HTTP status code returned by the API.
     *
     * @return The HTTP status code.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the response body returned by the API.
     *
     * @return The response body.
     */
    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "GeminiInteractionsException{" +
            "message=" + getMessage() +
            ", statusCode=" + statusCode +
            ", body='" + body + '\'' +
            '}';
    }
}
