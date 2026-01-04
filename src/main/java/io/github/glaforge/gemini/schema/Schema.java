package io.github.glaforge.gemini.schema;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for all Gemini JSON Schemas.
 */
public abstract class Schema {
    protected String type;
    protected String description;
    protected String title;
    protected boolean nullable = false;

    protected Schema(String type) {
        this.type = type;
    }

    /**
     * Set the description for this schema.
     * @param description The description string.
     * @return The schema instance.
     */
    public Schema desc(String description) {
        this.description = description;
        return this;
    }

    /**
     * Set the title for this schema.
     * @param title The title string.
     * @return The schema instance.
     */
    public Schema title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Mark this schema as nullable.
     * This will change the type in the JSON output to an array ["originalType", "null"].
     * @return The schema instance.
     */
    public Schema nullable() {
        this.nullable = true;
        return this;
    }

    /**
     * Convert this Schema object into a Map suitable for JSON serialization.
     * @return A Map representing the JSON schema.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (nullable) {
            String[] types = {type, "null"};
            map.put("type", types);
        } else {
            map.put("type", type);
        }

        if (description != null) {
            map.put("description", description);
        }
        if (title != null) {
            map.put("title", title);
        }
        return map;
    }
}
