package io.github.glaforge.gemini.interactions.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parameters for creating a trigger.
 *
 * @param displayName The human-readable name of the trigger.
 * @param environmentId The ID of the environment where this trigger runs.
 * @param executionTimeoutSeconds The timeout for each execution in seconds.
 * @param interaction The interaction parameters to execute.
 * @param maxConsecutiveFailures The maximum number of consecutive failures before the trigger is paused.
 * @param schedule The cron schedule expression.
 * @param timeZone The time zone for the schedule.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TriggerCreateParams(
    @JsonProperty("display_name") String displayName,
    @JsonProperty("environment_id") String environmentId,
    @JsonProperty("execution_timeout_seconds") Integer executionTimeoutSeconds,
    InteractionParams.Request interaction,
    @JsonProperty("max_consecutive_failures") Integer maxConsecutiveFailures,
    String schedule,
    @JsonProperty("time_zone") String timeZone
) {
    /**
     * Returns a new builder for TriggerCreateParams.
     * @return a new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder for {@link TriggerCreateParams}. */
    public static class Builder {
        /** Creates a new Builder. */
        public Builder() {}

        private String displayName;
        private String environmentId;
        private Integer executionTimeoutSeconds;
        private InteractionParams.Request interaction;
        private Integer maxConsecutiveFailures;
        private String schedule;
        private String timeZone;

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
         * Sets the interaction parameters.
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
         * Builds the TriggerCreateParams.
         * @return The TriggerCreateParams.
         */
        public TriggerCreateParams build() {
            return new TriggerCreateParams(displayName, environmentId, executionTimeoutSeconds, interaction, maxConsecutiveFailures, schedule, timeZone);
        }
    }
}
