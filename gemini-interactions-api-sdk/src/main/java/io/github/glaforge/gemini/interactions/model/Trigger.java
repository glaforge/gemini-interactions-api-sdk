package io.github.glaforge.gemini.interactions.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * A trigger that automatically creates interactions on a schedule.
 *
 * @param id The unique identifier of the trigger.
 * @param displayName The human-readable name of the trigger.
 * @param environmentId The ID of the environment where this trigger runs.
 * @param executionTimeoutSeconds The timeout for each execution in seconds.
 * @param interaction The interaction parameters to execute.
 * @param maxConsecutiveFailures The maximum number of consecutive failures before the trigger is paused.
 * @param schedule The cron schedule expression.
 * @param timeZone The time zone for the schedule.
 * @param status The current status of the trigger.
 * @param nextRunTime The calculated next execution time.
 * @param previousInteractionId The ID of the previous interaction (if continuing a thread).
 * @param createTime The creation time of the trigger.
 * @param updateTime The last update time of the trigger.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Trigger(
    String id,
    @JsonProperty("display_name") String displayName,
    @JsonProperty("environment_id") String environmentId,
    @JsonProperty("execution_timeout_seconds") Integer executionTimeoutSeconds,
    InteractionParams.Request interaction,
    @JsonProperty("max_consecutive_failures") Integer maxConsecutiveFailures,
    String schedule,
    @JsonProperty("time_zone") String timeZone,
    Status status,
    @JsonProperty("next_run_time") Instant nextRunTime,
    @JsonProperty("previous_interaction_id") String previousInteractionId,
    @JsonProperty("create_time") Instant createTime,
    @JsonProperty("update_time") Instant updateTime
) {
    /**
     * The status of the trigger.
     */
    public enum Status {
        /** The trigger is active and will fire on schedule. */
        @JsonProperty("active") ACTIVE,
        /** The trigger is paused and will not fire. */
        @JsonProperty("paused") PAUSED,
        /** The trigger is in an error state. */
        @JsonProperty("error") ERROR
    }

    /**
     * Returns a new builder for a Trigger.
     * @return a new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder for {@link Trigger}. */
    public static class Builder {
        /** Creates a new Builder. */
        public Builder() {}

        private String id;
        private String displayName;
        private String environmentId;
        private Integer executionTimeoutSeconds;
        private InteractionParams.Request interaction;
        private Integer maxConsecutiveFailures;
        private String schedule;
        private String timeZone;
        private Status status;
        private Instant nextRunTime;
        private String previousInteractionId;
        private Instant createTime;
        private Instant updateTime;

        /**
         * Sets the id.
         * @param id The id.
         * @return This builder.
         */
        public Builder id(String id) { this.id = id; return this; }
        /**
         * Sets the display name.
         * @param displayName The display name.
         * @return This builder.
         */
        public Builder displayName(String displayName) { this.displayName = displayName; return this; }
        /**
         * Sets the environment id.
         * @param environmentId The environment id.
         * @return This builder.
         */
        public Builder environmentId(String environmentId) { this.environmentId = environmentId; return this; }
        /**
         * Sets the execution timeout in seconds.
         * @param executionTimeoutSeconds The execution timeout.
         * @return This builder.
         */
        public Builder executionTimeoutSeconds(Integer executionTimeoutSeconds) { this.executionTimeoutSeconds = executionTimeoutSeconds; return this; }
        /**
         * Sets the interaction request parameters.
         * @param interaction The interaction parameters.
         * @return This builder.
         */
        public Builder interaction(InteractionParams.Request interaction) { this.interaction = interaction; return this; }
        /**
         * Sets the max consecutive failures.
         * @param maxConsecutiveFailures The max consecutive failures.
         * @return This builder.
         */
        public Builder maxConsecutiveFailures(Integer maxConsecutiveFailures) { this.maxConsecutiveFailures = maxConsecutiveFailures; return this; }
        /**
         * Sets the schedule.
         * @param schedule The cron schedule.
         * @return This builder.
         */
        public Builder schedule(String schedule) { this.schedule = schedule; return this; }
        /**
         * Sets the time zone.
         * @param timeZone The time zone.
         * @return This builder.
         */
        public Builder timeZone(String timeZone) { this.timeZone = timeZone; return this; }
        /**
         * Sets the status.
         * @param status The status.
         * @return This builder.
         */
        public Builder status(Status status) { this.status = status; return this; }
        /**
         * Sets the next run time.
         * @param nextRunTime The next run time.
         * @return This builder.
         */
        public Builder nextRunTime(Instant nextRunTime) { this.nextRunTime = nextRunTime; return this; }
        /**
         * Sets the previous interaction id.
         * @param previousInteractionId The previous interaction id.
         * @return This builder.
         */
        public Builder previousInteractionId(String previousInteractionId) { this.previousInteractionId = previousInteractionId; return this; }
        /**
         * Sets the create time.
         * @param createTime The create time.
         * @return This builder.
         */
        public Builder createTime(Instant createTime) { this.createTime = createTime; return this; }
        /**
         * Sets the update time.
         * @param updateTime The update time.
         * @return This builder.
         */
        public Builder updateTime(Instant updateTime) { this.updateTime = updateTime; return this; }

        /**
         * Builds the Trigger.
         * @return The Trigger.
         */
        public Trigger build() {
            return new Trigger(id, displayName, environmentId, executionTimeoutSeconds, interaction, maxConsecutiveFailures, schedule, timeZone, status, nextRunTime, previousInteractionId, createTime, updateTime);
        }
    }
}
