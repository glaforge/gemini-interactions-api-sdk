package io.github.glaforge.gemini.interactions.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response containing a list of triggers.
 *
 * @param triggers The list of triggers.
 * @param nextPageToken The token for retrieving the next page of results.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ListTriggersResponse(
    List<Trigger> triggers,
    @JsonProperty("next_page_token") String nextPageToken
) {}
