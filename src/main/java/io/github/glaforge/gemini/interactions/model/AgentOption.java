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

/**
 * Constants for the agent to interact with.
 */
public final class AgentOption {
    private AgentOption() {}

    /** Gemini Deep Research Agent (Preview 12-2025) */
    public static final String DEEP_RESEARCH_PRO_PREVIEW_12_2025 = "deep-research-pro-preview-12-2025";
    /** Gemini Deep Research Agent (Preview 04-2026) */
    public static final String DEEP_RESEARCH_PREVIEW_04_2026 = "deep-research-preview-04-2026";
    /** Gemini Deep Research Max Agent (Preview 04-2026) */
    public static final String DEEP_RESEARCH_MAX_PREVIEW_04_2026 = "deep-research-max-preview-04-2026";
}
