package io.github.glaforge.gemini.interactions.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the fields of a Trigger that can be updated.
 *
 * @param displayName The new display name for the trigger.
 * @param status The new status for the trigger.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TriggerUpdate(
    @JsonProperty("display_name") String displayName,
    Trigger.Status status
) {
    /**
     * Returns a new builder for TriggerUpdate.
     * @return a new builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder for {@link TriggerUpdate}. */
    public static class Builder {
        /** Creates a new Builder. */
        public Builder() {}

        private String displayName;
        private Trigger.Status status;

        /**
         * Sets the display name.
         * @param displayName The display name.
         * @return This builder.
         */
        public Builder displayName(String displayName) { this.displayName = displayName; return this; }
        /**
         * Sets the status.
         * @param status The status.
         * @return This builder.
         */
        public Builder status(Trigger.Status status) { this.status = status; return this; }

        /**
         * Builds the TriggerUpdate.
         * @return The TriggerUpdate.
         */
        public TriggerUpdate build() {
            return new TriggerUpdate(displayName, status);
        }
    }
}
