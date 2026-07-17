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

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Main entry point for defining Gemini JSON Schemas.
 * <p>
 * Provides static factory methods to create schemas fluently.
 * <p>
 * Example:
 * <pre>{@code
 * import static io.github.glaforge.gemini.schema.GSchema.*;
 *
 * Schema schema = obj()
 *     .str("name")
 *     .integer("age")
 *     .req("name");
 *
 * String json = GSchema.toJson(schema);
 * }</pre>
 */
public class GSchema {
    /** Private constructor to prevent instantiation. */
    private GSchema() {}
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();

    // Factory methods

    /**
     * Creates a new string schema.
     * @return a new string schema.
     */
    public static StringSchema str() {
        return new StringSchema();
    }

    /**
     * Creates a new number schema.
     * @return a new number schema.
     */
    public static NumberSchema num() {
        return new NumberSchema();
    }

    /**
     * Creates a new integer schema.
     * @return a new integer schema.
     */
    public static NumberSchema integer() {
        return new NumberSchema().integer();
    }

    /**
     * Creates a new boolean schema.
     * @return a new boolean schema.
     */
    public static BooleanSchema bool() {
        return new BooleanSchema();
    }

    /**
     * Creates a new object schema.
     * @return a new object schema.
     */
    public static ObjectSchema obj() {
        return new ObjectSchema();
    }

    /**
     * Creates a new array schema.
     * @return a new array schema.
     */
    public static ArraySchema arr() {
        return new ArraySchema();
    }

    /**
     * Convert a schema to a JSON string.
     * @param schema The schema to convert.
     * @return The JSON string.
     */
    public static String toJson(Schema schema) {
        try {
            return MAPPER.writeValueAsString(schema.toMap());
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to generate JSON schema", e);
        }
    }

