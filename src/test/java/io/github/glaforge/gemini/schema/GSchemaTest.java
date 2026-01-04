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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static io.github.glaforge.gemini.schema.GSchema.*;

public class GSchemaTest {

    @Test
    public void testStringSchema() {
        Schema s = str()
                .format(StringSchema.Format.DATE_TIME)
                .enumValues("A", "B", "C")
                .desc("A string schema");

        String json = GSchema.toJson(s);
        assertTrue(json.contains("\"type\" : \"string\""));
        assertTrue(json.contains("\"format\" : \"date-time\""));
        assertTrue(json.contains("\"enum\" : [ \"A\", \"B\", \"C\" ]"));
        assertTrue(json.contains("\"description\" : \"A string schema\""));

        Schema parsed = GSchema.fromJson(json);
        assertTrue(parsed instanceof StringSchema);
        // equals() is not implemented, so we re-serialize to compare
        assertEquals(json, GSchema.toJson(parsed));
    }

    @Test
    public void testNumberSchema() {
        NumberSchema s = num()
                .min(10.5)
                .max(20.5)
                .enumValues(12.0, 15.5);

        String json = GSchema.toJson(s);
        assertTrue(json.contains("\"type\" : \"number\""));
        assertTrue(json.contains("\"minimum\" : 10.5"));
        assertTrue(json.contains("\"maximum\" : 20.5"));
        assertTrue(json.contains("\"enum\" : [ 12.0, 15.5 ]"));

        Schema parsed = GSchema.fromJson(json);
        assertTrue(parsed instanceof NumberSchema);
        assertEquals(json, GSchema.toJson(parsed));
    }

    @Test
    public void testIntegerSchema() {
        NumberSchema s = integer()
            .min(1)
            .max(100);

        String json = GSchema.toJson(s);
        assertTrue(json.contains("\"type\" : \"integer\""));
        assertTrue(json.contains("\"minimum\" : 1.0")); // Serialized as double usually
        assertTrue(json.contains("\"maximum\" : 100.0"));

        Schema parsed = GSchema.fromJson(json);
        assertTrue(parsed instanceof NumberSchema);
         // Check if integer type is preserved after parsing
        // The parser logic sets integer() if type is "integer"
        assertEquals(json, GSchema.toJson(parsed));
    }


    @Test
    public void testBooleanSchema() {
        BooleanSchema s = bool();

        String json = GSchema.toJson(s);
        assertTrue(json.contains("\"type\" : \"boolean\""));

        Schema parsed = GSchema.fromJson(json);
        assertTrue(parsed instanceof BooleanSchema);
        assertEquals(json, GSchema.toJson(parsed));
    }

    @Test
    public void testArraySchema() {
        ArraySchema s = arr()
                .items(str())
                .minItems(1)
                .maxItems(10)
                .prefixItems(str(), num());

        String json = GSchema.toJson(s);
        assertTrue(json.contains("\"type\" : \"array\""));
        assertTrue(json.contains("\"items\" : {"));
        assertTrue(json.contains("\"type\" : \"string\""));
        assertTrue(json.contains("\"minItems\" : 1"));
        assertTrue(json.contains("\"maxItems\" : 10"));
        assertTrue(json.contains("\"prefixItems\" : [ {"));

        Schema parsed = GSchema.fromJson(json);
        assertTrue(parsed instanceof ArraySchema);
        assertEquals(json, GSchema.toJson(parsed));
    }

    @Test
    public void testObjectSchema() {
        ObjectSchema s = obj()
                .prop("name", str())
                .prop("age", integer())
                .req("name")
                .addProps(false);

        String json = GSchema.toJson(s);
        assertTrue(json.contains("\"type\" : \"object\""));
        assertTrue(json.contains("\"properties\" : {"));
        assertTrue(json.contains("\"required\" : [ \"name\" ]"));
        assertTrue(json.contains("\"additionalProperties\" : false"));

        Schema parsed = GSchema.fromJson(json);
        assertTrue(parsed instanceof ObjectSchema);
        assertEquals(json, GSchema.toJson(parsed));
    }

    @Test
    public void testObjectSchemaWithSchemaAddProps() {
        ObjectSchema s = obj()
                .addProps(str());

        String json = GSchema.toJson(s);
        assertTrue(json.contains("\"additionalProperties\" : {"));
        assertTrue(json.contains("\"type\" : \"string\""));

        Schema parsed = GSchema.fromJson(json);
        assertTrue(parsed instanceof ObjectSchema);
        assertEquals(json, GSchema.toJson(parsed));
    }

    @Test
    public void testComplexSchema() {
        // Define a schema using the fluent API with constraints
        Schema recipeSchema = obj()
                .prop("recipeName", str()
                        .format(StringSchema.Format.DATE_TIME)
                        .desc("The name of the recipe"))
                .prop("servings", num()
                        .integer()
                        .enumValues(1, 2, 4, 8))
                .prop("ingredients", arr()
                        .items(obj()
                                .prop("name", str())
                                .prop("quantity", str())
                                .addProps(false))
                        .prefixItems(str(), num()))
                .addProps(obj()
                        .prop("note", str()))
                .req("recipeName");

        // Convert to JSON
        String json = GSchema.toJson(recipeSchema);

        // Parse back to Schema
        Schema parsedSchema = GSchema.fromJson(json);
        String reparsedJson = GSchema.toJson(parsedSchema);

        assertEquals(json, reparsedJson, "Generated JSON should match re-parsed and re-generated JSON");
    }

    @Test
    public void testObjectConvenienceMethods() {
        ObjectSchema s = obj()
                .str("name")
                .integer("age")
                .bool("isActive")
                .num("score")
                .arr("tags")
                .obj("details");

        String json = GSchema.toJson(s);
        assertTrue(json.contains("\"name\" : {"));
        assertTrue(json.contains("\"type\" : \"string\""));
        assertTrue(json.contains("\"age\" : {"));
        assertTrue(json.contains("\"type\" : \"integer\""));
        assertTrue(json.contains("\"isActive\" : {"));
        assertTrue(json.contains("\"type\" : \"boolean\""));
        assertTrue(json.contains("\"score\" : {"));
        assertTrue(json.contains("\"type\" : \"number\""));
        assertTrue(json.contains("\"tags\" : {"));
        assertTrue(json.contains("\"type\" : \"array\""));
        assertTrue(json.contains("\"details\" : {"));
        assertTrue(json.contains("\"type\" : \"object\""));
    }
}
