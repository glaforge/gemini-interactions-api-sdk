package io.github.glaforge.gemini.schema;

import java.util.Map;

/**
 * Schema for Array types.
 * <p>
 * Example usage:
 * <pre>{@code
 * arr()
 *     .items(str())
 *     .minItems(1);
 * }</pre>
 */
public class ArraySchema extends Schema {
    private Schema items;
    private Integer minItems;
    private Integer maxItems;
    private Schema[] prefixItems;

    public ArraySchema() {
        super("array");
    }

    /**
     * Define the schema for items in this array.
     * @param itemsSchema The schema for the items.
     * @return The ArraySchema instance.
     */
    public ArraySchema items(Schema itemsSchema) {
        this.items = itemsSchema;
        return this;
    }

    /**
     * Set the minimum number of items.
     * @param min The minimum number of items.
     * @return The ArraySchema instance.
     */
    public ArraySchema minItems(int min) {
        this.minItems = min;
        return this;
    }

    /**
     * Set the maximum number of items.
     * @param max The maximum number of items.
     * @return The ArraySchema instance.
     */
    public ArraySchema maxItems(int max) {
        this.maxItems = max;
        return this;
    }

    /**
     * Define the schema for the first N items (prefixItems).
     * @param schemas The schemas for the prefix items.
     * @return The ArraySchema instance.
     */
    public ArraySchema prefixItems(Schema... schemas) {
        this.prefixItems = schemas;
        return this;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        if (items != null) {
            map.put("items", items.toMap());
        }
        if (prefixItems != null && prefixItems.length > 0) {
            java.util.List<Map<String, Object>> prefixItemsList = new java.util.ArrayList<>();
            for (Schema s : prefixItems) {
                prefixItemsList.add(s.toMap());
            }
            map.put("prefixItems", prefixItemsList);
        }
        if (minItems != null) {
            map.put("minItems", minItems);
        }
        if (maxItems != null) {
            map.put("maxItems", maxItems);
        }
        return map;
    }
}
