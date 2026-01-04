package io.github.glaforge.gemini.schema;

import java.util.Arrays;
import java.util.Map;

/**
 * Schema for String types.
 * <p>
 * Example usage:
 * <pre>{@code
 * StringSchema schema = new StringSchema()
 *     .format(StringSchema.Format.DATE)
 *     .enumValues("2023-11-01", "2023-11-02");
 * }</pre>
 */
public class StringSchema extends Schema {
    private String[] enumValues;
    private String format;

    public StringSchema() {
        super("string");
    }

    /**
     * Define the allowed enum values for this string.
     * @param values The allowed string values.
     * @return The StringSchema instance.
     */
    public StringSchema enumValues(String... values) {
        this.enumValues = values;
        return this;
    }

    public enum Format {
        DATE_TIME("date-time"),
        DATE("date"),
        TIME("time");

        private final String value;

        Format(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Define the format of the string (e.g., "date-time").
     * @param format The format string.
     * @return The StringSchema instance.
     */
    public StringSchema format(String format) {
        this.format = format;
        return this;
    }

    /**
     * Define the format of the string using a predefined enum.
     * @param format The format enum.
     * @return The StringSchema instance.
     */
    public StringSchema format(Format format) {
        this.format = format.getValue();
        return this;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        if (enumValues != null && enumValues.length > 0) {
            map.put("enum", Arrays.asList(enumValues));
        }
        if (format != null) {
            map.put("format", format);
        }
        return map;
    }
}
