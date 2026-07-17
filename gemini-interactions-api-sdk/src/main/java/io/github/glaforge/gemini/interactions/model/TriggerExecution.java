package io.github.glaforge.gemini.interactions.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * An execution instance of a trigger.
 *
 * @param id The unique identifier of the execution.
 * @param triggerId The ID of the parent trigger.
 * @param environmentId The environment ID where the execution took place.
 * @param interactionId The resulting interaction ID.
 * @param status The status of the execution.
 * @param error Any error message if the execution failed.
 * @param scheduledTime The time the execution was scheduled to run.
 * @param startTime The time the execution started.
 * @param endTime The time the execution finished.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TriggerExecution(
    String id,
    @JsonProperty("trigger_id") String triggerId,
    @JsonProperty("environment_id") String environmentId,
    @JsonProperty("interaction_id") String interactionId,
    Status status,
    String error,
    @JsonProperty("scheduled_time") Instant scheduledTime,
    @JsonProperty("start_time") Instant startTime,
    @JsonProperty("end_time") Instant endTime
) {
    /**
     * The status of the trigger execution.
     */
    public enum Status {
        /** The execution is currently in progress. */
        @JsonProperty("in_progress") IN_PROGRESS,
        /** The execution completed successfully. */
        @JsonProperty("completed") COMPLETED,
        /** The execution failed. */
        @JsonProperty("failed") FAILED,
        /** The execution was skipped. */
        @JsonProperty("skipped") SKIPPED,
        /** The execution timed out. */
        @JsonProperty("timed_out") TIMED_OUT
    }
}
