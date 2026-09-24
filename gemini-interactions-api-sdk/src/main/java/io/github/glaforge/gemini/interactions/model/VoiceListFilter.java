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

package io.github.glaforge.gemini.interactions.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Filter parameters for querying voices in the Voices library (VoicesService.ListVoices).
 *
 * @param types         Filter by voice type (replicated, prompted, prebuilt).
 * @param languageCodes Filter by BCP-47 language codes (e.g. "en-US", "fr-FR").
 * @param regionCodes   Filter by region codes (e.g. "US", "GB").
 * @param accents       Filter by dialect or accent (e.g. "en-scotland").
 * @param personas      Filter by vocal persona (e.g. "Warm, Friendly").
 * @param contexts      Filter by target context (e.g. "Audiobook", "Conversational").
 * @param genders       Filter by gender presentation ("female", "male", "neutral").
 * @param pitches       Filter by pitch (LOW, MEDIUM, HIGH).
 * @param search        Free-text search query against display name and description.
 * @param pageSize      Maximum number of voices to return per page.
 * @param pageToken     Pagination token from a previous call.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record VoiceListFilter(
    List<VoiceType> types,
    List<String> languageCodes,
    List<String> regionCodes,
    List<String> accents,
    List<String> personas,
    List<String> contexts,
    List<String> genders,
    List<Pitch> pitches,
    String search,
    Integer pageSize,
    String pageToken
) {
    /**
     * Creates a new Builder for VoiceListFilter.
     *
     * @return A new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Appends query parameters to the specified URL builder.
     *
     * @param urlBuilder The URL builder.
     */
    public void appendQueryParams(StringBuilder urlBuilder) {
        List<String> params = new ArrayList<>();

        if (types != null) {
            for (VoiceType t : types) {
                if (t != null) {
                    params.add("type=" + urlEncode(t.name().toLowerCase()));
                }
            }
        }
        if (languageCodes != null) {
            for (String code : languageCodes) {
                if (code != null && !code.isBlank()) {
                    params.add("language_code=" + urlEncode(code));
                }
            }
        }
        if (regionCodes != null) {
            for (String r : regionCodes) {
                if (r != null && !r.isBlank()) {
                    params.add("region_code=" + urlEncode(r));
                }
            }
        }
        if (accents != null) {
            for (String a : accents) {
                if (a != null && !a.isBlank()) {
                    params.add("accent=" + urlEncode(a));
                }
            }
        }
        if (personas != null) {
            for (String p : personas) {
                if (p != null && !p.isBlank()) {
                    params.add("persona=" + urlEncode(p));
                }
            }
        }
        if (contexts != null) {
            for (String c : contexts) {
                if (c != null && !c.isBlank()) {
                    params.add("context=" + urlEncode(c));
                }
            }
        }
        if (genders != null) {
            for (String g : genders) {
                if (g != null && !g.isBlank()) {
                    params.add("gender=" + urlEncode(g));
                }
            }
        }
        if (pitches != null) {
            for (Pitch p : pitches) {
                if (p != null) {
                    params.add("pitch=" + urlEncode(p.name().toLowerCase()));
                }
            }
        }
        if (search != null && !search.isBlank()) {
            params.add("search=" + urlEncode(search));
        }
        if (pageSize != null) {
            params.add("page_size=" + pageSize);
        }
        if (pageToken != null && !pageToken.isBlank()) {
            params.add("page_token=" + urlEncode(pageToken));
        }

        if (!params.isEmpty()) {
            boolean hasQuestionMark = urlBuilder.indexOf("?") != -1;
            for (int i = 0; i < params.size(); i++) {
                if (i == 0 && !hasQuestionMark) {
                    urlBuilder.append("?");
                } else {
                    urlBuilder.append("&");
                }
                urlBuilder.append(params.get(i));
            }
        }
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Builder for {@link VoiceListFilter}.
     */
    public static class Builder {
        private List<VoiceType> types;
        private List<String> languageCodes;
        private List<String> regionCodes;
        private List<String> accents;
        private List<String> personas;
        private List<String> contexts;
        private List<String> genders;
        private List<Pitch> pitches;
        private String search;
        private Integer pageSize;
        private String pageToken;

        /** Creates a new Builder. */
        public Builder() {}

        /**
         * Sets the voice types.
         *
         * @param types The voice types list.
         * @return This builder.
         */
        public Builder types(List<VoiceType> types) {
            this.types = types;
            return this;
        }

        /**
         * Sets the voice types.
         *
         * @param types The voice types.
         * @return This builder.
         */
        public Builder types(VoiceType... types) {
            this.types = types != null ? List.of(types) : null;
            return this;
        }

        /**
         * Sets language codes filter.
         *
         * @param languageCodes The language codes list.
         * @return This builder.
         */
        public Builder languageCodes(List<String> languageCodes) {
            this.languageCodes = languageCodes;
            return this;
        }

        /**
         * Sets language codes filter.
         *
         * @param languageCodes The language codes.
         * @return This builder.
         */
        public Builder languageCodes(String... languageCodes) {
            this.languageCodes = languageCodes != null ? List.of(languageCodes) : null;
            return this;
        }

        /**
         * Sets region codes filter.
         *
         * @param regionCodes The region codes list.
         * @return This builder.
         */
        public Builder regionCodes(List<String> regionCodes) {
            this.regionCodes = regionCodes;
            return this;
        }

        /**
         * Sets region codes filter.
         *
         * @param regionCodes The region codes.
         * @return This builder.
         */
        public Builder regionCodes(String... regionCodes) {
            this.regionCodes = regionCodes != null ? List.of(regionCodes) : null;
            return this;
        }

        /**
         * Sets accents filter.
         *
         * @param accents The accents list.
         * @return This builder.
         */
        public Builder accents(List<String> accents) {
            this.accents = accents;
            return this;
        }

        /**
         * Sets accents filter.
         *
         * @param accents The accents.
         * @return This builder.
         */
        public Builder accents(String... accents) {
            this.accents = accents != null ? List.of(accents) : null;
            return this;
        }

        /**
         * Sets personas filter.
         *
         * @param personas The personas list.
         * @return This builder.
         */
        public Builder personas(List<String> personas) {
            this.personas = personas;
            return this;
        }

        /**
         * Sets personas filter.
         *
         * @param personas The personas.
         * @return This builder.
         */
        public Builder personas(String... personas) {
            this.personas = personas != null ? List.of(personas) : null;
            return this;
        }

        /**
         * Sets contexts filter.
         *
         * @param contexts The contexts list.
         * @return This builder.
         */
        public Builder contexts(List<String> contexts) {
            this.contexts = contexts;
            return this;
        }

        /**
         * Sets contexts filter.
         *
         * @param contexts The contexts.
         * @return This builder.
         */
        public Builder contexts(String... contexts) {
            this.contexts = contexts != null ? List.of(contexts) : null;
            return this;
        }

        /**
         * Sets genders filter.
         *
         * @param genders The genders list.
         * @return This builder.
         */
        public Builder genders(List<String> genders) {
            this.genders = genders;
            return this;
        }

        /**
         * Sets genders filter.
         *
         * @param genders The genders.
         * @return This builder.
         */
        public Builder genders(String... genders) {
            this.genders = genders != null ? List.of(genders) : null;
            return this;
        }

        /**
         * Sets pitches filter.
         *
         * @param pitches The pitches list.
         * @return This builder.
         */
        public Builder pitches(List<Pitch> pitches) {
            this.pitches = pitches;
            return this;
        }

        /**
         * Sets pitches filter.
         *
         * @param pitches The pitches.
         * @return This builder.
         */
        public Builder pitches(Pitch... pitches) {
            this.pitches = pitches != null ? List.of(pitches) : null;
            return this;
        }

        /**
         * Sets search query filter.
         *
         * @param search The free-text search query.
         * @return This builder.
         */
        public Builder search(String search) {
            this.search = search;
            return this;
        }

        /**
         * Sets page size.
         *
         * @param pageSize The page size.
         * @return This builder.
         */
        public Builder pageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        /**
         * Sets page token.
         *
         * @param pageToken The page token.
         * @return This builder.
         */
        public Builder pageToken(String pageToken) {
            this.pageToken = pageToken;
            return this;
        }

        /**
         * Builds the {@link VoiceListFilter} instance.
         *
         * @return A new VoiceListFilter.
         */
        public VoiceListFilter build() {
            return new VoiceListFilter(
                types != null ? List.copyOf(types) : null,
                languageCodes != null ? List.copyOf(languageCodes) : null,
                regionCodes != null ? List.copyOf(regionCodes) : null,
                accents != null ? List.copyOf(accents) : null,
                personas != null ? List.copyOf(personas) : null,
                contexts != null ? List.copyOf(contexts) : null,
                genders != null ? List.copyOf(genders) : null,
                pitches != null ? List.copyOf(pitches) : null,
                search,
                pageSize,
                pageToken
            );
        }
    }
}
