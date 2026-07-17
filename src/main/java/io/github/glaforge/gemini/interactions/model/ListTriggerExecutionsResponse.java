package io.github.glaforge.gemini.interactions.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response containing a list of trigger executions.
 *
 * @param triggerExecutions The list of trigger executions.
 * @param nextPageToken The token for retrieving the next page of results.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ListTriggerExecutionsResponse(
    @JsonProperty("trigger_executions") List<TriggerExecution> triggerExecutions,
    @JsonProperty("next_page_token") String nextPageToken
) {}
