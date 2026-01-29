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

package io.github.glaforge.gemini.schema;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for all Gemini JSON Schemas.
 */
public abstract class Schema {
    /** The JSON schema type. */
    protected String type;
    /** The description of the parameter. */
    protected String description;
    /** The title of the parameter. */
    protected String title;
    /** Whether the parameter is nullable. */
    protected boolean nullable = false;

    /**
     * Initializes a new schema with the given type.
     * @param type The JSON schema type.
     */
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
