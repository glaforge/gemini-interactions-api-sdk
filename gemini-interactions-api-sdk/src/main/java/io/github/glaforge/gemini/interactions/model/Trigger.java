package io.github.glaforge.gemini.interactions.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * A trigger that automatically creates interactions on a schedule.
 *
 * @param id The unique identifier of the trigger.
 * @param displayName The human-readable name of the trigger.
 * @param environmentId The ID of the environment where this trigger runs.
 * @param executionTimeoutSeconds The timeout for each execution in seconds.
 * @param interaction The interaction parameters or template to execute.
 * @param maxConsecutiveFailures The maximum number of consecutive failures before the trigger is paused.
 * @param schedule The cron schedule expression.
 * @param timeZone The time zone for the schedule.
 * @param status The current status of the trigger.
 * @param nextRunTime The calculated next execution time.
 * @param previousInteractionId The ID of the previous interaction (if continuing a thread).
 * @param createTime The creation time of the trigger.
 * @param updateTime The last update time of the trigger.
 * @param consecutiveFailureCount The number of consecutive failures since the last successful execution.
 * @param lastPauseTime The time when the trigger was last paused.
 * @param lastResumeTime The time when the trigger was last resumed.
 * @param lastRunTime The time when the trigger was last run.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Trigger(
    String id,
    @JsonProperty("display_name") String displayName,
    @JsonProperty("environment_id") String environmentId,
    @JsonProperty("execution_timeout_seconds") Integer executionTimeoutSeconds,
    Object interaction,
    @JsonProperty("max_consecutive_failures") Integer maxConsecutiveFailures,
    String schedule,
    @JsonProperty("time_zone") String timeZone,
    Status status,
    @JsonProperty("next_run_time") Instant nextRunTime,
    @JsonProperty("previous_interaction_id") String previousInteractionId,
    @JsonProperty("create_time") Instant createTime,
    @JsonProperty("update_time") Instant updateTime,
    @JsonProperty("consecutive_failure_count") Integer consecutiveFailureCount,
    @JsonProperty("last_pause_time") Instant lastPauseTime,
    @JsonProperty("last_resume_time") Instant lastResumeTime,
    @JsonProperty("last_run_time") Instant lastRunTime
) {
    /**
     * Backward-compatible constructor with original 13 parameters.
     *
     * @param id The id.
     * @param displayName The display name.
     * @param environmentId The environment id.
     * @param executionTimeoutSeconds The execution timeout in seconds.
     * @param interaction The interaction request parameters.
     * @param maxConsecutiveFailures The max consecutive failures.
     * @param schedule The cron schedule.
     * @param timeZone The time zone.
     * @param status The status.
     * @param nextRunTime The next run time.
     * @param previousInteractionId The previous interaction id.
     * @param createTime The create time.
     * @param updateTime The update time.
     */
    public Trigger(
        String id,
        String displayName,
        String environmentId,
        Integer executionTimeoutSeconds,
        InteractionParams.Request interaction,
        Integer maxConsecutiveFailures,
        String schedule,
        String timeZone,
        Status status,
        Instant nextRunTime,
        String previousInteractionId,
        Instant createTime,
        Instant updateTime
    ) {
        this(id, displayName, environmentId, executionTimeoutSeconds, (Object) interaction, maxConsecutiveFailures,
            schedule, timeZone, status, nextRunTime, previousInteractionId, createTime, updateTime, null, null, null, null);
    }

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    /**
     * Returns the interaction as an {@link InteractionParams.Request}, if applicable.
     *
     * @return The interaction request, or null if not of this type.
     */
    public InteractionParams.Request interactionAsRequest() {
        if (interaction instanceof InteractionParams.Request req) {
            return req;
        }
        if (interaction instanceof Map<?, ?> map) {
            try {
                return MAPPER.convertValue(map, InteractionParams.Request.class);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    /**
     * Returns the interaction as an {@link Interaction}, if applicable.
     *
     * @return The interaction resource, or null if not of this type.
     */
    public Interaction interactionAsInteraction() {
        if (interaction instanceof Interaction i) {
            return i;
        }
        if (interaction instanceof Map<?, ?> map) {
            try {
                return MAPPER.convertValue(map, Interaction.class);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

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
        private Object interaction;
        private Integer maxConsecutiveFailures;
        private String schedule;
        private String timeZone;
        private Status status;
        private Instant nextRunTime;
        private String previousInteractionId;
        private Instant createTime;
        private Instant updateTime;
        private Integer consecutiveFailureCount;
        private Instant lastPauseTime;
        private Instant lastResumeTime;
        private Instant lastRunTime;

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
         * Sets the interaction parameters or template.
         * @param interaction The interaction parameters or resource.
         * @return This builder.
         */
        public Builder interaction(Object interaction) { this.interaction = interaction; return this; }
        /**
         * Sets the interaction request parameters.
         * @param interaction The interaction parameters.
         * @return This builder.
         */
        public Builder interaction(InteractionParams.Request interaction) { this.interaction = interaction; return this; }
        /**
         * Sets the interaction resource.
         * @param interaction The interaction resource.
         * @return This builder.
         */
        public Builder interaction(Interaction interaction) { this.interaction = interaction; return this; }
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
         * Sets the consecutive failure count.
         * @param consecutiveFailureCount The consecutive failure count.
         * @return This builder.
         */
        public Builder consecutiveFailureCount(Integer consecutiveFailureCount) { this.consecutiveFailureCount = consecutiveFailureCount; return this; }
        /**
         * Sets the last pause time.
         * @param lastPauseTime The last pause time.
         * @return This builder.
         */
        public Builder lastPauseTime(Instant lastPauseTime) { this.lastPauseTime = lastPauseTime; return this; }
        /**
         * Sets the last resume time.
         * @param lastResumeTime The last resume time.
         * @return This builder.
         */
        public Builder lastResumeTime(Instant lastResumeTime) { this.lastResumeTime = lastResumeTime; return this; }
        /**
         * Sets the last run time.
         * @param lastRunTime The last run time.
         * @return This builder.
         */
        public Builder lastRunTime(Instant lastRunTime) { this.lastRunTime = lastRunTime; return this; }

        /**
         * Builds the Trigger.
         * @return The Trigger.
         */
        public Trigger build() {
            return new Trigger(id, displayName, environmentId, executionTimeoutSeconds, interaction,
                maxConsecutiveFailures, schedule, timeZone, status, nextRunTime, previousInteractionId,
                createTime, updateTime, consecutiveFailureCount, lastPauseTime, lastResumeTime, lastRunTime);
        }
    }
}