    /**
     * Parse a JSON schema string back into a Schema object.
     * @param json The JSON schema string.
     * @return The Schema object.
     */
    @SuppressWarnings("unchecked")
    public static Schema fromJson(String json) {
        try {
            Map<String, Object> map = MAPPER.readValue(json, Map.class);
            return parseSchema(map);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to parse JSON schema", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Schema parseSchema(Map<String, Object> map) {
        Object typeObj = map.get("type");
        String type = null;
        boolean nullable = false;

        if (typeObj instanceof String) {
            type = (String) typeObj;
        } else if (typeObj instanceof List) {
            List<String> types = (List<String>) typeObj;
            if (types.contains("null")) {
                nullable = true;
                for (String t : types) {
                    if (!"null".equals(t)) {
                        type = t;
                        break;
                    }
                }
            } else {
                type = types.get(0); // Fallback
            }
        }

        if (type == null) {
            throw new IllegalArgumentException("Schema type not found or unsupported: " + map);
        }

        Schema schema = null;
        switch (type) {
            case "string":
                schema = parseString(map);
                break;
            case "number":
            case "integer":
                schema = parseNumber(map, type);
                break;
            case "boolean":
                schema = new BooleanSchema();
                break;
            case "object":
                schema = parseObject(map);
                break;
            case "array":
                schema = parseArray(map);
                break;
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }

        if (nullable) {
            schema.nullable();
        }

        if (map.containsKey("description")) {
            schema.desc((String) map.get("description"));
        }
        if (map.containsKey("title")) {
            schema.title((String) map.get("title"));
        }

        return schema;
    }

    @SuppressWarnings("unchecked")
    private static StringSchema parseString(Map<String, Object> map) {
        StringSchema s = new StringSchema();
        if (map.containsKey("enum")) {
            List<String> enums = (List<String>) map.get("enum");
            s.enumValues(enums.toArray(new String[0]));
        }
        if (map.containsKey("format")) {
            s.format((String) map.get("format"));
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    private static NumberSchema parseNumber(Map<String, Object> map, String type) {
        NumberSchema s = new NumberSchema();
        if ("integer".equals(type)) {
            s.integer();
        }
        if (map.containsKey("enum")) {
            List<Number> enums = (List<Number>) map.get("enum");
            double[] values = new double[enums.size()];
            for (int i = 0; i < enums.size(); i++) {
                values[i] = enums.get(i).doubleValue();
            }
            s.enumValues(values);
        }
        if (map.containsKey("minimum")) {
            s.min(((Number) map.get("minimum")).doubleValue());
        }
        if (map.containsKey("maximum")) {
            s.max(((Number) map.get("maximum")).doubleValue());
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    private static ObjectSchema parseObject(Map<String, Object> map) {
        ObjectSchema s = new ObjectSchema();
        if (map.containsKey("properties")) {
            Map<String, Object> props = (Map<String, Object>) map.get("properties");
            for (Map.Entry<String, Object> entry : props.entrySet()) {
                s.prop(entry.getKey(), parseSchema((Map<String, Object>) entry.getValue()));
            }
        }
        if (map.containsKey("required")) {
            List<String> req = (List<String>) map.get("required");
            s.req(req.toArray(new String[0]));
        }
        if (map.containsKey("additionalProperties")) {
            Object ap = map.get("additionalProperties");
            if (ap instanceof Boolean) {
                s.addProps((Boolean) ap);
            } else if (ap instanceof Map) {
                s.addProps(parseSchema((Map<String, Object>) ap));
            }
        }
        return s;
    }

    @SuppressWarnings("unchecked")
    private static ArraySchema parseArray(Map<String, Object> map) {
        ArraySchema s = new ArraySchema();
        if (map.containsKey("items")) {
            s.items(parseSchema((Map<String, Object>) map.get("items")));
        }
        if (map.containsKey("prefixItems")) {
            List<Map<String, Object>> prefixItemsList = (List<Map<String, Object>>) map.get("prefixItems");
            Schema[] schemas = new Schema[prefixItemsList.size()];
            for (int i = 0; i < prefixItemsList.size(); i++) {
                schemas[i] = parseSchema(prefixItemsList.get(i));
            }
            s.prefixItems(schemas);
        }
        if (map.containsKey("minItems")) {
            s.minItems(((Number) map.get("minItems")).intValue());
        }
        if (map.containsKey("maxItems")) {
            s.maxItems(((Number) map.get("maxItems")).intValue());
        }
        return s;
    }
    /**
     * Generate a Schema from a Java Class.
     * @param clazz The class to generate schema from.
     * @return The generated Schema.
     */
    public static Schema fromClass(Class<?> clazz) {
        return fromType(clazz);
    }

    private static Schema fromType(Type type) {
        if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            if (clazz == String.class) {
                return str();
            } else if (clazz == int.class || clazz == Integer.class ||
                       clazz == long.class || clazz == Long.class ||
                       clazz == short.class || clazz == Short.class ||
                       clazz == byte.class || clazz == Byte.class) {
                return integer();
            } else if (clazz == double.class || clazz == Double.class ||
                       clazz == float.class || clazz == Float.class) {
                return num();
            } else if (clazz == boolean.class || clazz == Boolean.class) {
                return bool();
            } else if (clazz.isEnum()) {
                Object[] constants = clazz.getEnumConstants();
                String[] names = new String[constants.length];
                for (int i = 0; i < constants.length; i++) {
                    names[i] = constants[i].toString();
                }
                return str().enumValues(names);
            } else if (clazz.isArray()) {
                return arr().items(fromType(clazz.getComponentType()));
            } else {
                // Assume Object / POJO / Record
                ObjectSchema schema = obj();
                Class<?> current = clazz;
                while (current != null && current != Object.class) {
                    for (Field field : current.getDeclaredFields()) {
                        if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                            continue;
                        }
                        if (field.isSynthetic()) {
                            continue;
                        }
                        schema.prop(field.getName(), fromType(field.getGenericType()));
                    }
                    current = current.getSuperclass();
                }
                return schema;
            }
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Type rawType = pt.getRawType();
            if (rawType instanceof Class && Collection.class.isAssignableFrom((Class<?>) rawType)) {
                Type arg = pt.getActualTypeArguments()[0];
                return arr().items(fromType(arg));
            }
        }
        // Fallback or unknown
        return str().desc("Unknown type: " + type.getTypeName());
    }
}
