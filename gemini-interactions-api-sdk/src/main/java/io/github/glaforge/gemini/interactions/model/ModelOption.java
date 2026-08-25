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
 * Constants for the model that will complete your prompt.
 */
public final class ModelOption {
    private ModelOption() {}

    /** Model option constant. */
    public static final String ANTIGRAVITY_PREVIEW_05_2026 = "antigravity-preview-05-2026";
    /** Model option constant. */
    public static final String AQA = "aqa";
    /** Model option constant. */
    public static final String DEEP_RESEARCH_MAX_PREVIEW_04_2026 = "deep-research-max-preview-04-2026";
    /** Model option constant. */
    public static final String DEEP_RESEARCH_PREVIEW_04_2026 = "deep-research-preview-04-2026";
    /** Model option constant. */
    public static final String DEEP_RESEARCH_PRO_PREVIEW_12_2025 = "deep-research-pro-preview-12-2025";
    /** Model option constant. */
    public static final String GEMINI_2_0_FLASH = "gemini-2.0-flash";
    /** Model option constant. */
    public static final String GEMINI_2_0_FLASH_001 = "gemini-2.0-flash-001";
    /** Model option constant. */
    public static final String GEMINI_2_0_FLASH_LITE = "gemini-2.0-flash-lite";
    /** Model option constant. */
    public static final String GEMINI_2_0_FLASH_LITE_001 = "gemini-2.0-flash-lite-001";
    /** Model option constant. */
    public static final String GEMINI_2_5_COMPUTER_USE_PREVIEW_10_2025 = "gemini-2.5-computer-use-preview-10-2025";
    /** Model option constant. */
    public static final String GEMINI_2_5_FLASH = "gemini-2.5-flash";
    /** Model option constant. */
    public static final String GEMINI_2_5_FLASH_IMAGE = "gemini-2.5-flash-image";
    /** Model option constant. */
    public static final String GEMINI_2_5_FLASH_LITE = "gemini-2.5-flash-lite";
    /** Model option constant. */
    public static final String GEMINI_2_5_FLASH_LITE_PREVIEW_09_2025 = "gemini-2.5-flash-lite-preview-09-2025";
    /** Model option constant. */
    public static final String GEMINI_2_5_FLASH_NATIVE_AUDIO_LATEST = "gemini-2.5-flash-native-audio-latest";
    /** Model option constant. */
    public static final String GEMINI_2_5_FLASH_NATIVE_AUDIO_PREVIEW_12_2025 = "gemini-2.5-flash-native-audio-preview-12-2025";
    /** Model option constant. */
    public static final String GEMINI_2_5_FLASH_PREVIEW_09_2025 = "gemini-2.5-flash-preview-09-2025";
    /** Model option constant. */
    public static final String GEMINI_2_5_FLASH_PREVIEW_TTS = "gemini-2.5-flash-preview-tts";
    /** Model option constant. */
    public static final String GEMINI_2_5_PRO = "gemini-2.5-pro";
    /** Model option constant. */
    public static final String GEMINI_2_5_PRO_PREVIEW_TTS = "gemini-2.5-pro-preview-tts";
    /** Model option constant. */
    public static final String GEMINI_3_FLASH_PREVIEW = "gemini-3-flash-preview";
    /** Model option constant. */
    public static final String GEMINI_3_PRO_IMAGE = "gemini-3-pro-image";
    /** Model option constant. */
    public static final String GEMINI_3_PRO_IMAGE_PREVIEW = "gemini-3-pro-image-preview";
    /** Model option constant. */
    public static final String GEMINI_3_PRO_PREVIEW = "gemini-3-pro-preview";
    /** Model option constant. */
    public static final String GEMINI_3_1_FLASH_IMAGE = "gemini-3.1-flash-image";
    /** Model option constant. */
    public static final String GEMINI_3_1_FLASH_IMAGE_PREVIEW = "gemini-3.1-flash-image-preview";
    /** Model option constant. */
    public static final String GEMINI_3_1_FLASH_LITE = "gemini-3.1-flash-lite";
    /** Model option constant. */
    public static final String GEMINI_3_1_FLASH_LITE_PREVIEW = "gemini-3.1-flash-lite-preview";
    /** Model option constant. */
    public static final String GEMINI_3_1_FLASH_LIVE_PREVIEW = "gemini-3.1-flash-live-preview";
    /** Model option constant. */
    public static final String GEMINI_3_1_FLASH_TTS_PREVIEW = "gemini-3.1-flash-tts-preview";
    /** Model option constant. */
    public static final String GEMINI_3_1_PRO_PREVIEW = "gemini-3.1-pro-preview";
    /** Model option constant. */
    public static final String GEMINI_3_1_PRO_PREVIEW_CUSTOMTOOLS = "gemini-3.1-pro-preview-customtools";
    /** Model option constant. */
    public static final String GEMINI_3_5_FLASH = "gemini-3.5-flash";
    /** Model option constant. */
    public static final String GEMINI_3_6_FLASH = "gemini-3.6-flash";
    /** Model option constant. */
    public static final String GEMINI_3_7_FLASH = "gemini-3.7-flash";
    /** Model option constant. */
    public static final String GEMINI_3_5_LIVE_TRANSLATE_PREVIEW = "gemini-3.5-live-translate-preview";
    /** Model option constant. */
    public static final String GEMINI_EMBEDDING_001 = "gemini-embedding-001";
    /** Model option constant. */
    public static final String GEMINI_EMBEDDING_2 = "gemini-embedding-2";
    /** Model option constant. */
    public static final String GEMINI_EMBEDDING_2_PREVIEW = "gemini-embedding-2-preview";
    /** Model option constant. */
    public static final String GEMINI_FLASH_LATEST = "gemini-flash-latest";
    /** Model option constant. */
    public static final String GEMINI_FLASH_LITE_LATEST = "gemini-flash-lite-latest";
    /** Model option constant. */
    public static final String GEMINI_PRO_LATEST = "gemini-pro-latest";
    /** Model option constant. */
    public static final String GEMINI_ROBOTICS_ER_1_5_PREVIEW = "gemini-robotics-er-1.5-preview";
    /** Model option constant. */
    public static final String GEMINI_ROBOTICS_ER_1_6_PREVIEW = "gemini-robotics-er-1.6-preview";
    /** Model option constant for Gemini Robotics Embodied Reasoning 2 Preview. */
    public static final String GEMINI_ROBOTICS_ER_2_PREVIEW = "gemini-robotics-er-2-preview";
    /** Model option constant. */
    public static final String GEMMA_4_26B_A4B_IT = "gemma-4-26b-a4b-it";
    /** Model option constant. */
    public static final String GEMMA_4_31B_IT = "gemma-4-31b-it";
    /** Model option constant. */
    public static final String IMAGEN_4_0_FAST_GENERATE_001 = "imagen-4.0-fast-generate-001";
    /** Model option constant. */
    public static final String IMAGEN_4_0_GENERATE_001 = "imagen-4.0-generate-001";
    /** Model option constant. */
    public static final String IMAGEN_4_0_ULTRA_GENERATE_001 = "imagen-4.0-ultra-generate-001";
    /** Model option constant. */
    public static final String LYRIA_3_CLIP_PREVIEW = "lyria-3-clip-preview";
    /** Model option constant. */
    public static final String LYRIA_3_PRO_PREVIEW = "lyria-3-pro-preview";
    /** Model option constant. */
    public static final String NANO_BANANA_PRO_PREVIEW = "nano-banana-pro-preview";
    /** Model option constant. */
    public static final String VEO_2_0_GENERATE_001 = "veo-2.0-generate-001";
    /** Model option constant. */
    public static final String VEO_3_0_FAST_GENERATE_001 = "veo-3.0-fast-generate-001";
    /** Model option constant. */
    public static final String VEO_3_0_GENERATE_001 = "veo-3.0-generate-001";
    /** Model option constant. */
    public static final String VEO_3_1_FAST_GENERATE_PREVIEW = "veo-3.1-fast-generate-preview";
    /** Model option constant. */
    public static final String VEO_3_1_GENERATE_PREVIEW = "veo-3.1-generate-preview";
    /** Model option constant. */
    public static final String VEO_3_1_LITE_GENERATE_PREVIEW = "veo-3.1-lite-generate-preview";
}
