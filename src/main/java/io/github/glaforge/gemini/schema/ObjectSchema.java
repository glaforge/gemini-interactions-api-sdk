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

import java.util.*;

/**
 * Schema for Object types.
 * <p>
 * Example usage:
 * <pre>{@code
 * obj()
 *     .str("name")
 *     .integer("age");
 * }</pre>
 * <p>
 * <b>Note on convenience methods:</b>
 * Methods like {@code str(name)}, {@code obj(name)}, etc., add a property to the <i>current</i> object
 * and return the <i>current</i> object schema to allow chaining siblings.
 * <p>
 * For example:
 * <pre>{@code
 * obj()
 *     .obj("first")   // Adds "first" property
 *     .obj("second"); // Adds "second" property (sibling of "first")
 * }</pre>
 * Creates: {@code { "first": {}, "second": {} }}
 * <p>
 * To create nested objects, use {@code prop(name, schema)}:
 * <pre>{@code
 * obj()
 *     .prop("parent", obj()
 *         .str("child"));
 * }</pre>
 */
public class ObjectSchema extends Schema {
    private final Map<String, Schema> properties = new LinkedHashMap<>();
    private final List<String> required = new ArrayList<>();
    private Object additionalProperties;

    /** Initializes a new object schema. */
    public ObjectSchema() {
        super("object");
    }

    /**
     * Add a property to this object schema.
     * @param name The name of the property.
     * @param schema The schema for the property.
     * @return The ObjectSchema instance.
     */

    public ObjectSchema prop(String name, Schema schema) {
        properties.put(name, schema);
        return this;
    }

    /**
     * Specify required properties.
     * @param names The names of the required properties.
     * @return The ObjectSchema instance.
     */
    public ObjectSchema req(String... names) {
        required.addAll(Arrays.asList(names));
        return this;
    }

    /**
     * Helper to add a required property in one go.
     * @param name The name of the property.
     * @param schema The schema for the property.
     * @return The ObjectSchema instance.
     */
    public ObjectSchema reqProp(String name, Schema schema) {
        prop(name, schema);
        required.add(name);
        return this;
    }

    /**
     * Add a boolean property.
     * @param name The name of the property.
     * @return The ObjectSchema instance.
     */
    public ObjectSchema bool(String name) {
        return prop(name, new BooleanSchema());
    }

    /**
     * Add a string property.
     * @param name The name of the property.
     * @return The ObjectSchema instance.
     */
    public ObjectSchema str(String name) {
        return prop(name, new StringSchema());
    }

    /**
     * Add a string property with a specific format.
     * @param name The name of the property.
     * @param format The format of the string.
     * @return The ObjectSchema instance.
     */
    public ObjectSchema str(String name, StringSchema.Format format) {
        return prop(name, new StringSchema().format(format));
    }

    /**
     * Add a string property with allowed enum values.
     * @param name The name of the property.
     * @param enumValues The allowed values.
     * @return The ObjectSchema instance.
     */
    public ObjectSchema str(String name, List<String> enumValues) {
        return prop(name, new StringSchema().enumValues(enumValues.toArray(new String[0])));
    }

    /**
     * Add a number property.
     * @param name The name of the property.
     * @return The ObjectSchema instance.
     */
    public ObjectSchema num(String name) {
        return prop(name, new NumberSchema());
    }

    /**
     * Add a number property with min and max constraints.
     * @param name The name of the property.
     * @param min The minimum value.
     * @param max The maximum value.
     * @return The ObjectSchema instance.
     */
    public ObjectSchema num(String name, double min, double max) {
        return prop(name, new NumberSchema().min(min).max(max));
    }

    /**
     * Add a number property with allowed enum values.
     * @param name The name of the property.
     * @param enumValues The allowed values.
     * @return The ObjectSchema instance.
     */
    public ObjectSchema num(String name, List<Double> enumValues) {
        double[] values = new double[enumValues.size()];
        for (int i = 0; i < enumValues.size(); i++) {
            values[i] = enumValues.get(i);
        }
        return prop(name, new NumberSchema().enumValues(values));
    }

    /**
     * Add an integer property.
     * @param name The name of the property.
     * @return The ObjectSchema instance.
     */
    public ObjectSchema integer(String name) {
        return prop(name, new NumberSchema().integer());
    }

    /**
     * Add an integer property with min and max constraints.
     * @param name The name of the property.
     * @param min The minimum value.
     * @param max The maximum value.
     * @return The ObjectSchema instance.
     */
    public ObjectSchema integer(String name, long min, long max) {
        return prop(name, new NumberSchema().integer().min((double)min).max((double)max));
    }

    /**
     * Add an integer property with allowed enum values.
     * @param name The name of the property.
     * @param enumValues The allowed values.
     * @return The ObjectSchema instance.
     */
    public ObjectSchema integer(String name, List<Long> enumValues) {
        double[] values = new double[enumValues.size()];
        for (int i = 0; i < enumValues.size(); i++) {
            values[i] = enumValues.get(i).doubleValue();
        }
        return prop(name, new NumberSchema().integer().enumValues(values));
    }

    /**
     * Add an array property.
     * @param name The name of the property.
     * @return The ObjectSchema instance.
     */
    public ObjectSchema arr(String name) {
        return prop(name, new ArraySchema());
    }

    /**
     * Add an array property with specified item schema.
     * @param name The name of the property.
     * @param itemSchema The schema for the items in the array.
     * @return The ObjectSchema instance.
     */
    public ObjectSchema arr(String name, Schema itemSchema) {
        return prop(name, new ArraySchema().items(itemSchema));
    }

    /**
     * Add an object property.
     * @param name The name of the property.
     * @return The ObjectSchema instance.
     */
    public ObjectSchema obj(String name) {
        return prop(name, new ObjectSchema());
    }

    /**
     * Allow or disallow additional properties.
     * @param allowed Whether additional properties are allowed.
     * @return The ObjectSchema instance.
     */
    public ObjectSchema addProps(boolean allowed) {
        this.additionalProperties = allowed;
        return this;
    }

    /**
     * Define the schema for additional properties.
     * @param schema The schema for additional properties.
     * @return The ObjectSchema instance.
     */
    public ObjectSchema addProps(Schema schema) {
        this.additionalProperties = schema;
        return this;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        if (!properties.isEmpty()) {
            Map<String, Object> propsMap = new LinkedHashMap<>();
            for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                propsMap.put(entry.getKey(), entry.getValue().toMap());
            }
            map.put("properties", propsMap);
        }
        if (!required.isEmpty()) {
            map.put("required", required);
        }
        if (additionalProperties != null) {
            if (additionalProperties instanceof Schema) {
                map.put("additionalProperties", ((Schema) additionalProperties).toMap());
            } else {
                map.put("additionalProperties", additionalProperties);
            }
        }
        return map;
    }
}
